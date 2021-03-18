package demo.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import demo.logic.util.XMLReader;
import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class DataSourceConfig {
	private XMLReader xmlReader;

    @Bean
    public DataSource getDataSource() {
    	Configurations configurations = xmlReader.loadConfigFile();
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceBuilder.url("jdbc:mysql://" + configurations.getDb().getIp() + ":" + configurations.getDb().getPort()+"/" + configurations.getDb().getDatabase());
        dataSourceBuilder.username(configurations.getDb().getUser());
        dataSourceBuilder.password(configurations.getDb().getPassword());
        return dataSourceBuilder.build();
    }
}