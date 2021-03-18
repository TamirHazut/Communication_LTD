package demo.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@XmlRootElement(name = "configurations")
public class Configurations {
	private General general;
	private Password password;
	private DB db;
	
	@XmlElement(name = "db")
	public void setdB(DB db) {
		this.db = db;
	}
	@XmlElement(name = "general")
	public void setGeneral(General general) {
		this.general = general;
	}
	@XmlElement(name = "password")
	public void setPassword(Password password) {
		this.password = password;
	}
}
