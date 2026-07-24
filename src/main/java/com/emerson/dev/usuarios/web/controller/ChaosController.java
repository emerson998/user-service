package com.emerson.dev.usuarios.web.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emerson.dev.usuarios.infrastructure.chaos.ChaosState;
import com.emerson.dev.usuarios.infrastructure.chaos.ChaosUserCorruptionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/internal/chaos/users-flow")
@CrossOrigin(origins = { "http://localhost:5173", "http://127.0.0.1:5173" })
@Tag(name = "Chaos", description = "Endpoints de demonstração — nunca expor em produção")
public class ChaosController {

    private final ChaosUserCorruptionService chaosUserCorruptionService;
    private final ChaosState chaosState;

    public ChaosController(ChaosUserCorruptionService chaosUserCorruptionService, ChaosState chaosState) {
        this.chaosUserCorruptionService = chaosUserCorruptionService;
        this.chaosState = chaosState;
    }

    @PostMapping("/trigger")
    @Operation(summary = "Insere um usuário corrompido (name = null), quebrando os GET de /api/v1/users")
    public TriggerResponse trigger() {
        Long id = chaosUserCorruptionService.insertCorruptedUser();
        return new TriggerResponse(id, "Usuário " + id + " inserido sem 'name'. "
                + "Qualquer GET em /api/v1/users vai quebrar.");
    }

    @PostMapping("/mitigate")
    @Operation(summary = "Remove os usuários corrompidos (name = null), liberando o GET imediatamente")
    public MitigateResponse mitigate() {
        var removedIds = chaosUserCorruptionService.mitigate();
        return new MitigateResponse(removedIds, "Removidos " + removedIds.size() + " usuário(s) corrompido(s).");
    }

    @PostMapping("/reset")
    @Operation(summary = "Apaga todos os usuários e zera os contadores — reinicia a demo do zero")
    public MitigateResponse reset() {
        var removedIds = chaosUserCorruptionService.resetAll();
        return new MitigateResponse(removedIds, "Reset completo: " + removedIds.size() + " usuário(s) removido(s).");
    }

    @GetMapping("/status")
    @Operation(summary = "Estado atual do chaos")
    public StatusResponse status() {
        return new StatusResponse(chaosState.lastCorruptedUserId(), chaosState.lastTriggeredAt(),
                chaosState.triggerCount(), chaosState.lastResolvedAt(), chaosState.resolvedCount());
    }

    public record TriggerResponse(Long corruptedUserId, String message) {
    }

    public record MitigateResponse(java.util.List<Long> removedUserIds, String message) {
    }

    public record StatusResponse(Long lastCorruptedUserId, java.time.Instant lastTriggeredAt, int triggerCount,
            java.time.Instant lastResolvedAt, int resolvedCount) {
    }
}
