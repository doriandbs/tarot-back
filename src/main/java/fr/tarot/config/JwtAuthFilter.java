package fr.tarot.config;

import fr.tarot.domaine.dto.CurrentUser;
import fr.tarot.jpa.repositories.UserRepository;
import fr.tarot.services.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = auth.substring("Bearer ".length()).trim();
        try {
            Claims claims = jwtService.parse(token);
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);

            // Optionnel : vérifier existence utilisateur (révocation simple)
            if (userRepository.findById(userId).isEmpty()) {
                chain.doFilter(request, response);
                return;
            }

            var principal = new CurrentUser(userId, email);
            var authToken = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            // Token invalide => on laisse Spring Security renvoyer 401 si endpoint protégé
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}