package demo.logic.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import demo.config.Configurations;
import demo.config.MailConfig;
import demo.config.Permission;
import demo.logic.exceptions.MailFailedException;
import demo.logic.service.interfaces.MailService;
import demo.logic.utilities.XMLReader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MailServiceImpl implements MailService {


	private final long PERMISSIONS_VALUE = Permission.MAIL.getId();
	@NonNull
	private final JavaMailSender javaMailSender;
	@NonNull
	private final Configuration configuration;
	@NonNull
	private final XMLReader xmlReader;
	private MailConfig mailConfig;
	
	@EventListener(ApplicationReadyEvent.class)
	private void init() {
		Configurations configurations = xmlReader.loadConfigFile();
		Map<Permission, Object> permissions = configurations.getConfigurations(PERMISSIONS_VALUE);
		this.mailConfig = (MailConfig) permissions.get(Permission.MAIL);
	}

	@Override
	public void sendMail(String to, String subject, String body) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(this.mailConfig.getFrom());
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			javaMailSender.send(message);
		} catch (MailException e) {
			e.printStackTrace();
			throw new MailFailedException("Sending email has failed");
		}
	}

	@Override
	public void sendResetPasswordMail(String to, String subject, String newPassword) {
		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
	        helper.setFrom(this.mailConfig.getFrom());
	        helper.setTo(to);
	        helper.setSubject(subject);
	        String body = getMailContent(newPassword);
	        helper.setText(body, true);
	        javaMailSender.send(mimeMessage);
		} catch (IOException | TemplateException | MessagingException e) {
			e.printStackTrace();
			throw new MailFailedException("Sending reset password email has failed");
		}
	}
	
	private String getMailContent(String newPassword) throws IOException, TemplateException {
        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();
        model.put("newPassword", newPassword);
        Template template = configuration.getTemplate(this.mailConfig.getResetPasswordHTMLTemplateFile());
        template.process(model, stringWriter);
        return stringWriter.getBuffer().toString();
    }

}
