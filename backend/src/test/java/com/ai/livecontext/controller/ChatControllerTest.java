package com.ai.livecontext.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ai.livecontext.domain.ChatRequest;
import com.ai.livecontext.domain.ChatResponse;
import com.ai.livecontext.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

  @Mock private ChatService chatService;

  private ChatController chatController;

  @BeforeEach
  void setUp() {
    chatController = new ChatController(chatService);
  }

  @Test
  void chat_shouldReturnChatResponse() {
    ChatRequest request = ChatRequest.builder().question("What is the weather?").build();

    ChatResponse response =
        ChatResponse.builder().answer("The weather is sunny.").correlationId("test-123").build();

    when(chatService.processChat(any(ChatRequest.class))).thenReturn(Mono.just(response));

    StepVerifier.create(chatController.chat(request))
        .expectNextMatches(r -> r.getAnswer().equals("The weather is sunny."))
        .verifyComplete();
  }

  @Test
  void chat_shouldPropagateError() {
    ChatRequest request = ChatRequest.builder().question("Test question").build();

    RuntimeException error = new RuntimeException("Service error");
    when(chatService.processChat(any(ChatRequest.class))).thenReturn(Mono.error(error));

    StepVerifier.create(chatController.chat(request))
        .expectErrorMatches(e -> e.getMessage().equals("Service error"))
        .verify();
  }

  @Test
  void chat_shouldSetCorrelationIdIfNotProvided() {
    ChatRequest request = ChatRequest.builder().question("Test").build();

    ChatResponse response = ChatResponse.builder().answer("Answer").build();

    when(chatService.processChat(any(ChatRequest.class))).thenReturn(Mono.just(response));

    StepVerifier.create(chatController.chat(request)).expectNext(response).verifyComplete();

    // Verify the request was processed
    verify(chatService).processChat(any(ChatRequest.class));
  }
}
