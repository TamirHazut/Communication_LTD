package demo.logic.exceptions;

public class InvalidUsernameOrPasswordException extends BadRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String EXCEPTION_MESSAGE = "Username or Password are not valid";

	public InvalidUsernameOrPasswordException() {
		this(EXCEPTION_MESSAGE);
	}

	public InvalidUsernameOrPasswordException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidUsernameOrPasswordException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidUsernameOrPasswordException(String message) {
		super(message);
	}

	public InvalidUsernameOrPasswordException(Throwable cause) {
		this(EXCEPTION_MESSAGE, cause);
	}

}
