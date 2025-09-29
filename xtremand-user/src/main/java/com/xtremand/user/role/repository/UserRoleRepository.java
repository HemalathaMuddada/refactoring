package com.xtremand.user.role.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.entity.UserRole;

/**
 * Repository for persisting UserRole entities.
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
	List<UserRole> findByUserIdIn(Collection<Long> userIds);

	List<UserRole> findByUserId(Long userId);

	@Query("select count(ur) > 0 from UserRole ur join ur.role r where ur.user.id = :userId and r.roleKey = :roleKey")
	boolean existsByUserIdAndRoleKey(@Param("userId") Long userId, @Param("roleKey") String roleKey);

	@Query("select count(ur) > 0 from UserRole ur join ur.role r where ur.user.id = :userId and r.name = :roleName")
	boolean hasRole(@Param("userId") Long userId, @Param("roleName") String roleName);

	void deleteByUserId(Long userId);
}