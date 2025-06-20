package it.unisalento.iot2425.watchapp.user.security;

import it.unisalento.iot2425.watchapp.user.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static it.unisalento.iot2425.watchapp.user.security.SecurityConstants.JWT_ISSUER;


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtilities jwtUtilities ;

    @Autowired
    private CustomUserDetailsService customerUserDetailsService ;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {


        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        String id = null;
        String role = null;


        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            jwt = authorizationHeader.substring(7);

            if(jwt.equals(JWT_ISSUER)){
                //è il token statico.
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_SYSTEM"));
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("system", null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                //ci prendiamo il token ed estraiamo username, id e ruolo
                username = jwtUtilities.extractUsername(jwt);
                id=jwtUtilities.extractUserId(jwt);
                role=jwtUtilities.extractUserRole(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    //mettiamo nella lista il ruolo dell'utente
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_"+ role));

                    UserDetails userDetails = this.customerUserDetailsService.loadUserByUsername(username);

                    if (jwtUtilities.validateToken(jwt, userDetails)) {

                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, authorities);
                        usernamePasswordAuthenticationToken
                                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }

            }
        }

        chain.doFilter(request, response);
    }

}