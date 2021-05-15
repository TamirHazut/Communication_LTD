package demo.logic.service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import demo.boundary.UserBoundaryBase;
import demo.boundary.UserBoundaryBaseWithPassword;
import demo.boundary.UserBoundaryPasswordChange;
import demo.boundary.UserBoundarySignup;
import demo.config.Configurations;
import demo.config.GeneralConfig;
import demo.config.Permission;
import demo.data.UserEntity;
import demo.data.repository.UserRepository;
import demo.logic.exceptions.InternalErrorException;
import demo.logic.exceptions.InvalidInputDataException;
import demo.logic.exceptions.InvalidPasswordException;
import demo.logic.exceptions.InvalidUsernameOrPasswordException;
import demo.logic.service.interfaces.MailService;
import demo.logic.service.interfaces.UserService;
import demo.logic.utilities.Constants;
import demo.logic.utilities.PasswordManager;
import demo.logic.utilities.XMLReader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceImpl implements UserService {
	private final int SALT_BYTE_SIZE = 16;
	private final long PERMISSIONS_VALUE = Permission.GENERAL.getId();
	private @NonNull UserRepository userRepository;
	private @NonNull XMLReader xmlReader;
	private @NonNull PasswordManager passwordManager;
	private GeneralConfig generalConfig;
	@PersistenceContext
	private @NonNull EntityManager entityManager;
	private @NonNull MailService emailService;

	@EventListener(ApplicationReadyEvent.class)
	private void init() {
		Configurations configurations = xmlReader.loadConfigFile();
		Map<Permission, Object> permissions = configurations.getConfigurations(PERMISSIONS_VALUE);
		this.generalConfig = (GeneralConfig) permissions.get(Permission.GENERAL);
	}

	@Override
	public UserBoundaryBase signup(UserBoundarySignup userBoundary) {
		try {
			if (this.isUsernameExists(userBoundary.getUsername())
					|| !this.passwordManager.validatePasswordForSignup(userBoundary)) {
				throw new InvalidUsernameOrPasswordException("Something went wrong");
			}
			// Generate new salt and hash
			byte[] salt = new byte[SALT_BYTE_SIZE];
			this.passwordManager.generateSaltValue(salt);
			byte[] hash = this.passwordManager.encrypt(userBoundary.getPassword(), salt);
			// Setting up the new user
			List<String> oldPasswords = new ArrayList<>();
			oldPasswords.add(new String(hash, PasswordManager.BYTE_CHARSET));
			UserEntity entity = UserEntity.builder().username(userBoundary.getUsername())
					.password(new String(hash, PasswordManager.BYTE_CHARSET))
					.salt(new String(salt, PasswordManager.BYTE_CHARSET)).email(userBoundary.getEmail())
					.numberOfLoginAttempt(0).creationTimestamp(new Date()).oldPasswords(oldPasswords).build();

			saveUser(entity, true);
			return UserBoundaryBase.builder().username(userBoundary.getUsername()).build();
		} catch (UnsupportedEncodingException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new InternalErrorException("Something went wrong");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new InvalidInputDataException("Some of the data entered is invalid");
		}
	}

	@Override
	public UserBoundaryBase login(UserBoundaryBaseWithPassword userBoundary) {
		// Check if user exists
		UserEntity entity = getUser(userBoundary.getUsername());
		// Validate user credentials
		if (!this.passwordManager.validatePasswordForLogin(userBoundary, entity)) {
			// login failed, saving changes
			entity.setNumberOfLoginAttempt(entity.getNumberOfLoginAttempt() + 1);
			saveUser(entity, false);
			throw new InvalidUsernameOrPasswordException();
		}
		// login succeeded, saving changes
		entity.setNumberOfLoginAttempt(0);
		saveUser(entity, false);
		return UserBoundaryBase.builder().username(userBoundary.getUsername()).build();
	}

	@Override
	public UserBoundaryBase changePassword(UserBoundaryPasswordChange userBoundary) {
		UserEntity entity = getUser(userBoundary.getUsername());
		// Check if password entered is correct
		if (!this.passwordManager.validatePasswordForLogin(userBoundary, entity)) {
			throw new InvalidPasswordException("Password entered does not match");
		}
		// Check if new password is valid
		if (!this.passwordManager.validatePasswordForChangePassword(userBoundary, entity)) {
			throw new InvalidPasswordException("New password is not valid");
		}
		return resetUserPassword(userBoundary, entity);

	}

	@Override
	public UserBoundaryBase forgotPassword(UserBoundaryBase userBoundary) {
		UserEntity userEntity = getUser(userBoundary.getUsername());
		UserBoundaryPasswordChange userBoundaryPasswordChange = UserBoundaryPasswordChange
				.userWithOldAndNewPasswordsBuilder().username(userBoundary.getUsername())
				.password(userBoundary.getUsername()).newPassword(this.passwordManager.generateRandomPassword())
				.build();
		resetUserPassword(userBoundaryPasswordChange, userEntity);
		this.emailService.sendResetPasswordMail(userEntity.getEmail(), Constants.RESET_PASSWORD, userBoundaryPasswordChange.getNewPassword());
		return userBoundaryPasswordChange;
	}

	@Override
	public UserBoundaryBase[] getAll() {
		return userRepository.findAll(PageRequest.of(0, 10, Direction.ASC, "username")).getContent().stream()
				.map(entity -> new UserBoundaryBase(entity.getUsername())).collect(Collectors.toList())
				.toArray(new UserBoundaryBase[0]);
	}

	@Override
	public void deleteAll() {
		this.userRepository.deleteAll();
	}

	private UserBoundaryBase resetUserPassword(UserBoundaryPasswordChange userBoundary, UserEntity entity) {
		try {
			// Generate new hash
			byte[] newHash = this.passwordManager.encrypt(userBoundary.getNewPassword(),
					entity.getSalt().getBytes(PasswordManager.BYTE_CHARSET));
			String newPassword = new String(newHash, PasswordManager.BYTE_CHARSET);
			// Update old passwords list
			List<String> oldPasswords = entity.getOldPasswords();
			if (oldPasswords.size() == this.passwordManager.getPasswordConfig().getHistory()) {
				oldPasswords.remove(0);
			}
			oldPasswords.add(newPassword);
			// Save the changes
			entity.setPassword(newPassword);
			entity.setOldPasswords(oldPasswords);
			entity.setNumberOfLoginAttempt(0);
			saveUser(entity, false);
			return UserBoundaryBase.builder().username(userBoundary.getUsername()).build();
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			throw new InternalErrorException("Something went wrong");
		}
	}

	private boolean isUsernameExists(String username) {
		UserEntity userEntity;
		if (this.generalConfig.isSecure()) {
			return this.userRepository.existsById(username);
		}
		String sqlQueryAsString = "SELECT id FROM users WHERE id = " + username + " limit 1;";
		userEntity = (UserEntity) this.entityManager.createNativeQuery(sqlQueryAsString, UserEntity.class)
				.getSingleResult();
		return userEntity != null;
	}

	private UserEntity getUser(String username) {
		UserEntity userEntity;
		if (this.generalConfig.isSecure()) {
			return this.userRepository.findById(username).orElseThrow(() -> new InvalidUsernameOrPasswordException());
		}
		String sqlQueryAsString = "SELECT id FROM users WHERE id = " + username + " limit 1;";
		userEntity = (UserEntity) this.entityManager.createNativeQuery(sqlQueryAsString, UserEntity.class)
				.getSingleResult();
		return userEntity;
	}

	private UserEntity saveUser(UserEntity userEntity, boolean newUser) {
		if (this.generalConfig.isSecure()) {
			return this.userRepository.save(userEntity);
		}
		String sqlQueryAsString;
		if (newUser) {
			sqlQueryAsString = "INSERT INTO users (username, creation_timestamp, email, number_of_login_attempt, password, salt, old_passwords)"
					+ "VALUES(" + userEntity.getUsername() + "," + userEntity.getCreationTimestamp() + ","
					+ userEntity.getEmail() + "," + userEntity.getNumberOfLoginAttempt() + ","
					+ userEntity.getPassword() + "," + userEntity.getSalt() + "," + userEntity.getOldPasswords() + ");";
		} else {
			sqlQueryAsString = "UPDATE users" + "SET username = " + userEntity.getUsername() + ","
					+ "SET creation_timestamp = " + userEntity.getCreationTimestamp() + "," + "SET email = "
					+ userEntity.getEmail() + "," + "SET number_of_login_attempt = "
					+ userEntity.getNumberOfLoginAttempt() + "," + "SET password = " + userEntity.getPassword() + ","
					+ "SET salt = " + userEntity.getSalt() + "," + "SET old_passwords = " + userEntity.getOldPasswords()
					+ "WHERE id = " + userEntity.getUsername() + ";";
		}
		userEntity = (UserEntity) this.entityManager.createNativeQuery(sqlQueryAsString, UserEntity.class)
				.getSingleResult();
		return userEntity;
	}

}
