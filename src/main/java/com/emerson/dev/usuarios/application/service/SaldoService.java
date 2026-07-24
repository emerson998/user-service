package com.emerson.dev.usuarios.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emerson.dev.usuarios.application.dto.saldo.TransferenciaRequest;
import com.emerson.dev.usuarios.application.dto.saldo.TransferenciaResponse;
import com.emerson.dev.usuarios.domain.exception.ResourceNotFoundException;
import com.emerson.dev.usuarios.domain.model.User;
import com.emerson.dev.usuarios.domain.repository.UserRepository;

@Service
public class SaldoService {

    private final UserRepository userRepository;

    public SaldoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public TransferenciaResponse transferir(TransferenciaRequest request) {
        User pagador = userRepository.findById(request.pagadorId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.pagadorId()));
        User recebedor = userRepository.findById(request.recebedorId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.recebedorId()));

        pagador.debitar(request.valor());
        recebedor.creditar(request.valor());

        User pagadorSalvo = userRepository.save(pagador);
        User recebedorSalvo = userRepository.save(recebedor);

        return new TransferenciaResponse(pagadorSalvo.getId(), pagadorSalvo.getSaldo(), recebedorSalvo.getId(),
                recebedorSalvo.getSaldo());
    }
}
