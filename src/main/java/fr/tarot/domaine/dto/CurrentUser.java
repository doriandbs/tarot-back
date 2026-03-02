package fr.tarot.domaine.dto;


import java.util.UUID;

public record CurrentUser(UUID id, String email) {}