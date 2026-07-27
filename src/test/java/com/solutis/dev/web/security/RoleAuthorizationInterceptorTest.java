package com.solutis.dev.web.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import com.solutis.dev.application.port.in.AuthUseCase;
import com.solutis.dev.domain.exception.AccessDeniedException;
import com.solutis.dev.domain.exception.InvalidTokenException;
import com.solutis.dev.domain.exception.ResourceNotFoundException;
import com.solutis.dev.domain.model.Role;
import com.solutis.dev.domain.model.User;
import com.solutis.dev.domain.repository.UserRepository;

class RoleAuthorizationInterceptorTest {

    private static class TestController {

        @RequireRole(Role.ADMIN)
        public void adminOnly() {
        }

        public void unrestricted() {
        }
    }

    private final AuthUseCase authUseCase = mock(AuthUseCase.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleAuthorizationInterceptor interceptor =
            new RoleAuthorizationInterceptor(authUseCase, userRepository);

    private HandlerMethod handlerMethodFor(String methodName) throws NoSuchMethodException {
        return new HandlerMethod(new TestController(), TestController.class.getMethod(methodName));
    }

    private User user(Long id, Role role) {
        return new User(id, "Name", "email@example.com", "12345678909", "hashed",
                null, null, true, null, false, role);
    }

    @Test
    void preHandle_shouldReturnTrue_whenHandlerIsNotAHandlerMethod() {
        boolean result = interceptor.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_shouldReturnTrue_whenMethodHasNoRequireRoleAnnotation() throws NoSuchMethodException {
        boolean result = interceptor.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(), handlerMethodFor("unrestricted"));

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_shouldReturnTrue_whenTokenBelongsToRequiredRole() throws NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer admin-token");
        when(authUseCase.requireValidToken("admin-token")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, Role.ADMIN)));

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), handlerMethodFor("adminOnly"));

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_shouldThrowAccessDeniedException_whenTokenBelongsToDifferentRole() throws NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer user-token");
        when(authUseCase.requireValidToken("user-token")).thenReturn(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, Role.USER)));

        assertThatThrownBy(() -> interceptor.preHandle(
                request, new MockHttpServletResponse(), handlerMethodFor("adminOnly")))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void preHandle_shouldThrowInvalidTokenException_whenAuthorizationHeaderIsMissing() throws NoSuchMethodException {
        assertThatThrownBy(() -> interceptor.preHandle(
                new MockHttpServletRequest(), new MockHttpServletResponse(), handlerMethodFor("adminOnly")))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void preHandle_shouldThrowInvalidTokenException_whenAuthorizationHeaderIsNotBearer() throws NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");

        assertThatThrownBy(() -> interceptor.preHandle(
                request, new MockHttpServletResponse(), handlerMethodFor("adminOnly")))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void preHandle_shouldThrowResourceNotFoundException_whenUserNoLongerExists() throws NoSuchMethodException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer stale-token");
        when(authUseCase.requireValidToken("stale-token")).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interceptor.preHandle(
                request, new MockHttpServletResponse(), handlerMethodFor("adminOnly")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
