# Task 08 — application.service.UserService

Status: [x] Concluída

Depende de: Task 01, 02, 03, 04, 05, 06, 07.

## Objetivo

Implementação do caso de uso, orquestra domínio + ports. Injeção só via
construtor (nunca `@Autowired` em campo).

## Arquivos

- criar: `src/main/java/com/emerson/dev/usuarios/application/service/UserService.java`

## Especificação

- `@Service`, `@Transactional` na classe, `@Transactional(readOnly = true)`
  em `getById`/`listAll`.
- Construtor: `UserRepository userRepository, PasswordEncoderPort passwordEncoderPort`.
- `create(UserRequest request)`:
  1. `userRepository.findByEmail(request.email())` presente →
     `throw new DuplicateResourceException(...)`.
  2. `passwordEncoderPort.encode(request.password())`.
  3. `UserMapper.toDomain(request, hash)`, `userRepository.save(...)`.
  4. `UserMapper.toResponse(saved)`.
- `update(Long id, UserUpdateRequest request)`:
  1. `findById` ou `throw new ResourceNotFoundException(...)`.
  2. `user.updateProfile(request.name(), request.phone(), request.bio())`.
  3. `save`, mapear resposta.
- `getById(Long id)`: `findById` ou `ResourceNotFoundException`, mapear.
- `listAll()`: `findAll()`, mapear cada um.
- `delete(Long id)`: `existsById` (senão `ResourceNotFoundException`),
  `deleteById`.

## Critérios de aceite

- [x] Nenhum `@Autowired` em campo — só construtor.
- [x] Regra de e-mail duplicado coberta em `create` (não se aplica a
      `update`, já que `UserUpdateRequest` não carrega e-mail — decisão
      da task 04).
- [ ] Coberto por `UserServiceTest` (task 16) — pendente, task 16 ainda
      não começou.

## Nota de implementação

`UserRepository` não expõe transações — `save`/`findById`/etc. dependem
de `UserRepositoryAdapter` (task 12) e `UserJpaRepository` (task 10)
existirem para rodar de verdade; até lá o service compila mas não tem
implementação de infraestrutura por trás.
