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
public class PasswordConfig {
	private int length;
	private String symbols;
	private int history;
	private String dictionaryFile;
	private int loginAttempts;
}
