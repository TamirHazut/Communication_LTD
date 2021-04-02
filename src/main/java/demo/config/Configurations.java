package demo.config;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@XmlRootElement(name = "configurations")
public class Configurations {
	private GeneralConfig general;
	private PasswordConfig password;
	private DBConfig db;
	private SSLConfig ssl; 
	
	@XmlElement(name = "db")
	public void setdB(DBConfig db) {
		this.db = db;
	}
	@XmlElement(name = "general")
	public void setGeneral(GeneralConfig general) {
		this.general = general;
	}
	@XmlElement(name = "password")
	public void setPassword(PasswordConfig password) {
		this.password = password;
	}
	
	@XmlElement(name = "ssl")
	public void setSSL(SSLConfig ssl) {
		this.ssl = ssl;
	}
	
	public Map<Permission, Object> getConfigurations(long permission) {
		Map<Permission, Object> configurations = new TreeMap<>();
		List<Permission> permissions = Permission.getPermissions(permission);
		for (Permission p : permissions) {
			switch (p) {
			case GENERAL:
				configurations.put(p, this.general);
				break;
			case PASSWORD:
				configurations.put(p, this.password);
				break;
			case DB:
				configurations.put(p, this.db);
				break;
			case SSL:
				configurations.put(p, this.ssl);
				break;
			default:
				break;
			}
		}
		return configurations;
	}
}
