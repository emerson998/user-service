package com.emerson.dev.usuarios.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Identidade do PSP simulado (usuarios-service + pix-service juntos formam um
 * unico PSP nesta fase). Consultado por quem precisar saber o ISPB/tipo do
 * participante -- ver PLANO_EVOLUCAO_PIX_SIMULACAO.md, tarefa 1.1.3.
 */
@RestController
@RequestMapping("/internal/psp")
@Tag(name = "PSP", description = "Identidade do PSP simulado (ISPB/tipo)")
public class PspInfoController {

    private final PspInfoResponse info;

    public PspInfoController(
            @Value("${app.psp.ispb}") String ispb,
            @Value("${app.psp.participant-id}") String participantId,
            @Value("${app.psp.type}") String type) {
        this.info = new PspInfoResponse(ispb, participantId, type);
    }

    @GetMapping("/info")
    @Operation(summary = "Identidade do PSP simulado (ISPB, participantId, tipo direto/indireto)")
    public PspInfoResponse info() {
        return info;
    }

    public record PspInfoResponse(String ispb, String participantId, String type) {
    }
}
