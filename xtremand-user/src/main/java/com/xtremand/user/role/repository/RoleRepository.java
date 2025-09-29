package com.xtremand.user.role.repository;

import java.util.List;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.Role;

/**
 * Repository for Role entities.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	/**
	 * Find a role by its name.
	 *
	 * @param name the role name
	 * 
	 * @return optional Role
	 */
	Optional<Role> findByName(String name);

	/**
	 * Find a role by its key.
	 *
	 * @param roleKey the role key
	 * 
	 * @return optional Role
	 */
	Optional<Role> findByRoleKey(String roleKey);

	/**
	 * Retrieve all role keys assigned to a user.
	 *
	 * @param userId the user id
	 * @return list of role keys
	 */
	@Query("select r.roleKey from UserRole ur join ur.role r where ur.user.id = :userId")
	List<String> findRoleKeysByUserId(@Param("userId") Long userId);
}