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
public class MailConfig {
	private String host;
	@Min(0)
	@Max(65536)
	private int port;
	private String username;
	private String password;
	private String from;
	private String resetPasswordHTMLTemplateFile;
}
