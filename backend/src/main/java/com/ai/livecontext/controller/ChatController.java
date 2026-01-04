package com.ai.livecontext.controller;

import com.ai.livecontext.domain.ChatRequest;
import com.ai.livecontext.domain.ChatResponse;
import com.ai.livecontext.service.ChatService;
import com.ai.livecontext.util.CorrelationIdHolder;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ChatController {

  private final ChatService chatService;

  public ChatController(ChatService chatService) {
    this.chatService = chatService;
  }

  @PostMapping("/chat")
  public Mono<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
    if (request.getCorrelationId() == null || request.getCorrelationId().isBlank()) {
      request.setCorrelationId(UUID.randomUUID().toString());
    }
    CorrelationIdHolder.set(request.getCorrelationId());

    return chatService.processChat(request).doFinally(signalType -> CorrelationIdHolder.clear());
  }
}
