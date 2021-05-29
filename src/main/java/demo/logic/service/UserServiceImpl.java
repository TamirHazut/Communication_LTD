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
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
	private @NonNull PlatformTransactionManager transactionManager;
	private @NonNull TransactionTemplate transactionTemplate;
	private Gson gson;

	@EventListener(ApplicationReadyEvent.class)
	private void init() {
		Configurations configurations = xmlReader.loadConfigFile();
		Map<Permission, Object> permissions = configurations.getConfigurations(PERMISSIONS_VALUE);
		this.generalConfig = (GeneralConfig) permissions.get(Permission.GENERAL);
		this.gson = new Gson();
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
					.numberOfLoginAttempt(0).creationTimestamp(new Date()).oldPasswords(this.gson.toJson(oldPasswords))
					.build();

			saveUser(entity);
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
		UserEntity entity = findByUsernameAndPassword(userBoundary);
		// Validate user credentials
		if (!this.passwordManager.validateLogin(userBoundary, entity)) {
			// login failed, saving changes
			entity.setNumberOfLoginAttempt(entity.getNumberOfLoginAttempt() + 1);
			updateUser(entity);
			throw new InvalidUsernameOrPasswordException();
		}
		// login succeeded, saving changes
		entity.setNumberOfLoginAttempt(0);
		updateUser(entity);
		return UserBoundaryBase.builder().username(userBoundary.getUsername()).build();
	}

	@Override
	public UserBoundaryBase changePassword(UserBoundaryPasswordChange userBoundary) {
		UserEntity entity = findByUsername(userBoundary.getUsername());
		// Check if password entered is correct
		if (!this.passwordManager.validateLogin(userBoundary, entity)) {
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
		UserEntity userEntity = findByUsername(userBoundary.getUsername());
		UserBoundaryPasswordChange userBoundaryPasswordChange = UserBoundaryPasswordChange
				.userWithOldAndNewPasswordsBuilder().username(userBoundary.getUsername())
				.password(userBoundary.getUsername()).newPassword(this.passwordManager.generateRandomPassword())
				.build();
		resetUserPassword(userBoundaryPasswordChange, userEntity);
		this.emailService.sendResetPasswordMail(userEntity.getEmail(), Constants.RESET_PASSWORD,
				userBoundaryPasswordChange.getNewPassword());
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
			List<String> oldPasswords = this.gson.fromJson(entity.getOldPasswords(), new TypeToken<List<String>>() {
			}.getType());
			if (oldPasswords.size() == this.passwordManager.getPasswordConfig().getHistory()) {
				oldPasswords.remove(0);
			}
			oldPasswords.add(newPassword);
			// Save the changes
			entity.setPassword(newPassword);
			entity.setOldPasswords(this.gson.toJson(oldPasswords));
			entity.setNumberOfLoginAttempt(0);
			updateUser(entity);
			return UserBoundaryBase.builder().username(userBoundary.getUsername()).build();
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			throw new InternalErrorException("Something went wrong");
		}
	}

	private boolean isUsernameExists(String username) {
		try {
			return findByUsername(username) != null;
		} catch (Exception ex) {
			return false;
		}
	}

	private UserEntity findByUsername(String username) {
		try {
			Query query;
			if (this.generalConfig.isSecure()) {
				String sqlQueryAsString = "SELECT * FROM users WHERE username = :username";
				query = this.entityManager.createNativeQuery(sqlQueryAsString, UserEntity.class)
						.setParameter("username", username);
			} else {
				String sqlQueryAsString = "SELECT * FROM users WHERE username = '" + username + "'";
				query = this.entityManager.createNativeQuery(sqlQueryAsString, UserEntity.class);
			}
			return (UserEntity) query.getResultList().get(0);
		} catch (Exception ex) {
			throw new InvalidUsernameOrPasswordException();
		}
	}

	private UserEntity findByUsernameAndPassword(UserBoundaryBaseWithPassword boundary) {
		try {
			Query query;
			String sqlQueryAsString;
			// Check if user exists first
			UserEntity userEntity = findByUsername(boundary.getUsername());
			// Validating the users password
			String password = new String(
					this.passwordManager.encrypt(boundary.getPassword(), userEntity.getSalt().getBytes("ISO-8859-1")),
					"ISO-8859-1");
			if (this.generalConfig.isSecure()) {
				sqlQueryAsString = "SELECT * FROM users WHERE username = :username AND password = :password";
				query = this.entityManager.createNativeQuery(sqlQueryAsString, UserEntity.class)
						.setParameter("username", userEntity.getUsername()).setParameter("password", password);
			} else {
				sqlQueryAsString = "SELECT * FROM users WHERE username = '" + boundary.getUsername()
						+ "' AND password = :password";
				query = this.entityManager.createNativeQuery(sqlQueryAsString, UserEntity.class)
						.setParameter("password", password);
			}
			return (UserEntity) query.getResultList().get(0);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new InvalidUsernameOrPasswordException();
		}
	}

	@Transactional
	private void updateUser(UserEntity userEntity) {
		try {
			this.transactionTemplate = new TransactionTemplate(this.transactionManager);
			this.transactionTemplate.execute(transactionStatus -> {
				String sqlQueryAsString;
				Query query;
//				if (this.generalConfig.isSecure()) {
				sqlQueryAsString = "UPDATE users SET number_of_login_attempt = :number_of_login_attempt, password = :password, "
						+ "old_passwords = :old_passwords WHERE username = :username";
				query = this.entityManager.createNativeQuery(sqlQueryAsString, UserEntity.class)
						.setParameter("number_of_login_attempt", userEntity.getNumberOfLoginAttempt())
						.setParameter("password", userEntity.getPassword())
						.setParameter("old_passwords", userEntity.getOldPasswords())
						.setParameter("username", userEntity.getUsername());
//				} else {
//					sqlQueryAsString = "UPDATE users SET number_of_login_attempt = "
//							+ userEntity.getNumberOfLoginAttempt() + ", " + "password = '" + userEntity.getPassword()
//							+ "', " + "old_passwords = '" + userEntity.getOldPasswords() + "' WHERE username = '"
//							+ userEntity.getUsername() + "'";
//					query = this.entityManager.createNativeQuery(sqlQueryAsString, UserEntity.class);
//				}
				query.executeUpdate();
				transactionStatus.flush();
				return null;
			});
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new InternalErrorException("Something went wrong");
		}
	}

	@Transactional
	private void saveUser(UserEntity userEntity) {
		try {
			this.transactionTemplate = new TransactionTemplate(this.transactionManager);
			this.transactionTemplate.execute(transactionStatus -> {
				String sqlQueryAsString;
				Query query;
				if (this.generalConfig.isSecure()) {
					sqlQueryAsString = "INSERT INTO users (username, creation_timestamp, email, number_of_login_attempt, password, salt, old_passwords)"
							+ " VALUES(:username, :creation_timestamp, :email, :number_of_login_attempt, :password, :salt, :old_passwords)";
					query = this.entityManager.createNativeQuery(sqlQueryAsString, UserEntity.class)
							.setParameter("username", userEntity.getUsername())
							.setParameter("creation_timestamp", userEntity.getCreationTimestamp())
							.setParameter("email", userEntity.getEmail())
							.setParameter("number_of_login_attempt", userEntity.getNumberOfLoginAttempt())
							.setParameter("password", userEntity.getPassword())
							.setParameter("salt", userEntity.getSalt())
							.setParameter("old_passwords", userEntity.getOldPasswords());

				} else {
					sqlQueryAsString = "INSERT INTO users (username, creation_timestamp, email, number_of_login_attempt, password, salt, old_passwords)"
							+ " VALUES('" + userEntity.getUsername()
							+ "', :creation_timestamp, :email, :number_of_login_attempt, :password, :salt, :old_passwords)";
					query = this.entityManager.createNativeQuery(sqlQueryAsString, UserEntity.class)
							.setParameter("creation_timestamp", userEntity.getCreationTimestamp())
							.setParameter("number_of_login_attempt", userEntity.getNumberOfLoginAttempt())
							.setParameter("password", userEntity.getPassword())
							.setParameter("salt", userEntity.getSalt())
							.setParameter("old_passwords", userEntity.getOldPasswords())
							.setParameter("username", userEntity.getUsername());
				}
				query.executeUpdate();
				transactionStatus.flush();
				return null;
			});
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new InternalErrorException("Something went wrong");
		}
	}

}
