package com.xtremand.config.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xtremand.domain.dto.MailConfigDTO;
import com.xtremand.domain.entity.MailConfig;
import com.xtremand.domain.entity.User;
import com.xtremand.domain.enums.EmailConfigType;

@Repository
public interface MailConfigRepository extends JpaRepository<MailConfig, Long> {

	Optional<MailConfig> findById(Long id);

	Optional<MailConfig> findByEmail(String email);

	Optional<MailConfig> findByCreatedBy(User user);

	@Query("SELECT new com.xtremand.domain.dto.MailConfigDTO(m.id, m.configType, m.email, m.displayName, "
			+ "m.username, m.password, m.oauthAccessToken, m.createdAt, m.updatedAt, m.host, m.port, "
			+ "m.oauthRefreshToken, m.tokenExpiry) " + "FROM MailConfig m WHERE m.createdBy = :user")
	List<MailConfigDTO> findDtoByCreatedBy(@Param("user") User user);
	
	
	@Query("SELECT m FROM MailConfig m WHERE m.createdBy.email = :email AND m.configType = :type")
	Optional<MailConfig> findByEmailAndConfigType(@Param("email") String email,@Param("type") EmailConfigType  type);
	
	@Query("SELECT m FROM MailConfig m WHERE m.createdBy.email = :email AND m.id = :id")
	Optional<MailConfig> findByCreatedByUserAndId(@Param("email") String email,@Param("id") Long id);
	
	@Query("SELECT m FROM MailConfig m WHERE m.createdBy = :user AND m.id = :id")
	MailConfig findByCreatedByUserAndIds(@Param("user") User user,@Param("id") Long id);
	
	
	@Query("SELECT m FROM MailConfig m WHERE m.createdBy = :user")
	List<MailConfig> findAllByCreatedByUser(@Param("user") User user);

	

}
