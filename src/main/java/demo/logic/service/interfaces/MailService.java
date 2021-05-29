package demo.logic.service.interfaces;

public interface MailService {
	
	void sendMail(String to, String subject, String body);

	void sendResetPasswordMail(String to, String subject, String newPassword);
}
