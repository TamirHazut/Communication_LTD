package demo.boundary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserBoundaryPasswordChange extends UserBoundaryBaseWithPassword {
	private String newPassword;
	
	@Builder(builderMethodName = "userWithOldAndNewPasswordsBuilder")
	public UserBoundaryPasswordChange(String username, String password, String newPassword) {
		super(username, password);
		setNewPassword(newPassword);
	}
}
