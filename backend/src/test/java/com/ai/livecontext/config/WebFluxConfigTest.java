package com.ai.livecontext.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.config.CorsRegistration;
import org.springframework.web.reactive.config.CorsRegistry;

class WebFluxConfigTest {

  @Test
  void addCorsMappings_ShouldConfigureCorrectly() {
    // Arrange
    WebFluxConfig webFluxConfig = new WebFluxConfig();
    CorsRegistry registry = new CorsRegistry();

    // Act
    webFluxConfig.addCorsMappings(registry);

    // Assert
    // Fixed: registrations is a List in modern Spring WebFlux
    @SuppressWarnings("unchecked")
    java.util.List<CorsRegistration> registrations =
        (java.util.List<CorsRegistration>) ReflectionTestUtils.getField(registry, "registrations");

    assertThat(registrations).hasSize(1);

    CorsRegistration registration = registrations.get(0);

    // Verify the path pattern
    String pathPattern = (String) ReflectionTestUtils.getField(registration, "pathPattern");
    assertThat(pathPattern).isEqualTo("/**");

    CorsConfiguration config =
        (CorsConfiguration) ReflectionTestUtils.getField(registration, "config");

    assertThat(config.getAllowedOrigins())
        .containsExactlyInAnyOrder("http://localhost:5173", "http://localhost:3000");
    assertThat(config.getAllowedMethods())
        .containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "OPTIONS");
    assertThat(config.getAllowedHeaders()).containsExactly("*");
    assertThat(config.getExposedHeaders()).containsExactly("X-Correlation-Id");
    assertThat(config.getAllowCredentials()).isTrue();
  }
}
