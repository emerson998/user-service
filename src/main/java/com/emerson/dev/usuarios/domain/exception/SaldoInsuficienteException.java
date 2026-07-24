package com.emerson.dev.usuarios.domain.exception;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(Long userId, BigDecimal saldoAtual, BigDecimal valorSolicitado) {
        super("Usuario " + userId + " tem saldo insuficiente: saldo=" + saldoAtual + " valor=" + valorSolicitado);
    }
}
