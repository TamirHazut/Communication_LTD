package demo.logic.service.interfaces;

import demo.boundary.ClientBoundaryAddNewClient;
import demo.boundary.ClientBoundaryBase;

public interface ClientService {
	
	ClientBoundaryBase addNewClient(ClientBoundaryAddNewClient clientBoundary);
	
	ClientBoundaryBase[] getAll();

	void deleteAll();
	

}
