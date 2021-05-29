package demo.boundary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserBoundarySignup extends UserBoundaryBaseWithPassword {
	private String email;
	
	@Builder(builderMethodName = "userWithPasswordAndEmailBuilder")
	public UserBoundarySignup(String username, String password, String email) {
		super(username, password);
		setEmail(email);
	}
}
