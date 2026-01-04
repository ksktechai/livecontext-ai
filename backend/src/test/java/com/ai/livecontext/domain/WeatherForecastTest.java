package com.ai.livecontext.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class WeatherForecastTest {

  @Test
  void builder_shouldCreateWeatherForecast() {
    Instant now = Instant.now();
    List<WeatherForecast.HourlyForecast> hourly =
        Arrays.asList(
            WeatherForecast.HourlyForecast.builder()
                .time(now)
                .temperature(25.5)
                .precipitation(0.0)
                .build());

    WeatherForecast forecast =
        WeatherForecast.builder()
            .latitude(40.7128)
            .longitude(-74.0060)
            .location("New York")
            .timestamp(now)
            .hourly(hourly)
            .build();

    assertEquals(40.7128, forecast.getLatitude());
    assertEquals(-74.0060, forecast.getLongitude());
    assertEquals("New York", forecast.getLocation());
    assertEquals(now, forecast.getTimestamp());
    assertEquals(1, forecast.getHourly().size());
  }

  @Test
  void hourlyForecast_shouldBuildCorrectly() {
    Instant time = Instant.now();
    WeatherForecast.HourlyForecast hourly =
        WeatherForecast.HourlyForecast.builder()
            .time(time)
            .temperature(20.0)
            .precipitation(5.5)
            .build();

    assertEquals(time, hourly.getTime());
    assertEquals(20.0, hourly.getTemperature());
    assertEquals(5.5, hourly.getPrecipitation());
  }

  @Test
  void setters_shouldModifyFields() {
    WeatherForecast forecast = new WeatherForecast();
    forecast.setLatitude(51.5074);
    forecast.setLongitude(-0.1278);
    forecast.setLocation("London");

    assertEquals(51.5074, forecast.getLatitude());
    assertEquals(-0.1278, forecast.getLongitude());
    assertEquals("London", forecast.getLocation());
  }

  @Test
  void noArgsConstructor_shouldWork() {
    WeatherForecast forecast = new WeatherForecast();
    assertNull(forecast.getTimestamp());
    assertNull(forecast.getLocation());
  }
}
