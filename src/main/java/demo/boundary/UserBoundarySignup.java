package demo.boundary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserBoundarySignup extends UserBoundaryBaseWithPassword {
	private String email;
}
