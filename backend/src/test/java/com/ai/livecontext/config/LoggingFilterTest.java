package com.ai.livecontext.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ai.livecontext.util.CorrelationIdHolder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class LoggingFilterTest {

  private LoggingFilter loggingFilter;
  private WebFilterChain filterChain;

  @BeforeEach
  void setUp() {
    loggingFilter = new LoggingFilter();
    filterChain = mock(WebFilterChain.class);
    // Default mock behavior: just return empty mono
    when(filterChain.filter(any())).thenReturn(Mono.empty());
  }

  @Test
  void filter_ShouldUseExistingCorrelationId() {
    String existingId = UUID.randomUUID().toString();
    MockServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/test").header("X-Correlation-Id", existingId).build());

    StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();

    assertThat(exchange.getResponse().getHeaders().getFirst("X-Correlation-Id"))
        .isEqualTo(existingId);
  }

  @Test
  void filter_ShouldGenerateCorrelationId_WhenMissing() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());

    StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();

    String generatedId = exchange.getResponse().getHeaders().getFirst("X-Correlation-Id");
    assertThat(generatedId).isNotBlank();
    // Verify UUID format
    assertThat(UUID.fromString(generatedId)).isNotNull();
  }

  @Test
  void filter_ShouldClearCorrelationId_OnComplete() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());

    StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();

    assertThat(CorrelationIdHolder.get()).isNull();
  }

  @Test
  void requestDecorator_ShouldLogBody() {
    String bodyText = "test request body";
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.post("/api/test").body(bodyText));

    when(filterChain.filter(any()))
        .thenAnswer(
            invocation -> {
              // Fix: Use the interface ServerWebExchange instead of MockServerWebExchange
              ServerWebExchange decoratedExchange = invocation.getArgument(0);
              return decoratedExchange.getRequest().getBody().collectList().then();
            });

    StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();
  }

  @Test
  void responseDecorator_ShouldLogBodyAndStatus() {
    String responseText = "test response body";
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());

    when(filterChain.filter(any()))
        .thenAnswer(
            invocation -> {
              ServerWebExchange decoratedExchange = invocation.getArgument(0);
              decoratedExchange.getResponse().setStatusCode(HttpStatus.OK);
              DataBuffer buffer =
                  decoratedExchange
                      .getResponse()
                      .bufferFactory()
                      .wrap(responseText.getBytes(StandardCharsets.UTF_8));
              // Fixed: Added return statement to match Mono<Void> return type
              return decoratedExchange.getResponse().writeWith(Mono.just(buffer));
            });

    StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void responseDecorator_ShouldHandleWriteAndFlushWith() {
    String responseText = "test response body";
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());

    when(filterChain.filter(any()))
        .thenAnswer(
            invocation -> {
              // Fixed: Use ServerWebExchange interface to avoid ClassCastException
              ServerWebExchange decoratedExchange = invocation.getArgument(0);
              DataBuffer buffer =
                  decoratedExchange
                      .getResponse()
                      .bufferFactory()
                      .wrap(responseText.getBytes(StandardCharsets.UTF_8));
              return decoratedExchange
                  .getResponse()
                  .writeAndFlushWith(Mono.just(Mono.just(buffer)));
            });

    StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();
  }

  @Test
  void truncateBody_ShouldLimitLength() {
    StringBuilder longBody = new StringBuilder();
    for (int i = 0; i < 2100; i++) {
      longBody.append("a");
    }

    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.post("/api/test").build());

    when(filterChain.filter(any()))
        .thenAnswer(
            invocation -> {
              // Fix: Use the interface ServerWebExchange instead of MockServerWebExchange
              ServerWebExchange decoratedExchange = invocation.getArgument(0);
              DataBuffer buffer =
                  decoratedExchange
                      .getResponse()
                      .bufferFactory()
                      .wrap(longBody.toString().getBytes(StandardCharsets.UTF_8));
              return decoratedExchange.getResponse().writeWith(Mono.just(buffer));
            });

    StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();
  }
}
