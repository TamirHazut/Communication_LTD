package demo.logic.exceptions;

public class InvalidInputDataException extends BadRequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidInputDataException() {
		super();
	}

	public InvalidInputDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidInputDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidInputDataException(String message) {
		super(message);
	}

	public InvalidInputDataException(Throwable cause) {
		super(cause);
	}

	
}
