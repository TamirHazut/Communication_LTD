package demo.boundary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserBoundaryPasswordChange extends UserBoundaryBaseWithPassword {
	private String newPassword;
}
