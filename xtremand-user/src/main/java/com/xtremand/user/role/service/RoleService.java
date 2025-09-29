package com.xtremand.user.role.service;

import java.util.List;

import java.util.Optional;

import com.xtremand.domain.entity.Role;

/**
 * Service interface for managing roles.
 */
public interface RoleService {
	/**
	 * Create a new role with the given name.
	 *
	 * @param name the name of the role
	 * 
	 * @return the created Role
	 * 
	 * @throws RoleAlreadyExistsException if a role with the same name already
	 *                                    exists
	 */
	Role createRole(String name);

	/**
	 * Retrieve all roles.
	 *
	 * @return list of roles
	 */
	List<Role> getAllRoles();

	/**
	 * Retrieve all role keys.
	 *
	 * @return list of role key strings
	 */
	List<String> getAllRoleKeys();

	/**
	 * Find a role by its name.
	 *
	 * @param name the role name
	 * 
	 * @return optional Role
	 */
	Optional<Role> getRoleByName(String name);
}