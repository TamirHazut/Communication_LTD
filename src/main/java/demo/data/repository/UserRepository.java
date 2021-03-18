package demo.data.repository;

import org.springframework.data.repository.CrudRepository;

import demo.data.UserEntity;

public interface UserRepository extends CrudRepository<UserEntity, String> {

}
