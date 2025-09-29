package com.xtremand.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xtremand.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	boolean existsByEmailIgnoreCase(String emailAddress);

	Optional<User> findByEmailIgnoreCase(String email);

	Optional<User> findByUsername(String username);
	
	@Query("SELECT u FROM User u WHERE u.email = :username")
    User fetchByUsername(@Param("username") String username);
}
