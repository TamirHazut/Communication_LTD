package demo.config;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.server.Ssl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import demo.logic.utilities.XMLReader;
import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigSetter {
	private XMLReader xmlReader;

    @Bean
    public DataSource getDataSource() {
    	Configurations configurations = xmlReader.loadConfigFile();
    	Map<Permission, Object> permissions = configurations.getConfigurations(Permission.DB.getId());
    	DBConfig dbConfig = (DBConfig) permissions.get(Permission.DB);
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceBuilder.url("jdbc:mysql://" + dbConfig.getIp() + ":" + dbConfig.getPort()+"/" + dbConfig.getDatabase());
        dataSourceBuilder.username(dbConfig.getUser());
        dataSourceBuilder.password(dbConfig.getPassword());
        return dataSourceBuilder.build();
    }
    
    @Bean
    public Ssl getSSL() {
       	Configurations configurations = xmlReader.loadConfigFile();
    	Map<Permission, Object> permissions = configurations.getConfigurations(Permission.SSL.getId());
    	SSLConfig sslConfig = (SSLConfig) permissions.get(Permission.SSL);
    	Ssl ssl = new Ssl();
    	ssl.setKeyStoreType(sslConfig.getKeyStoreType());
    	ssl.setKeyStore(sslConfig.getKeyStore());
    	ssl.setKeyStorePassword(sslConfig.getKeyStorePassword());
    	ssl.setKeyAlias(sslConfig.getKeyAlias());
    	return ssl;
    	
    }
}