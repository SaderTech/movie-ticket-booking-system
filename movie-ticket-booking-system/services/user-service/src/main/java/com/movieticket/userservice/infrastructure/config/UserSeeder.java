package com.movieticket.userservice.infrastructure.config;


import com.movieticket.userservice.infrastructure.persistence.entity.UserJpaEntity;
import com.movieticket.userservice.infrastructure.persistence.entity.UserRoleJpaEntity;

import com.movieticket.userservice.infrastructure.persistence.repository.JpaUserRepository;
import com.movieticket.userservice.infrastructure.persistence.repository.JpaRoleRepository;
import com.movieticket.userservice.infrastructure.persistence.repository.JpaUserRoleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;



@Component
@RequiredArgsConstructor
public class UserSeeder implements CommandLineRunner {


    private final JpaUserRepository userRepository;

    private final JpaRoleRepository roleRepository;

    private final JpaUserRoleRepository userRoleRepository;

    private final PasswordEncoder passwordEncoder;



    @Override
    public void run(String... args) {


        createAdmin();


        createStaff();


        createUser();


        System.out.println(
                "User Seeder completed!"
        );

    }





    // ======================================
    // CREATE ADMIN
    // ======================================

    private void createAdmin() {


        UserJpaEntity user =
                userRepository.findByEmail(
                                "admin@movieticket.com"
                        )
                        .orElseGet(() -> {


                            UserJpaEntity newUser =
                                    new UserJpaEntity();


                            newUser.setUsername(
                                    "movie_admin"
                            );


                            newUser.setEmail(
                                    "admin@movieticket.com"
                            );


                            newUser.setPassword(
                                    passwordEncoder.encode(
                                            "Admin@123"
                                    )
                            );


                            newUser.setFullName(
                                    "Movie Ticket Administrator"
                            );


                            newUser.setPhone(
                                    "0981000001"
                            );


                            newUser.setIsActive(true);


                            newUser.setCreatedAt(
                                    LocalDateTime.now()
                            );


                            newUser.setUpdatedAt(
                                    LocalDateTime.now()
                            );


                            return userRepository.save(newUser);

                        });



        assignRole(
                user.getId(),
                "ADMIN"
        );

    }






    // ======================================
    // CREATE STAFF
    // ======================================

    private void createStaff() {


        UserJpaEntity user =
                userRepository.findByEmail(
                                "staff@movieticket.com"
                        )
                        .orElseGet(() -> {


                            UserJpaEntity newUser =
                                    new UserJpaEntity();



                            newUser.setUsername(
                                    "cinema_staff"
                            );


                            newUser.setEmail(
                                    "staff@movieticket.com"
                            );


                            newUser.setPassword(
                                    passwordEncoder.encode(
                                            "Staff@123"
                                    )
                            );


                            newUser.setFullName(
                                    "Cinema Management Staff"
                            );


                            newUser.setPhone(
                                    "0981000002"
                            );


                            newUser.setIsActive(true);


                            newUser.setCreatedAt(
                                    LocalDateTime.now()
                            );


                            newUser.setUpdatedAt(
                                    LocalDateTime.now()
                            );


                            return userRepository.save(newUser);

                        });



        assignRole(
                user.getId(),
                "STAFF"
        );

    }







    // ======================================
    // CREATE USER
    // ======================================

    private void createUser() {


        UserJpaEntity user =
                userRepository.findByEmail(
                                "user@movieticket.com"
                        )
                        .orElseGet(() -> {


                            UserJpaEntity newUser =
                                    new UserJpaEntity();



                            newUser.setUsername(
                                    "customer01"
                            );


                            newUser.setEmail(
                                    "user@movieticket.com"
                            );


                            newUser.setPassword(
                                    passwordEncoder.encode(
                                            "User@123"
                                    )
                            );


                            newUser.setFullName(
                                    "Movie Customer"
                            );


                            newUser.setPhone(
                                    "0981000003"
                            );


                            newUser.setIsActive(true);


                            newUser.setCreatedAt(
                                    LocalDateTime.now()
                            );


                            newUser.setUpdatedAt(
                                    LocalDateTime.now()
                            );


                            return userRepository.save(newUser);

                        });



        assignRole(
                user.getId(),
                "USER"
        );

    }








    // ======================================
    // ASSIGN ROLE
    // ======================================

    private void assignRole(
            Long userId,
            String roleName
    ) {


        Long roleId =
                roleRepository
                        .findByRoleName(roleName)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Role not found: "
                                                + roleName
                                )
                        )
                        .getId()
                        .longValue();




        boolean exists =
                userRoleRepository
                        .existsByUserIdAndRoleId(
                                userId,
                                roleId
                        );



        if(exists) {

            return;

        }





        UserRoleJpaEntity userRole =
                new UserRoleJpaEntity();



        userRole.setUserId(
                userId
        );


        userRole.setRoleId(
                roleId
        );



        userRoleRepository.save(
                userRole
        );

    }

}