package com.ai.livecontext.domain;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherForecast {

  private double latitude;

  private double longitude;

  private String location;

  private Instant timestamp;

  private List<HourlyForecast> hourly;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class HourlyForecast {
    private Instant time;
    private double temperature;
    private double precipitation;
  }
}
