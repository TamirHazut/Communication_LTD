package demo.logic.service.interfaces;

import demo.boundary.UserBoundaryBase;
import demo.boundary.UserBoundaryBaseWithPassword;
import demo.boundary.UserBoundaryPasswordChange;
import demo.boundary.UserBoundarySignup;

public interface UserService {

	UserBoundaryBase signup(UserBoundarySignup userBoundary);

	UserBoundaryBase login(UserBoundaryBaseWithPassword userBoundary);

	UserBoundaryBase changePassword(UserBoundaryPasswordChange userBoundary);
	
	UserBoundaryBase forgotPassword(UserBoundaryBase userBoundary);
	
	UserBoundaryBase[] getAll();

	void deleteAll();

}
