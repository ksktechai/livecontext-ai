package com.ai.livecontext.config;

import com.ai.livecontext.util.CorrelationIdHolder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter implements WebFilter {

  private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
  private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    ServerHttpResponse response = exchange.getResponse();

    String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
    }

    CorrelationIdHolder.set(correlationId);

    response.getHeaders().add(CORRELATION_ID_HEADER, correlationId);

    long startTime = System.currentTimeMillis();

    // Log request headers
    logger.info(
        "[http_request] Incoming HTTP request | method={} path={} query={} headers={} timestamp={}",
        request.getMethod().name(),
        request.getPath().value(),
        request.getURI().getQuery() != null ? request.getURI().getQuery() : "",
        request.getHeaders().toSingleValueMap().toString(),
        Instant.now().toString());

    // Create decorated exchange to capture request/response bodies
    ServerWebExchange decoratedExchange =
        new ServerWebExchangeDecorator(exchange) {
          @Override
          public ServerHttpRequest getRequest() {
            return new LoggingRequestDecorator(super.getRequest());
          }

          @Override
          public ServerHttpResponse getResponse() {
            return new LoggingResponseDecorator(super.getResponse(), startTime, request);
          }
        };

    return chain.filter(decoratedExchange).doFinally(signalType -> CorrelationIdHolder.clear());
  }

  private static class LoggingRequestDecorator extends ServerHttpRequestDecorator {

    public LoggingRequestDecorator(ServerHttpRequest delegate) {
      super(delegate);
    }

    @Override
    public Flux<DataBuffer> getBody() {
      return super.getBody()
          .doOnNext(
              dataBuffer -> {
                byte[] content = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(content);
                dataBuffer.readPosition(0); // Reset position for downstream consumers
                String body = new String(content, StandardCharsets.UTF_8);
                if (!body.isBlank()) {
                  logger.debug("[http_request_body] Request body | body={}", truncateBody(body));
                }
              });
    }
  }

  private static class LoggingResponseDecorator extends ServerHttpResponseDecorator {
    private final long startTime;
    private final ServerHttpRequest request;

    public LoggingResponseDecorator(
        ServerHttpResponse delegate, long startTime, ServerHttpRequest request) {
      super(delegate);
      this.startTime = startTime;
      this.request = request;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
      return DataBufferUtils.join(Flux.from(body))
          .flatMap(
              dataBuffer -> {
                byte[] content = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(content);
                String responseBody = new String(content, StandardCharsets.UTF_8);
                long duration = System.currentTimeMillis() - startTime;

                // Log response with body
                logger.info(
                    "[http_response] HTTP response sent | method={} path={} status={} duration_ms={} body={}",
                    request.getMethod().name(),
                    request.getPath().value(),
                    getStatusCode() != null ? getStatusCode().value() : 0,
                    duration,
                    truncateBody(responseBody));

                // Recreate the buffer for downstream
                DataBuffer buffer = bufferFactory().wrap(content);
                return super.writeWith(Mono.just(buffer));
              });
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
      return writeWith(Flux.from(body).flatMap(Flux::from));
    }
  }

  private static String truncateBody(String body) {
    int maxLength = 2000;
    if (body.length() > maxLength) {
      return body.substring(0, maxLength) + "... [TRUNCATED]";
    }
    return body;
  }
}
