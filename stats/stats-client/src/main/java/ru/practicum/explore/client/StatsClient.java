package ru.practicum.explore.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.explore.useDto.dto.HitDto;
import ru.practicum.explore.useDto.dto.StatsDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StatsClient {

    @Value("${server.url}")
    private String url;

    private final RestTemplate restTemplate;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void saveStats(String app, String uri, String ip, LocalDateTime timestamp) {
        HitDto body = new HitDto(app, uri, ip, timestamp);
        restTemplate.postForEntity(url + "/hit", body, Void.class);
    }

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {

        if (uris != null && !uris.isEmpty()) {
            String urisString = String.join(",", uris);

            Map<String, Object> parameters = Map.of(
                    "start", encodeDateTime(start),
                    "end", encodeDateTime(end),
                    "uris", urisString,
                    "unique", unique);

            StatsDto[] response = restTemplate.getForObject(
                    url + "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                    StatsDto[].class,
                    parameters);

            return Objects.isNull(response) ? List.of() : List.of(response);
        }

        Map<String, Object> parameters = Map.of("start", encodeDateTime(start),
                "end", encodeDateTime(end),
                "unique", unique);

        StatsDto[] response = restTemplate.getForObject(
                url + "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                StatsDto[].class,
                parameters);

        return Objects.isNull(response) ? List.of() : List.of(response);

    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private String encodeDateTime(LocalDateTime time) {
        String timeString = time.format(formatter);
        return URLEncoder.encode(timeString, StandardCharsets.UTF_8);
    }

}
