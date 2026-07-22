package com.movieticket.userservice.application.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import lombok.RequiredArgsConstructor;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;



@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {


    private final JwtService jwtService;



    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException, IOException {



        String authHeader =
                request.getHeader("Authorization");



        if(authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request,response);
            return;
        }



        String token =
                authHeader.substring(7);



        if(jwtService.validateToken(token)) {



            String email =
                    jwtService.extractEmail(token);



            List<String> roles =
                    jwtService.extractRoles(token);

            if (roles == null || roles.isEmpty()) {
                roles = List.of("USER");
            }



            List<SimpleGrantedAuthority> authorities =
                    roles.stream()

                            .map(role ->
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role
                                    )
                            )

                            .collect(Collectors.toList());




            UsernamePasswordAuthenticationToken authentication =

                    new UsernamePasswordAuthenticationToken(

                            email,

                            null,

                            authorities

                    );



            if(SecurityContextHolder.getContext()
                    .getAuthentication() == null) {


                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

            }

        }



        filterChain.doFilter(request,response);

    }
}