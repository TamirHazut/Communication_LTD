package demo.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

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
	private boolean enable;
	@Min(0)
	@Max(65536)
	private int port;
	private String enabledProtocols;
	private String keyStoreType;
	private String keyStore;
	private String keyStorePassword;
	private String keyAlias;
	private String keyPassword;
}
