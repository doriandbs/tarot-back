package fr.tarot.services;

import fr.tarot.domaine.dto.tarot.*;
import fr.tarot.jpa.entities.*;
import fr.tarot.jpa.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TarotService {

    private static final int BONUS_POINTS = 40;
    private static final int TOTAL_TAS_POINTS = 91;
    private static final BigDecimal BONUS_POINTS_DECIMAL = BigDecimal.valueOf(BONUS_POINTS);

    private final UserRepository userRepository;
    private final JoueurRepository joueurRepository;
    private final SessionTarotRepository sessionTarotRepository;
    private final SessionParticipantRepository sessionParticipantRepository;
    private final PartieRepository partieRepository;
    private final ScorePartieRepository scorePartieRepository;
    private final BonusPartieRepository bonusPartieRepository;

    public TarotService(UserRepository userRepository,
                        JoueurRepository joueurRepository,
                        SessionTarotRepository sessionTarotRepository,
                        SessionParticipantRepository sessionParticipantRepository,
                        PartieRepository partieRepository,
                        ScorePartieRepository scorePartieRepository,
                        BonusPartieRepository bonusPartieRepository) {
        this.userRepository = userRepository;
        this.joueurRepository = joueurRepository;
        this.sessionTarotRepository = sessionTarotRepository;
        this.sessionParticipantRepository = sessionParticipantRepository;
        this.partieRepository = partieRepository;
        this.scorePartieRepository = scorePartieRepository;
        this.bonusPartieRepository = bonusPartieRepository;
    }

    @Transactional
    public JoueurResponse createJoueur(UUID utilisateurId, CreateJoueurRequest req) {
        String nom = normalize(req.nom());
        if (joueurRepository.existsByUtilisateurIdAndNomIgnoreCase(utilisateurId, nom)) {
            throw new IllegalArgumentException("Ce joueur existe deja.");
        }

        Utilisateur utilisateur = userRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

        Joueur joueur = new Joueur();
        joueur.setNom(nom);
        joueur.setUtilisateur(utilisateur);
        joueurRepository.save(joueur);
        return new JoueurResponse(joueur.getId(), joueur.getNom());
    }

    @Transactional(readOnly = true)
    public List<JoueurResponse> listJoueurs(UUID utilisateurId) {
        return joueurRepository.findByUtilisateurIdOrderByNomAsc(utilisateurId).stream()
                .map(j -> new JoueurResponse(j.getId(), j.getNom()))
                .toList();
    }

    @Transactional
    public SessionSummaryResponse createSession(UUID utilisateurId, CreateSessionRequest req) {
        Set<UUID> uniqueParticipantIds = new LinkedHashSet<>(req.participantIds());
        if (uniqueParticipantIds.size() < 3) {
            throw new IllegalArgumentException("Une session doit contenir au moins 3 participants.");
        }

        Utilisateur utilisateur = userRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

        List<Joueur> joueurs = joueurRepository.findAllById(uniqueParticipantIds).stream()
                .filter(j -> j.getUtilisateur().getId().equals(utilisateurId))
                .toList();
        if (joueurs.size() != uniqueParticipantIds.size()) {
            throw new IllegalArgumentException("Un ou plusieurs joueurs sont invalides.");
        }

        SessionTarot session = new SessionTarot();
        session.setUtilisateur(utilisateur);
        session.setDateSession(req.dateSession());
        session.setNom(normalizeOptional(req.nom()));
        sessionTarotRepository.save(session);

        List<SessionParticipant> sessionParticipants = new ArrayList<>();
        for (Joueur joueur : joueurs) {
            SessionParticipant sp = new SessionParticipant();
            sp.setSession(session);
            sp.setJoueur(joueur);
            sessionParticipants.add(sp);
        }
        sessionParticipantRepository.saveAll(sessionParticipants);

        return buildSessionSummary(session);
    }

    @Transactional(readOnly = true)
    public List<SessionSummaryResponse> listSessions(UUID utilisateurId) {
        List<SessionTarot> sessions = sessionTarotRepository.findByUtilisateurIdOrderByDateSessionDescCreeLeDesc(utilisateurId);
        return sessions.stream().map(this::buildSessionSummary).toList();
    }

    @Transactional(readOnly = true)
    public SessionDetailResponse getSession(UUID utilisateurId, UUID sessionId) {
        SessionTarot session = sessionTarotRepository.findByIdAndUtilisateurId(sessionId, utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable."));
        return buildSessionDetail(session);
    }

    @Transactional
    public PartieResponse createPartie(UUID utilisateurId, UUID sessionId, CreatePartieRequest req) {
        SessionTarot session = sessionTarotRepository.findByIdAndUtilisateurId(sessionId, utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable."));

        List<SessionParticipant> participants = sessionParticipantRepository.findBySessionIdOrderByJoueurNom(sessionId);
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("Aucun participant dans cette session.");
        }

        Map<UUID, SessionParticipant> sessionParticipantByJoueurId = participants.stream()
                .collect(Collectors.toMap(sp -> sp.getJoueur().getId(), sp -> sp));

        Set<UUID> winners = new LinkedHashSet<>(req.winnerJoueurIds());
        if (winners.isEmpty()) {
            throw new IllegalArgumentException("Au moins un gagnant est obligatoire.");
        }
        if (!sessionParticipantByJoueurId.keySet().containsAll(winners)) {
            throw new IllegalArgumentException("Un ou plusieurs gagnants ne font pas partie de la session.");
        }

        int pointsFaitsGagnants = resolvePointsFaitsGagnants(req);
        Integer pointsAFaire = pointsAFaireByBouts(req.boutsGagnants());
        int pointsDeBase = 25 + Math.abs(pointsFaitsGagnants - pointsAFaire);
        BigDecimal multiplicateurPreneur = multiplicateurByPreneurType(req.preneurType());
        BigDecimal pointsContrat = BigDecimal.valueOf(pointsDeBase)
                .multiply(multiplicateurPreneur)
                .setScale(2, RoundingMode.HALF_UP);
        boolean gagnantsReussissent = pointsFaitsGagnants >= pointsAFaire;
        BigDecimal pointsGagnants = gagnantsReussissent ? pointsContrat : pointsContrat.negate();
        BigDecimal pointsPerdants = pointsGagnants.negate();

        List<BonusJoueurRequest> bonusRequests = req.bonusJoueurs() == null ? List.of() : req.bonusJoueurs();
        Map<UUID, BigDecimal> bonusByJoueur = new HashMap<>();
        Set<String> uniqueBonus = new HashSet<>();
        List<BonusPartie> bonusParties = new ArrayList<>();

        for (BonusJoueurRequest bonusReq : bonusRequests) {
            SessionParticipant sp = sessionParticipantByJoueurId.get(bonusReq.joueurId());
            if (sp == null) {
                throw new IllegalArgumentException("Un bonus est attribue a un joueur hors session.");
            }
            String key = bonusReq.joueurId() + "|" + bonusReq.typeBonus().name();
            if (!uniqueBonus.add(key)) {
                throw new IllegalArgumentException("Bonus duplique pour un meme joueur.");
            }

            bonusByJoueur.merge(bonusReq.joueurId(), BONUS_POINTS_DECIMAL, BigDecimal::add);
            BonusPartie bonusPartie = new BonusPartie();
            bonusPartie.setSessionParticipant(sp);
            bonusPartie.setTypeBonus(bonusReq.typeBonus().name());
            bonusPartie.setPoints(BONUS_POINTS);
            bonusParties.add(bonusPartie);
        }

        Integer maxNumero = partieRepository.findMaxNumeroBySessionId(sessionId);
        Partie partie = new Partie();
        partie.setSession(session);
        partie.setNumero(maxNumero + 1);
        partie.setBoutsGagnants(req.boutsGagnants());
        partie.setPointsFaitsGagnants(pointsFaitsGagnants);
        partie.setPointsAFaire(pointsAFaire);
        partie.setPointsDeBase(pointsDeBase);
        partie.setTypePreneur(req.preneurType().name());
        partie.setMultiplicateurPreneur(multiplicateurPreneur);
        partie.setPointsContrat(pointsContrat);
        partieRepository.save(partie);

        List<ScorePartie> scores = new ArrayList<>();
        for (SessionParticipant participant : participants) {
            UUID joueurId = participant.getJoueur().getId();
            BigDecimal points = winners.contains(joueurId) ? pointsGagnants : pointsPerdants;
            points = points.add(bonusByJoueur.getOrDefault(joueurId, BigDecimal.ZERO));

            ScorePartie score = new ScorePartie();
            score.setPartie(partie);
            score.setSessionParticipant(participant);
            score.setGagnant(winners.contains(joueurId));
            score.setPoints(points);
            scores.add(score);
        }
        scorePartieRepository.saveAll(scores);
        for (BonusPartie bonusPartie : bonusParties) {
            bonusPartie.setPartie(partie);
        }
        if (!bonusParties.isEmpty()) {
            bonusPartieRepository.saveAll(bonusParties);
        }

        return mapPartie(partie, scores, bonusParties);
    }

    @Transactional(readOnly = true)
    public List<PartieResponse> listParties(UUID utilisateurId, UUID sessionId) {
        sessionTarotRepository.findByIdAndUtilisateurId(sessionId, utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable."));
        return buildParties(sessionId);
    }

    @Transactional
    public void deleteSession(UUID utilisateurId, UUID sessionId) {
        SessionTarot session = sessionTarotRepository.findByIdAndUtilisateurId(sessionId, utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable."));
        sessionTarotRepository.delete(session);
    }

    @Transactional
    public void deletePartie(UUID utilisateurId, UUID sessionId, UUID partieId) {
        sessionTarotRepository.findByIdAndUtilisateurId(sessionId, utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable."));

        Partie partie = partieRepository.findByIdAndSessionId(partieId, sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Partie introuvable."));
        partieRepository.delete(partie);
    }

    @Transactional(readOnly = true)
    public FeuilleSessionResponse getFeuilleSession(UUID utilisateurId, UUID sessionId) {
        SessionTarot session = sessionTarotRepository.findByIdAndUtilisateurId(sessionId, utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Session introuvable."));

        List<SessionParticipant> participants = sessionParticipantRepository.findBySessionIdOrderByJoueurNom(sessionId);
        List<JoueurResponse> participantDtos = participants.stream()
                .map(sp -> new JoueurResponse(sp.getJoueur().getId(), sp.getJoueur().getNom()))
                .toList();

        List<Partie> parties = partieRepository.findBySessionIdOrderByNumeroAsc(sessionId);
        if (parties.isEmpty()) {
            return new FeuilleSessionResponse(
                    session.getId(),
                    session.getNom(),
                    session.getDateSession(),
                    participantDtos,
                    List.of()
            );
        }

        List<UUID> partieIds = parties.stream().map(Partie::getId).toList();
        Map<UUID, List<ScorePartie>> scoresByPartieId = scorePartieRepository.findByPartieIdIn(partieIds).stream()
                .collect(Collectors.groupingBy(s -> s.getPartie().getId()));

        Map<UUID, BigDecimal> cumulByJoueur = new LinkedHashMap<>();
        for (JoueurResponse participant : participantDtos) {
            cumulByJoueur.put(participant.id(), BigDecimal.ZERO);
        }

        List<FeuillePartieLigneResponse> lignes = new ArrayList<>();
        for (Partie partie : parties) {
            List<ScorePartie> scores = scoresByPartieId.getOrDefault(partie.getId(), List.of());
            List<PartieScoreResponse> scoreDtos = orderedScoreDtos(participantDtos, scores);

            for (PartieScoreResponse score : scoreDtos) {
                cumulByJoueur.merge(score.joueurId(), score.points(), BigDecimal::add);
            }

            List<ParticipantTotalResponse> cumuls = participantDtos.stream()
                    .map(p -> new ParticipantTotalResponse(
                            p.id(),
                            p.nom(),
                            cumulByJoueur.getOrDefault(p.id(), BigDecimal.ZERO)
                    ))
                    .toList();

            lignes.add(new FeuillePartieLigneResponse(
                    partie.getId(),
                    partie.getNumero(),
                    partie.getJoueLe(),
                    scoreDtos,
                    cumuls
            ));
        }

        return new FeuilleSessionResponse(
                session.getId(),
                session.getNom(),
                session.getDateSession(),
                participantDtos,
                lignes
        );
    }

    @Transactional(readOnly = true)
    public List<GroupeBilanResponse> bilanGroupes(UUID utilisateurId) {
        List<SessionTarot> sessions = sessionTarotRepository.findByUtilisateurIdOrderByDateSessionDescCreeLeDesc(utilisateurId);
        Map<String, GroupAggregate> groups = new LinkedHashMap<>();

        for (SessionTarot session : sessions) {
            List<SessionParticipant> participants = sessionParticipantRepository.findBySessionIdOrderByJoueurNom(session.getId());
            if (participants.isEmpty()) {
                continue;
            }

            String groupKey = participants.stream()
                    .map(sp -> sp.getJoueur().getId().toString())
                    .sorted()
                    .collect(Collectors.joining("|"));

            GroupAggregate group = groups.computeIfAbsent(groupKey, key -> {
                List<JoueurResponse> groupParticipants = participants.stream()
                        .map(sp -> new JoueurResponse(sp.getJoueur().getId(), sp.getJoueur().getNom()))
                        .toList();
                Map<UUID, BigDecimal> cumulByJoueur = new HashMap<>();
                for (SessionParticipant p : participants) {
                    cumulByJoueur.put(p.getJoueur().getId(), BigDecimal.ZERO);
                }
                return new GroupAggregate(groupParticipants, new ArrayList<>(), cumulByJoueur);
            });

            group.sessions().add(new BilanSessionRefResponse(session.getId(), session.getNom(), session.getDateSession()));

            Map<UUID, BigDecimal> sessionTotals = computeSessionTotals(session.getId(), participants);
            for (Map.Entry<UUID, BigDecimal> entry : sessionTotals.entrySet()) {
                group.cumulByJoueur().merge(entry.getKey(), entry.getValue(), BigDecimal::add);
            }
        }

        return groups.entrySet().stream()
                .map(entry -> {
                    GroupAggregate agg = entry.getValue();
                    List<ParticipantTotalResponse> cumuls = agg.participants().stream()
                            .map(p -> new ParticipantTotalResponse(
                                    p.id(),
                                    p.nom(),
                                    agg.cumulByJoueur().getOrDefault(p.id(), BigDecimal.ZERO)
                            ))
                            .toList();
                    return new GroupeBilanResponse(
                            entry.getKey(),
                            agg.participants(),
                            agg.sessions().size(),
                            agg.sessions(),
                            cumuls
                    );
                })
                .toList();
    }

    private SessionSummaryResponse buildSessionSummary(SessionTarot session) {
        List<SessionParticipant> participants = sessionParticipantRepository.findBySessionIdOrderByJoueurNom(session.getId());
        List<JoueurResponse> participantDtos = participants.stream()
                .map(sp -> new JoueurResponse(sp.getJoueur().getId(), sp.getJoueur().getNom()))
                .toList();

        Map<UUID, BigDecimal> totals = computeSessionTotals(session.getId(), participants);
        List<ParticipantTotalResponse> totalDtos = participantDtos.stream()
                .map(p -> new ParticipantTotalResponse(p.id(), p.nom(), totals.getOrDefault(p.id(), BigDecimal.ZERO)))
                .toList();

        return new SessionSummaryResponse(
                session.getId(),
                session.getNom(),
                session.getDateSession(),
                participantDtos,
                totalDtos
        );
    }

    private SessionDetailResponse buildSessionDetail(SessionTarot session) {
        List<SessionParticipant> participants = sessionParticipantRepository.findBySessionIdOrderByJoueurNom(session.getId());
        List<JoueurResponse> participantDtos = participants.stream()
                .map(sp -> new JoueurResponse(sp.getJoueur().getId(), sp.getJoueur().getNom()))
                .toList();
        List<PartieResponse> parties = buildParties(session.getId());

        Map<UUID, BigDecimal> totals = computeSessionTotals(session.getId(), participants);
        List<ParticipantTotalResponse> totalDtos = participantDtos.stream()
                .map(p -> new ParticipantTotalResponse(p.id(), p.nom(), totals.getOrDefault(p.id(), BigDecimal.ZERO)))
                .toList();

        return new SessionDetailResponse(
                session.getId(),
                session.getNom(),
                session.getDateSession(),
                participantDtos,
                parties,
                totalDtos
        );
    }

    private List<PartieResponse> buildParties(UUID sessionId) {
        List<Partie> parties = partieRepository.findBySessionIdOrderByNumeroAsc(sessionId);
        if (parties.isEmpty()) {
            return List.of();
        }

        List<UUID> partieIds = parties.stream().map(Partie::getId).toList();
        Map<UUID, List<ScorePartie>> scoresByPartieId = scorePartieRepository.findByPartieIdIn(partieIds).stream()
                .collect(Collectors.groupingBy(s -> s.getPartie().getId()));
        Map<UUID, List<BonusPartie>> bonusByPartieId = bonusPartieRepository.findByPartieIdIn(partieIds).stream()
                .collect(Collectors.groupingBy(b -> b.getPartie().getId()));

        List<PartieResponse> responses = new ArrayList<>();
        for (Partie partie : parties) {
            List<ScorePartie> scores = scoresByPartieId.getOrDefault(partie.getId(), List.of());
            List<BonusPartie> bonus = bonusByPartieId.getOrDefault(partie.getId(), List.of());
            responses.add(mapPartie(partie, scores, bonus));
        }
        return responses;
    }

    private PartieResponse mapPartie(Partie partie, List<ScorePartie> scores, List<BonusPartie> bonus) {
        List<PartieScoreResponse> scoreDtos = scores.stream()
                .sorted(Comparator.comparing(s -> s.getSessionParticipant().getJoueur().getNom(), String.CASE_INSENSITIVE_ORDER))
                .map(s -> new PartieScoreResponse(
                        s.getSessionParticipant().getJoueur().getId(),
                        s.getSessionParticipant().getJoueur().getNom(),
                        Boolean.TRUE.equals(s.getGagnant()),
                        s.getPoints()
                ))
                .toList();

        List<BonusPartieResponse> bonusDtos = bonus.stream()
                .sorted(Comparator.comparing(b -> b.getSessionParticipant().getJoueur().getNom(), String.CASE_INSENSITIVE_ORDER))
                .map(b -> new BonusPartieResponse(
                        b.getSessionParticipant().getJoueur().getId(),
                        b.getSessionParticipant().getJoueur().getNom(),
                        BonusType.valueOf(b.getTypeBonus()),
                        b.getPoints()
                ))
                .toList();

        return new PartieResponse(
                partie.getId(),
                partie.getNumero(),
                partie.getJoueLe(),
                partie.getTypePreneur() == null ? null : PreneurType.valueOf(partie.getTypePreneur()),
                partie.getMultiplicateurPreneur(),
                partie.getBoutsGagnants(),
                partie.getPointsFaitsGagnants(),
                partie.getPointsAFaire(),
                partie.getPointsDeBase(),
                partie.getPointsContrat(),
                bonusDtos,
                scoreDtos
        );
    }

    private Map<UUID, BigDecimal> computeSessionTotals(UUID sessionId, List<SessionParticipant> participants) {
        Map<UUID, BigDecimal> totals = new HashMap<>();
        for (SessionParticipant participant : participants) {
            totals.put(participant.getJoueur().getId(), BigDecimal.ZERO);
        }

        List<Partie> parties = partieRepository.findBySessionIdOrderByNumeroAsc(sessionId);
        if (parties.isEmpty()) {
            return totals;
        }

        List<UUID> partieIds = parties.stream().map(Partie::getId).toList();
        List<ScorePartie> scores = scorePartieRepository.findByPartieIdIn(partieIds);
        for (ScorePartie score : scores) {
            UUID joueurId = score.getSessionParticipant().getJoueur().getId();
            totals.merge(joueurId, score.getPoints(), BigDecimal::add);
        }
        return totals;
    }

    private String normalize(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Valeur invalide.");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private Integer pointsAFaireByBouts(Integer bouts) {
        return switch (bouts) {
            case 0 -> 56;
            case 1 -> 51;
            case 2 -> 41;
            case 3 -> 36;
            default -> throw new IllegalArgumentException("Le nombre de bouts doit etre entre 0 et 3.");
        };
    }

    private int resolvePointsFaitsGagnants(CreatePartieRequest req) {
        Integer pointsGagnants = req.pointsFaitsGagnants();
        Integer pointsPerdants = req.pointsFaitsPerdants();
        if (pointsGagnants == null && pointsPerdants == null) {
            throw new IllegalArgumentException("Renseignez pointsFaitsGagnants ou pointsFaitsPerdants.");
        }
        if (pointsGagnants != null && pointsPerdants != null) {
            throw new IllegalArgumentException("Renseignez un seul champ: pointsFaitsGagnants ou pointsFaitsPerdants.");
        }
        return pointsGagnants != null ? pointsGagnants : TOTAL_TAS_POINTS - pointsPerdants;
    }

    private BigDecimal multiplicateurByPreneurType(PreneurType type) {
        return switch (type) {
            case PETITE -> BigDecimal.valueOf(1);
            case POUCE -> BigDecimal.valueOf(1.5);
            case GARDE -> BigDecimal.valueOf(2);
            case GARDE_SANS -> BigDecimal.valueOf(4);
        };
    }

    private List<PartieScoreResponse> orderedScoreDtos(List<JoueurResponse> participants,
                                                       List<ScorePartie> scores) {
        Map<UUID, ScorePartie> byJoueurId = scores.stream()
                .collect(Collectors.toMap(s -> s.getSessionParticipant().getJoueur().getId(), s -> s));
        List<PartieScoreResponse> ordered = new ArrayList<>();
        for (JoueurResponse p : participants) {
            ScorePartie score = byJoueurId.get(p.id());
            if (score == null) {
                ordered.add(new PartieScoreResponse(p.id(), p.nom(), false, BigDecimal.ZERO));
                continue;
            }
            ordered.add(new PartieScoreResponse(
                    p.id(),
                    p.nom(),
                    Boolean.TRUE.equals(score.getGagnant()),
                    score.getPoints()
            ));
        }
        return ordered;
    }

    private record GroupAggregate(
            List<JoueurResponse> participants,
            List<BilanSessionRefResponse> sessions,
            Map<UUID, BigDecimal> cumulByJoueur
    ) {}
}
