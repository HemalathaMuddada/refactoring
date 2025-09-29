package com.xtremand.user.role.service;

import java.util.Arrays;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.domain.entity.Role;
import com.xtremand.user.role.repository.RoleRepository;

@Service
@Transactional
public class RoleSeedServiceImpl implements RoleSeedService {

	private static final Logger log = LoggerFactory.getLogger(RoleSeedServiceImpl.class);

	private final RoleRepository roleRepository;

	public RoleSeedServiceImpl(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	@Override
	@Transactional
	public void createDefaultRoles() {
		List<Role> defaults = Arrays.asList(Role.builder().name("SUPER_ADMIN").roleKey("super-admin").build(),
				Role.builder().name("ADMIN").roleKey("admin").build(),
				Role.builder().name("TEAM_MEMBER").roleKey("team-memeber").build());

		for (Role role : defaults) {
			roleRepository.findByName(role.getName())
					.ifPresentOrElse(r -> log.info("Role '{}' already exists", role.getName()), () -> {
						roleRepository.save(role);
						log.info("Inserted role '{}'", role.getName());
					});
		}
	}
}
