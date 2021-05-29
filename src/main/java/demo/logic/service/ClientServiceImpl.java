package demo.logic.service;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import demo.boundary.ClientBoundaryAddNewClient;
import demo.boundary.ClientBoundaryBase;
import demo.config.Configurations;
import demo.config.GeneralConfig;
import demo.config.Permission;
import demo.data.ClientEntity;
import demo.data.repository.ClientRepository;
import demo.logic.exceptions.InternalErrorException;
import demo.logic.exceptions.InvalidInputDataException;
import demo.logic.exceptions.InvalidUsernameOrPasswordException;
import demo.logic.service.interfaces.ClientService;
import demo.logic.utilities.XMLReader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ClientServiceImpl implements ClientService {
	private @NonNull XMLReader xmlReader;
	private final long PERMISSIONS_VALUE = Permission.GENERAL.getId();
	private @NonNull ClientRepository clientRepository;
	@PersistenceContext
	private @NonNull EntityManager entityManager;
	private GeneralConfig generalConfig;
	private @NonNull PlatformTransactionManager transactionManager;
	private @NonNull TransactionTemplate transactionTemplate;

	@EventListener(ApplicationReadyEvent.class)
	private void init() {
		Configurations configurations = xmlReader.loadConfigFile();
		Map<Permission, Object> permissions = configurations.getConfigurations(PERMISSIONS_VALUE);
		this.generalConfig = (GeneralConfig) permissions.get(Permission.GENERAL);
	}

	@Override
	public ClientBoundaryBase addNewClient(ClientBoundaryAddNewClient clientBoundary) {
		try {
			if (this.ClientExists(clientBoundary.getId())) {
				throw new InvalidUsernameOrPasswordException("Something went wrong");
			}

			ClientEntity entity = ClientEntity.builder().clientName(clientBoundary.getClientName())
					.id(clientBoundary.getId()).lastName(clientBoundary.getLastName())
					.phoneNumber(clientBoundary.getPhoneNumber()).creationTimestamp(new Date()).build();

			saveClient(entity);
			return ClientBoundaryBase.builder().clientName(clientBoundary.getClientName()).build();
		} catch (Exception e) {
			throw new InternalErrorException("Something went wrong");
		}
	}

	private boolean ClientExists(String id) {
		try {
			return findByClientId(id) != null;
		} catch (Exception e) {
			return false;
		}
	}

	private ClientEntity findByClientId(String id) {
		try {
			String sqlQueryAsString;
			Query query;
//			if (this.generalConfig.isSecure()) {
				sqlQueryAsString = "SELECT * FROM clients WHERE id = :id";
				query = this.entityManager.createNativeQuery(sqlQueryAsString, ClientEntity.class).setParameter("id",
						id);
//			} else {
//				sqlQueryAsString = "SELECT * FROM clients WHERE id = '" + id + "'";
//				query = this.entityManager.createNativeQuery(sqlQueryAsString, ClientEntity.class);
//			}
			return (ClientEntity) query.getResultList().get(0);
		} catch (Exception ex) {
			throw new InvalidInputDataException();
		}
	}

	@Transactional
	private void saveClient(ClientEntity clientEntity) {
		try {
			this.transactionTemplate = new TransactionTemplate(this.transactionManager);
			this.transactionTemplate.execute(transactionStatus -> {
				String sqlQueryAsString;
				Query query;
				if (this.generalConfig.isSecure()) {
					sqlQueryAsString = "INSERT INTO clients (id, client_name, creation_timestamp, last_name, phone_number)"
							+ " VALUES(:id, :client_name, :creation_timestamp, :last_name, :phone_number)";
					query = this.entityManager.createNativeQuery(sqlQueryAsString, ClientEntity.class)
							.setParameter("id", clientEntity.getId())
							.setParameter("client_name", clientEntity.getClientName())
							.setParameter("creation_timestamp", clientEntity.getCreationTimestamp())
							.setParameter("last_name", clientEntity.getLastName())
							.setParameter("phone_number", clientEntity.getPhoneNumber());
				} else {
					sqlQueryAsString = "INSERT INTO clients (id, creation_timestamp)" + " VALUES(:id, :creation_timestamp)";
					query = this.entityManager.createNativeQuery(sqlQueryAsString, ClientEntity.class)
							.setParameter("id", clientEntity.getId())
							.setParameter("creation_timestamp", clientEntity.getCreationTimestamp());
					query.executeUpdate();
					sqlQueryAsString = "UPDATE clients SET client_name = '" + clientEntity.getClientName()
							+ "', last_name = '" + clientEntity.getLastName() + "', phone_number = '"
							+ clientEntity.getPhoneNumber() + "' WHERE id = '" + clientEntity.getId() + "'";
					query = this.entityManager.createNativeQuery(sqlQueryAsString, ClientEntity.class);
				}
				query.executeUpdate();
				transactionStatus.flush();
				return null;
			});
		} catch (Exception e) {
			e.printStackTrace();
			throw new InternalErrorException("Something went wrong");
		}
	}

	@Override
	public ClientBoundaryBase[] getAll() {
		return clientRepository.findAll(PageRequest.of(0, 10, Direction.ASC, "clientName")).getContent().stream()
				.map(entity -> new ClientBoundaryBase(entity.getClientName())).collect(Collectors.toList())
				.toArray(new ClientBoundaryBase[0]);
	}

	@Override
	public void deleteAll() {
		this.clientRepository.deleteAll();
	}

}
