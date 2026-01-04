package com.ai.livecontext.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

  private String answer;

  private List<Evidence> evidence;

  private String correlationId;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Evidence {
    private String type;
    private String source;
    private String timestamp;
    private String summary;
  }
}
