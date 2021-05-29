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
public class ClientBoundaryAddNewClient extends ClientBoundaryBase {
	private String id;
	private String lastName;
	private String phoneNumber;
	
	@Builder(builderMethodName = "ClientWithDetailsBuilder")
	public ClientBoundaryAddNewClient(String clientName, String lastName, String phoneNumber, String id) {
		super(clientName);
		setId(id);
		setLastName(lastName);
		setPhoneNumber(phoneNumber);
	}
}
