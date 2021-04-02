package demo.logic.service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import demo.boundary.UserBoundaryBase;
import demo.boundary.UserBoundaryBaseWithPassword;
import demo.boundary.UserBoundaryPasswordChange;
import demo.boundary.UserBoundarySignup;
import demo.data.UserEntity;
import demo.data.repository.UserRepository;
import demo.logic.exceptions.InternalErrorException;
import demo.logic.exceptions.InvalidPasswordException;
import demo.logic.exceptions.InvalidUsernameOrPasswordException;
import demo.logic.service.interfaces.UserService;
import demo.logic.utilities.PasswordManager;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceImpl implements UserService {
	private final int SALT_BYTE_SIZE = 16;
	private UserRepository userRepository;
	private PasswordManager passwordValidator;
	
	@Override
	public UserBoundaryBase signup(UserBoundarySignup userBoundary) {
		try {
			if (this.userRepository.existsById(userBoundary.getUsername()) || !this.passwordValidator.validatePasswordForSignup(userBoundary)) {
				throw new InvalidUsernameOrPasswordException("Something went wrong");
			}
			// Generate new salt and hash
			byte[] salt = new byte[SALT_BYTE_SIZE];
			this.passwordValidator.generateSaltValue(salt);
			byte[] hash = this.passwordValidator.encrypt(userBoundary.getPassword(), salt);
			// Setting up the new user
			List<String> oldPasswords = new ArrayList<>();
			oldPasswords.add(new String(hash, PasswordManager.BYTE_CHARSET));
			UserEntity entity = UserEntity.builder()
					.username(userBoundary.getUsername())
					.password(new String(hash, PasswordManager.BYTE_CHARSET))
					.salt(new String(salt, PasswordManager.BYTE_CHARSET))
					.email(userBoundary.getEmail())
					.numberOfLoginAttempt(0)
					.creationTimestamp(new Date())
					.oldPasswords(oldPasswords)
					.build();

			this.userRepository.save(entity);
			return UserBoundaryBase.builder()
					.username(userBoundary.getUsername())
					.build();
		} catch (UnsupportedEncodingException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new InternalErrorException("Something went wrong");
		}
	}

	@Override
	public UserBoundaryBase login(UserBoundaryBaseWithPassword userBoundary) {
		// Check if user exists
		UserEntity entity = this.userRepository.findById(userBoundary.getUsername())
				.orElseThrow(() -> new InvalidUsernameOrPasswordException());
		// Validate user credentials
		if (!this.passwordValidator.validatePasswordForLogin(userBoundary, entity)) {
			// login failed, saving changes
			entity.setNumberOfLoginAttempt(entity.getNumberOfLoginAttempt() + 1);
			this.userRepository.save(entity);
			throw new InvalidUsernameOrPasswordException();
		}
		// login succeeded, saving changes
		entity.setNumberOfLoginAttempt(0);
		this.userRepository.save(entity);
		return UserBoundaryBase.builder().username(userBoundary.getUsername()).build();
	}
	
	@Override
	public UserBoundaryBase changePassword(UserBoundaryPasswordChange userBoundary) {
		try {
			UserEntity entity = this.userRepository.findById(userBoundary.getUsername())
					.orElseThrow(() -> new InvalidUsernameOrPasswordException());
			// Check if password entered is correct
			if (!this.passwordValidator.validatePasswordForLogin(userBoundary, entity)) {
				throw new InvalidPasswordException("Password entered does not match");
			}
			// Check if new password is valid
			if (!this.passwordValidator.validatePasswordForChangePassword(userBoundary, entity)) {
				throw new InvalidPasswordException("New password is not valid");
			}
			// Generate new hash
			byte[] newHash = this.passwordValidator.encrypt(userBoundary.getNewPassword(), entity.getSalt().getBytes(PasswordManager.BYTE_CHARSET));
			String newPassword = new String(newHash, PasswordManager.BYTE_CHARSET);
			// Update old passwords list
			List<String> oldPasswords = entity.getOldPasswords();
			if (oldPasswords.size() == this.passwordValidator.getPasswordConfig().getHistory()) {
				oldPasswords.remove(0);
			}
			oldPasswords.add(newPassword);
			// Save the changes
			entity.setPassword(newPassword);
			entity.setOldPasswords(oldPasswords);
			this.userRepository.save(entity);
			return UserBoundaryBase.builder()
					.username(userBoundary.getUsername())
					.build();
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			throw new InternalErrorException("Something went wrong");
		}
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

}
