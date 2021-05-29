package demo.data.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import demo.data.UserEntity;

public interface UserRepository extends PagingAndSortingRepository<UserEntity, String>{

}
