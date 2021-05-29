package demo.logic.exceptions;

public class MailFailedException extends InternalErrorException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MailFailedException() {
		super();
	}

	public MailFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MailFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public MailFailedException(String message) {
		super(message);
	}

	public MailFailedException(Throwable cause) {
		super(cause);
	}


}
