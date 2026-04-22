package com.taskengine.backend;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import com.taskengine.backend.dto.AuthResponse;
import com.taskengine.backend.entity.User;
import com.taskengine.backend.repository.UserRepository;
import com.taskengine.backend.service.AuthService;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthServiceOauthLoginTest {

  @Autowired private AuthService authService;
  @Autowired private UserRepository userRepository;

  @Test
  void oauthLogin_createsDistinctUsersByGoogleSub() {
    AuthResponse a =
        authService.oauthLogin(
            "sub-A", "alice@amazon.com", "Alice A", "https://example.com/a.png");
    AuthResponse b =
        authService.oauthLogin(
            "sub-B", "bob@amazon.com", "Bob B", "https://example.com/b.png");

    assertThat(a.getAccessToken()).isNotBlank();
    assertThat(b.getAccessToken()).isNotBlank();

    User ua = userRepository.findByGoogleSubWithOrganization("sub-A").orElseThrow();
    User ub = userRepository.findByGoogleSubWithOrganization("sub-B").orElseThrow();

    assertThat(ua.getId()).isNotEqualTo(ub.getId());
    assertThat(ua.getEmail()).isEqualTo("alice@amazon.com");
    assertThat(ub.getEmail()).isEqualTo("bob@amazon.com");
  }

  @Test
  void oauthLogin_sameGoogleSub_returnsSameUser() {
    authService.oauthLogin("sub-X", "x@google.com", "X", null);
    authService.oauthLogin("sub-X", "x2@google.com", "X2", null);
    User u = userRepository.findByGoogleSubWithOrganization("sub-X").orElseThrow();
    assertThat(u.getEmail()).isEqualTo("x@google.com");
  }
}

