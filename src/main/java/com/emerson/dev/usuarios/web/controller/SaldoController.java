package com.emerson.dev.usuarios.web.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emerson.dev.usuarios.application.dto.saldo.TransferenciaRequest;
import com.emerson.dev.usuarios.application.dto.saldo.TransferenciaResponse;
import com.emerson.dev.usuarios.application.service.SaldoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Transferencia de saldo entre usuarios, chamada pelo pix-service apos o SPI
 * confirmar a liquidacao (ver PixService.processar). Debito e credito
 * ocorrem na mesma transacao aqui -- pagador e recebedor vivem no mesmo
 * banco de usuarios-service, entao nao ha necessidade de saga/2PC nesta fase.
 */
@RestController
@RequestMapping("/internal/saldo")
@CrossOrigin(origins = { "http://localhost:5173", "http://127.0.0.1:5173" })
@Tag(name = "Saldo (interno)", description = "Endpoint interno usado pelo pix-service — nunca expor em producao")
public class SaldoController {

    private final SaldoService saldoService;

    public SaldoController(SaldoService saldoService) {
        this.saldoService = saldoService;
    }

    @PostMapping("/transferencia")
    @Operation(summary = "Debita o pagador e credita o recebedor de forma atomica")
    public TransferenciaResponse transferir(@Valid @RequestBody TransferenciaRequest request) {
        return saldoService.transferir(request);
    }
}
