package fr.tarot.services;



import fr.tarot.domaine.dto.LoginRequest;
import fr.tarot.domaine.dto.LoginResponse;
import fr.tarot.domaine.dto.RegisterRequest;
import fr.tarot.domaine.dto.ChangePasswordRequest;
import fr.tarot.domaine.dto.CurrentUser;
import fr.tarot.domaine.dto.ForgotPasswordRequest;
import fr.tarot.domaine.dto.ForgotPasswordResponse;
import fr.tarot.domaine.dto.ResetPasswordRequest;
import fr.tarot.jpa.entities.Utilisateur;
import fr.tarot.jpa.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new IllegalArgumentException("Email déjà utilisé.");
        }

        Utilisateur u = new Utilisateur();
        u.setEmail(req.email().trim().toLowerCase());
        u.setMotDePasse(passwordEncoder.encode(req.password()));
        userRepository.save(u);


    }

    public LoginResponse login(LoginRequest req) {
        var utilisateur = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Identifiants invalides."));

        if (!passwordEncoder.matches(req.password(), utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("Identifiants invalides.");
        }

        String token = jwtService.generateAccessToken(utilisateur.getId(), utilisateur.getEmail());
        return new LoginResponse(token);
    }

    public void changePassword(CurrentUser currentUser, ChangePasswordRequest req) {
        var utilisateur = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

        if (!passwordEncoder.matches(req.currentPassword(), utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("Mot de passe actuel invalide.");
        }

        if (passwordEncoder.matches(req.newPassword(), utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit etre different.");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(req.newPassword()));
        userRepository.save(utilisateur);
    }

    public ForgotPasswordResponse requestPasswordReset(ForgotPasswordRequest req) {
        var utilisateurOpt = userRepository.findByEmailIgnoreCase(req.email());
        if (utilisateurOpt.isEmpty()) {
            return new ForgotPasswordResponse(null);
        }

        var utilisateur = utilisateurOpt.get();
        String token = jwtService.generatePasswordResetToken(utilisateur.getId(), utilisateur.getEmail());
        return new ForgotPasswordResponse(token);
    }

    public void resetPassword(ResetPasswordRequest req) {
        final Claims claims;
        try {
            claims = jwtService.parse(req.token());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Jeton de reinitialisation invalide ou expire.");
        }

        String type = claims.get("typ", String.class);
        if (!"password_reset".equals(type)) {
            throw new IllegalArgumentException("Jeton de reinitialisation invalide.");
        }

        var utilisateur = userRepository.findById(java.util.UUID.fromString(claims.getSubject()))
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

        if (passwordEncoder.matches(req.newPassword(), utilisateur.getMotDePasse())) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit etre different.");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(req.newPassword()));
        userRepository.save(utilisateur);
    }
}
