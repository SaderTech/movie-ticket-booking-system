package com.movieticket.userservice.infrastructure.config;


import com.movieticket.userservice.domain.entity.Role;
import com.movieticket.userservice.domain.repository.RoleRepository;
import com.movieticket.userservice.domain.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {


    private final RoleRepository roleRepository;

    private final UserRoleRepository userRoleRepository;



    @Override
    public void run(String... args) {


        System.out.println(
                "Starting Role Seeder..."
        );



        // ==============================
        // CREATE ADMIN IF NOT EXISTS
        // ==============================

        if(roleRepository.findByRoleName("ADMIN").isEmpty()) {

            roleRepository.save(
                    Role.create(
                            "ADMIN",
                            "System administrator"
                    )
            );

        }



        // ==============================
        // CREATE STAFF IF NOT EXISTS
        // ==============================

        if(roleRepository.findByRoleName("STAFF").isEmpty()) {

            roleRepository.save(
                    Role.create(
                            "STAFF",
                            "Cinema staff"
                    )
            );

        }



        // ==============================
        // CREATE USER IF NOT EXISTS
        // ==============================

        Role userRole =
                roleRepository.findByRoleName("USER")
                        .orElseGet(() ->

                                roleRepository.save(
                                        Role.create(
                                                "USER",
                                                "Customer"
                                        )
                                )

                        );



        // ==============================
        // MIGRATE CUSTOMER -> USER
        // ==============================

        roleRepository.findByRoleName("CUSTOMER")
                .ifPresent(customerRole -> {


                    System.out.println(
                            "Migrating CUSTOMER role to USER..."
                    );


                    userRoleRepository.updateRoleId(

                            customerRole.getId(),

                            userRole.getId()

                    );



                    roleRepository.deleteById(
                            customerRole.getId()
                    );


                    System.out.println(
                            "CUSTOMER role removed"
                    );

                });



        System.out.println(
                "Role Seeder completed!"
        );

    }

}