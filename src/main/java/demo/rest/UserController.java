package demo.rest;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import demo.boundary.UserBoundaryBase;
import demo.boundary.UserBoundaryBaseWithPassword;
import demo.boundary.UserBoundaryPasswordChange;
import demo.boundary.UserBoundarySignup;
import demo.logic.exceptions.BadRequestException;
import demo.logic.exceptions.InternalErrorException;
import demo.logic.service.interfaces.UserService;
import lombok.AllArgsConstructor;

@CrossOrigin(origins = "https://localhost:8443")
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
			path = "/forgotPassword",
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundaryBase forgotPassword(@RequestBody UserBoundaryBase userBoundary) {
		return this.userService.forgotPassword(userBoundary);	
	}
	
	@RequestMapping(
			path = "/users/getAll",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public UserBoundaryBase[] getAll() {
		return this.userService.getAll();
	}
	
	@RequestMapping(
			path = "/users/deleteAll",
			method = RequestMethod.DELETE)
	public void deleteAll() {
		this.userService.deleteAll();
	}
	
	@ExceptionHandler
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	public Map<String, Object> handleException(InternalErrorException e) {
		String error = e.getMessage();
		if (error == null) {
			error = "Somthing went wrong";
		}
		return Collections.singletonMap("message", error);
	}
	
	@ExceptionHandler
	@ResponseStatus(code = HttpStatus.BAD_REQUEST)
	public Map<String, Object> handleException(BadRequestException e) {
		String error = e.getMessage();
		if (error == null) {
			error = "Somthing went wrong";
		}
		return Collections.singletonMap("message", error);
	}
}
