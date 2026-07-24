package com.emerson.dev.usuarios.infrastructure.chaos;

import java.util.List;

import org.springframework.stereotype.Service;

import com.emerson.dev.usuarios.domain.model.User;
import com.emerson.dev.usuarios.domain.repository.UserRepository;

@Service
public class ChaosUserCorruptionService {

    private final UserRepository userRepository;
    private final ChaosState chaosState;

    public ChaosUserCorruptionService(UserRepository userRepository, ChaosState chaosState) {
        this.userRepository = userRepository;
        this.chaosState = chaosState;
    }

    public Long insertCorruptedUser() {
        // Construção direta do domínio, sem passar por UserRequest/@NotBlank —
        // é exatamente esse desvio da validação normal que simula o dado corrompido.
        User corrupted = new User(null, null, "chaos+" + System.nanoTime() + "@demo.io", "N/A", null, null, true,
                User.SALDO_INICIAL);
        Long id = userRepository.save(corrupted).getId();
        chaosState.recordTrigger(id);
        return id;
    }

    /**
     * Mitigação em runtime: remove os usuários sem "name" que o chaos inseriu,
     * liberando o GET imediatamente. Representa o hotfix aplicado enquanto o
     * PR com a correção de código segue pendente de aprovação.
     */
    public List<Long> mitigate() {
        List<Long> removedIds = userRepository.findAll().stream()
                .filter(u -> u.getName() == null)
                .map(User::getId)
                .toList();
        removedIds.forEach(userRepository::deleteById);
        chaosState.recordResolution();
        return removedIds;
    }

    /** Reset completo pra reiniciar a demo: apaga todos os usuários e zera os contadores. */
    public List<Long> resetAll() {
        List<Long> removedIds = userRepository.findAll().stream().map(User::getId).toList();
        removedIds.forEach(userRepository::deleteById);
        chaosState.reset();
        return removedIds;
    }
}
