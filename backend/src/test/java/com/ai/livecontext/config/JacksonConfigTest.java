package com.ai.livecontext.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class JacksonConfigTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(JacksonConfig.class);

  @Test
  void objectMapper_ShouldBeConfiguredCorrectly() {
    contextRunner.run(
        context -> {
          assertThat(context).hasSingleBean(ObjectMapper.class);
          ObjectMapper mapper = context.getBean(ObjectMapper.class);

          // Verify JavaTimeModule is registered
          // The module is typically identified by its ID "jackson-datatype-jsr310"
          assertTrue(
              mapper.getRegisteredModuleIds().contains("jackson-datatype-jsr310"),
              "JavaTimeModule should be registered");

          // Verify WRITE_DATES_AS_TIMESTAMPS is disabled
          assertFalse(
              mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS),
              "WRITE_DATES_AS_TIMESTAMPS should be disabled");
        });
  }

  @Test
  void objectMapperMethod_ShouldReturnConfiguredInstance_WhenCalledManually() {
    // Direct unit test of the method for coverage of the class logic
    JacksonConfig config = new JacksonConfig();
    ObjectMapper mapper = config.objectMapper();

    assertThat(mapper).isNotNull();
    assertTrue(mapper.getRegisteredModuleIds().contains("jackson-datatype-jsr310"));
    assertFalse(mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
  }
}
