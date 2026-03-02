package fr.tarot.controller;
import fr.tarot.domaine.dto.*;
import fr.tarot.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        var cu = (CurrentUser) authentication.getPrincipal();
        return new MeResponse(cu.id(), cu.email());
    }

    @PostMapping("/change-password")
    public void changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest req) {
        var cu = (CurrentUser) authentication.getPrincipal();
        authService.changePassword(cu, req);
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return authService.requestPasswordReset(req);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
    }
}
