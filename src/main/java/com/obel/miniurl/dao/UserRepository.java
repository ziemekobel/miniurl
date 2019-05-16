package com.obel.miniurl.dao;

import org.springframework.data.repository.CrudRepository;

import com.obel.miniurl.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

	User findByUsername(String username);
}
