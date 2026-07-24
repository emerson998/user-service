package com.emerson.dev.usuarios.application.dto.saldo;

import java.math.BigDecimal;

public record TransferenciaResponse(Long pagadorId, BigDecimal saldoPagador, Long recebedorId,
        BigDecimal saldoRecebedor) {
}
