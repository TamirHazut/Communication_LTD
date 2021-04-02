package demo.logic.utilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import demo.boundary.UserBoundaryBaseWithPassword;
import demo.boundary.UserBoundaryPasswordChange;
import demo.config.Configurations;
import demo.config.PasswordConfig;
import demo.config.Permission;
import demo.data.UserEntity;
import demo.data.repository.UserRepository;
import demo.logic.exceptions.InvalidFileException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PasswordManager {
	public static final String BYTE_CHARSET = "ISO-8859-1";
	private final long permissionsValue = Permission.PASSWORD.getId();
	@Getter private PasswordConfig passwordConfig;
	private @NonNull XMLReader xmlReader;
	private @NonNull UserRepository userRepository;
	private SecureRandom random;

	@EventListener(ApplicationReadyEvent.class)
	private void init() {
		this.setPasswordConfig();
		this.random = new SecureRandom();
	}
	
	public byte[] encrypt(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		return factory.generateSecret(spec).getEncoded();
	}
	
	public void generateSaltValue(byte[] salt) {
		random.nextBytes(salt);
	}

	private void setPasswordConfig() {
		Configurations configurations = xmlReader.loadConfigFile();
		Map<Permission, Object> permissions = configurations.getConfigurations(permissionsValue);
		this.passwordConfig = (PasswordConfig) permissions.get(Permission.PASSWORD);
	}

	public boolean validatePasswordForSignup(UserBoundaryBaseWithPassword userBoundary) {
		return (userBoundary.getPassword() != null && validateLength(userBoundary.getPassword())
				&& validateSymbols(userBoundary.getPassword()) && validateDictionary(userBoundary));
	}

	public boolean validatePasswordForLogin(UserBoundaryBaseWithPassword userBoundary, UserEntity userEntity) {
		try {
			/*
			 * charset need to be "ISO-8859-1" in order to keep 1-1 conversion byte[] ->
			 * String: new String(byte[], "ISO-8859-1") String -> byte[]:
			 * String.getBytes("ISO-8859-1")
			 */
			byte[] salt = userEntity.getSalt().getBytes(BYTE_CHARSET);
			byte[] hash = encrypt(userBoundary.getPassword(), salt);
			return (validatePasswordForSignup(userBoundary)
					&& validateLoginAttempts(userEntity.getNumberOfLoginAttempt())
					&& Arrays.equals(hash, userEntity.getPassword().getBytes(BYTE_CHARSET)));
		} catch (UnsupportedEncodingException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean validatePasswordForChangePassword(UserBoundaryPasswordChange userBoundary, UserEntity entity) {
		return validatePasswordForSignup(userBoundary) && validateHistory(userBoundary, entity);
	}
	
	private boolean validateLength(String password) {
		return (password.length() >= this.passwordConfig.getLength());
	}

	private boolean validateSymbols(String password) {
		Pattern pattern = Pattern.compile(this.passwordConfig.getSymbols());
		Matcher m = pattern.matcher(password);
		return m.matches();
	}

	private boolean validateHistory(UserBoundaryPasswordChange userBoundary, UserEntity entity) {
		try {
			byte[] salt = entity.getSalt().getBytes(BYTE_CHARSET);
			byte[] newPasswordHash = encrypt(userBoundary.getNewPassword(), salt);
			byte[] oldPasswordHash;
			for (String oldPassword : entity.getOldPasswords()) {
				oldPasswordHash = oldPassword.getBytes(BYTE_CHARSET);
				if (Arrays.equals(newPasswordHash, oldPasswordHash)) {
					return false;
				}
			}
			return true;
		} catch (UnsupportedEncodingException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean validateDictionary(UserBoundaryBaseWithPassword userBoundary) {
		boolean passwordExistsInDictionary[] = { false };
		try (Stream<String> lines = Files.lines(Paths.get(this.passwordConfig.getDictionaryFile()),
				Charset.defaultCharset())) {
			lines.forEachOrdered(line -> {
				if (userBoundary.getPassword().equals(line)) {
					passwordExistsInDictionary[0] = true;
					return;
				}
			});
		} catch (IOException e) {
			throw new InvalidFileException(e.getMessage());
		}
		return !passwordExistsInDictionary[0];
	}

	private boolean validateLoginAttempts(int numberOfLoginAttempts) {
		return (numberOfLoginAttempts <= this.passwordConfig.getLoginAttempts());
	}


}