package demo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import demo.boundary.UserBoundaryBase;
import demo.boundary.UserBoundaryBaseWithPassword;
import demo.boundary.UserBoundaryPasswordChange;
import demo.boundary.UserBoundarySignup;
import demo.logic.service.interfaces.UserService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {
	private UserService userService;
	
	@RequestMapping(
			path = "/signup",
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundaryBase signup(@RequestBody UserBoundarySignup userBoundary) {
		return this.userService.signup(userBoundary);	
	}
	
	@RequestMapping(
			path = "/login",
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundaryBase login(@RequestBody UserBoundaryBaseWithPassword userBoundary) {
		return this.userService.login(userBoundary);
	}
	
	@RequestMapping(
			path = "/changePassword",
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundaryBase changePassword(@RequestBody UserBoundaryPasswordChange userBoundary) {
		return this.userService.changePassword(userBoundary);	
	}
	
	@RequestMapping(
			path = "/getAll",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundaryBase[] getAll() {
		return this.userService.getAll();
	}
	
	@RequestMapping(
			path = "/deleteAll",
			method = RequestMethod.DELETE)
	public void deleteAll() {
		this.userService.deleteAll();
	}
}
