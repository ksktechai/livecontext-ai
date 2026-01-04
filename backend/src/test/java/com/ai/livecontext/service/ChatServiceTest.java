package com.ai.livecontext.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.ai.livecontext.domain.ChatRequest;
import com.ai.livecontext.domain.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @Mock private LlmService llmService;

  private ChatService chatService;

  @BeforeEach
  void setUp() {
    chatService = new ChatService(llmService);
  }

  @Test
  void processChat_shouldDelegateToLlmService() {
    ChatRequest request = ChatRequest.builder().question("What is the weather?").build();

    ChatResponse expectedResponse = ChatResponse.builder().answer("The weather is sunny.").build();

    when(llmService.chat(anyString())).thenReturn(Mono.just(expectedResponse));

    StepVerifier.create(chatService.processChat(request))
        .expectNext(expectedResponse)
        .verifyComplete();

    verify(llmService).chat("What is the weather?");
  }

  @Test
  void processChat_shouldPropagateError() {
    ChatRequest request = ChatRequest.builder().question("Test question").build();

    RuntimeException error = new RuntimeException("LLM service error");
    when(llmService.chat(anyString())).thenReturn(Mono.error(error));

    StepVerifier.create(chatService.processChat(request))
        .expectErrorMatches(e -> e.getMessage().equals("LLM service error"))
        .verify();
  }
}
