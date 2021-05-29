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
public class UserBoundaryBaseWithPassword extends UserBoundaryBase {
	private String password;
	
	@Builder(builderMethodName = "userWithPasswordBuilder")
	public UserBoundaryBaseWithPassword(String username, String password) {
		super(username);
		setPassword(password);
	}
}
