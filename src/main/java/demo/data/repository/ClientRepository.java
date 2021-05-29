package demo.data.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import demo.data.ClientEntity;

public interface ClientRepository extends PagingAndSortingRepository<ClientEntity, String>{

}
