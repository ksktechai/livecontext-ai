package com.ai.livecontext.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.r2dbc.spi.ConnectionFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

class DatabaseConfigTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(DatabaseConfig.class);

  @Test
  void initializerBean_ShouldBeCreated_WhenPropertyIsAlways() {
    // Create a "safe" mock that won't crash the InitializingBean logic
    ConnectionFactory safeMock = mock(ConnectionFactory.class);
    when(safeMock.create()).thenReturn(Mono.empty());

    contextRunner
        .withPropertyValues("spring.sql.init.mode=always")
        .withBean(ConnectionFactory.class, () -> safeMock)
        .run(
            context -> {
              // Check if bean exists. If it failed to start, the failure is accessible via context
              assertThat(context).hasNotFailed();
              assertThat(context).hasBean("initializer");
              assertThat(context).hasSingleBean(ConnectionFactoryInitializer.class);
            });
  }

  @Test
  void initializerBean_ShouldNotBeCreated_WhenPropertyIsNever() {
    contextRunner
        .withPropertyValues("spring.sql.init.mode=never")
        .withBean(ConnectionFactory.class, () -> mock(ConnectionFactory.class))
        .run(
            context -> {
              assertThat(context).doesNotHaveBean(ConnectionFactoryInitializer.class);
            });
  }

  @Test
  void initializerBean_ShouldNotBeCreated_WhenPropertyIsMissing() {
    contextRunner
        .withBean(ConnectionFactory.class, () -> mock(ConnectionFactory.class))
        .run(
            context -> {
              assertThat(context).doesNotHaveBean(ConnectionFactoryInitializer.class);
            });
  }

  @Test
  void initializerMethod_ShouldReturnCorrectConfiguration_WhenCalledManually() {
    DatabaseConfig config = new DatabaseConfig();
    ConnectionFactory mockFactory = mock(ConnectionFactory.class);

    // Manual call avoids the Spring LifeCycle / InitializingBean crash
    ConnectionFactoryInitializer initializer = config.initializer(mockFactory);

    assertNotNull(initializer);

    Object actualFactory = ReflectionTestUtils.getField(initializer, "connectionFactory");
    assertSame(mockFactory, actualFactory, "The ConnectionFactory should match the mock");

    ResourceDatabasePopulator populator =
        (ResourceDatabasePopulator) ReflectionTestUtils.getField(initializer, "databasePopulator");
    assertNotNull(populator, "ResourceDatabasePopulator should be configured");

    Boolean continueOnError = (Boolean) ReflectionTestUtils.getField(populator, "continueOnError");
    assertFalse(continueOnError != null && continueOnError, "Should not continue on error");

    @SuppressWarnings("unchecked")
    List<Object> scripts = (List<Object>) ReflectionTestUtils.getField(populator, "scripts");
    assertNotNull(scripts);
    assertEquals(1, scripts.size());
    assertThat(scripts.get(0)).isInstanceOf(ClassPathResource.class);
    assertEquals("schema.sql", ((ClassPathResource) scripts.get(0)).getPath());
  }
}
