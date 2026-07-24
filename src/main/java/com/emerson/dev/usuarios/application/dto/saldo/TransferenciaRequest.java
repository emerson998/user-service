package com.emerson.dev.usuarios.application.dto.saldo;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record TransferenciaRequest(
        @NotNull Long pagadorId,
        @NotNull Long recebedorId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor) {
}
