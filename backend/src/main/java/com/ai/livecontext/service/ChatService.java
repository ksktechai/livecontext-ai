package com.ai.livecontext.service;

import com.ai.livecontext.domain.ChatRequest;
import com.ai.livecontext.domain.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ChatService {

  private final LlmService llmService;

  public ChatService(LlmService llmService) {
    this.llmService = llmService;
  }

  public Mono<ChatResponse> processChat(ChatRequest request) {
    return llmService.chat(request.getQuestion());
  }
}
