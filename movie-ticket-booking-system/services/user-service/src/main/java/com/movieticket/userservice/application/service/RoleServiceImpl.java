package com.movieticket.userservice.application.service;



import com.movieticket.userservice.application.service.RoleService;
import com.movieticket.userservice.domain.entity.Role;
import com.movieticket.userservice.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;


    @Override
    public Role createRole(
            String roleName,
            String description
    ) {

        if (roleRepository.existsByRoleName(roleName)) {

            throw new RuntimeException(
                    "Role already exists"
            );
        }

        Role role = Role.create(
                roleName,
                description
        );

        return roleRepository.save(role);
    }


    @Override
    public Role getById(Long id) {

        return roleRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Role not found"
                        )
                );
    }


    @Override
    public Role getByName(String roleName) {

        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Role not found"
                        )
                );
    }


    @Override
    public List<Role> getAll() {

        return roleRepository.findAll();
    }


    @Override
    public void delete(Long id) {

        Role role = getById(id);

        roleRepository.deleteById(
                role.getId()
        );
    }
}