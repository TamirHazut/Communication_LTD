package demo.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SSLConfig {
	private String keyStoreType;
	private String keyStore;
	private String keyStorePassword;
	private String keyAlias;
}
