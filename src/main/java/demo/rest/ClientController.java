package demo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import demo.boundary.ClientBoundaryAddNewClient;
import demo.boundary.ClientBoundaryBase;
import demo.logic.service.interfaces.ClientService;
import lombok.AllArgsConstructor;

@CrossOrigin(origins = "https://localhost:8443")
@RestController
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ClientController {
	private ClientService clientService;
	
	@RequestMapping(
			path = "/addClient",
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ClientBoundaryBase addNewClient(@RequestBody ClientBoundaryAddNewClient clientBoundary) {
		return this.clientService.addNewClient(clientBoundary);	
	}
	
	@RequestMapping(
			path = "/clients/getAll",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ClientBoundaryBase[] getAll() {
		return this.clientService.getAll();
	}
	
	@RequestMapping(
			path = "/clients/deleteAll",
			method = RequestMethod.DELETE)
	public void deleteAll() {
		this.clientService.deleteAll();
	}

}
