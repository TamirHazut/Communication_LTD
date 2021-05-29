package demo.config;

import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Http2;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import demo.logic.utilities.XMLReader;
import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigSetter implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
	private XMLReader xmlReader;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
		Configurations configurations = xmlReader.loadConfigFile();
		Map<Permission, Object> permissions = configurations.getConfigurations(Permission.SSL.getId());
		SSLConfig sslConfig = (SSLConfig) permissions.get(Permission.SSL);
        factory.setContextPath("");
        factory.setPort(sslConfig.getPort());
        factory.setSsl(getSSL());
        factory.setHttp2(getHttp2());
    }
    
    public Http2 getHttp2() {
    	Http2 http2Config = new Http2();
    	http2Config.setEnabled(true);
    	return http2Config;
    }
	
	public Ssl getSSL() {
		Configurations configurations = xmlReader.loadConfigFile();
		Map<Permission, Object> permissions = configurations.getConfigurations(Permission.SSL.getId());
		SSLConfig sslConfig = (SSLConfig) permissions.get(Permission.SSL);
		Ssl ssl = new Ssl();
		if (sslConfig.isEnable()) {
			ssl.setEnabled(sslConfig.isEnable());
			ssl.setKeyStore(sslConfig.getKeyStore());
			ssl.setKeyStoreType(sslConfig.getKeyStoreType());
			ssl.setKeyStorePassword(sslConfig.getKeyStorePassword());
			ssl.setKeyAlias(sslConfig.getKeyAlias());
			ssl.setKeyPassword(sslConfig.getKeyPassword());
			ssl.setEnabledProtocols(sslConfig.getEnabledProtocols().split(","));
		}
		return ssl;
	}

	@Bean
	public DataSource getDataSource() {
		Configurations configurations = xmlReader.loadConfigFile();
		Map<Permission, Object> permissions = configurations.getConfigurations(Permission.DB.getId());
		DBConfig dbConfig = (DBConfig) permissions.get(Permission.DB);
		DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
		dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
		dataSourceBuilder
				.url("jdbc:mysql://" + dbConfig.getIp() + ":" + dbConfig.getPort() + "/" + dbConfig.getDatabase());
		dataSourceBuilder.username(dbConfig.getUser());
		dataSourceBuilder.password(dbConfig.getPassword());
		return dataSourceBuilder.build();
	}
	
	@Bean
	public JavaMailSender getJavaMailSender() {
		Configurations configurations = xmlReader.loadConfigFile();
		Map<Permission, Object> permissions = configurations.getConfigurations(Permission.MAIL.getId());
		MailConfig mailConfig = (MailConfig) permissions.get(Permission.MAIL);
	    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	    mailSender.setHost(mailConfig.getHost());
	    mailSender.setPort(mailConfig.getPort());
	    
	    mailSender.setUsername(mailConfig.getUsername());
	    mailSender.setPassword(mailConfig.getPassword());
	    
	    Properties props = mailSender.getJavaMailProperties();
	    props.put("mail.transport.protocol", "smtp");
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.debug", "true");
	    
	    return mailSender;
	}

}