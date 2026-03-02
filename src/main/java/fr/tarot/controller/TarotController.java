package fr.tarot.controller;

import fr.tarot.domaine.dto.CurrentUser;
import fr.tarot.domaine.dto.tarot.*;
import fr.tarot.services.TarotService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tarot")
public class TarotController {

    private final TarotService tarotService;

    public TarotController(TarotService tarotService) {
        this.tarotService = tarotService;
    }

    @PostMapping("/joueurs")
    public JoueurResponse createJoueur(Authentication authentication, @Valid @RequestBody CreateJoueurRequest req) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        return tarotService.createJoueur(currentUser.id(), req);
    }

    @GetMapping("/joueurs")
    public List<JoueurResponse> listJoueurs(Authentication authentication) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        return tarotService.listJoueurs(currentUser.id());
    }

    @PostMapping("/sessions")
    public SessionSummaryResponse createSession(Authentication authentication, @Valid @RequestBody CreateSessionRequest req) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        return tarotService.createSession(currentUser.id(), req);
    }

    @GetMapping("/sessions")
    public List<SessionSummaryResponse> listSessions(Authentication authentication) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        return tarotService.listSessions(currentUser.id());
    }

    @GetMapping("/sessions/{sessionId}")
    public SessionDetailResponse getSession(Authentication authentication, @PathVariable UUID sessionId) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        return tarotService.getSession(currentUser.id(), sessionId);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public void deleteSession(Authentication authentication, @PathVariable UUID sessionId) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        tarotService.deleteSession(currentUser.id(), sessionId);
    }

    @PostMapping("/sessions/{sessionId}/parties")
    public PartieResponse createPartie(Authentication authentication,
                                       @PathVariable UUID sessionId,
                                       @Valid @RequestBody CreatePartieRequest req) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        return tarotService.createPartie(currentUser.id(), sessionId, req);
    }

    @GetMapping("/sessions/{sessionId}/parties")
    public List<PartieResponse> listParties(Authentication authentication, @PathVariable UUID sessionId) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        return tarotService.listParties(currentUser.id(), sessionId);
    }

    @DeleteMapping("/sessions/{sessionId}/parties/{partieId}")
    public void deletePartie(Authentication authentication,
                             @PathVariable UUID sessionId,
                             @PathVariable UUID partieId) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        tarotService.deletePartie(currentUser.id(), sessionId, partieId);
    }

    @GetMapping("/sessions/{sessionId}/feuille")
    public FeuilleSessionResponse getFeuilleSession(Authentication authentication, @PathVariable UUID sessionId) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        return tarotService.getFeuilleSession(currentUser.id(), sessionId);
    }

    @GetMapping("/bilan/groupes")
    public List<GroupeBilanResponse> bilanGroupes(Authentication authentication) {
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        return tarotService.bilanGroupes(currentUser.id());
    }
}
