package ru.practicum.explore.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.explore.model.ModelHit;
import ru.practicum.explore.repository.StatsRepository;
import ru.practicum.explore.useDto.dto.HitDto;
import ru.practicum.explore.useDto.dto.StatsDto;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.practicum.explore.model.ModelMapper.toModelHit;

@Service
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public StatsServiceImpl(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }


    @Override
    public ModelHit saveHit(HitDto hitDto) {
        ModelHit newModelHit = toModelHit(hitDto);
        return statsRepository.save(newModelHit);
    }

    @Override
    public List<StatsDto> getStats(String start, String end, List<String> uris, boolean unique) {

        try {
            LocalDateTime startTime = LocalDateTime.parse(
                    java.net.URLDecoder.decode(start, StandardCharsets.UTF_8.name()),
                    dateTimeFormatter);
            LocalDateTime endTime = LocalDateTime.parse(
                    java.net.URLDecoder.decode(end, StandardCharsets.UTF_8.name()), dateTimeFormatter);

            if (startTime.isAfter(endTime)) {
                log.warn("Дата начала {} должна быть ранее даты окончания {}.", startTime, endTime);
                throw new IllegalArgumentException("Дата начала {} должна быть ранее даты окончания {}."
                        + startTime + endTime);
            }

            List<StatsDto> hits;
            if (unique) {
                hits = getUniqueHits(startTime, endTime, uris);
            } else {
                hits = getAllHits(startTime, endTime, uris);
            }
            return hits;
        } catch (UnsupportedEncodingException e) {
            log.error(e.getLocalizedMessage());
            return List.of();
        }
    }


    private List<StatsDto> getUniqueHits(LocalDateTime start, LocalDateTime end, List<String> uris) {
        List<StatsDto> hits;
        if (uris == null) {
            log.info("Uris is null");
            hits = statsRepository.findAllUniqueIp(start, end);
            ;
        } else {
            log.info("Uris : {}", uris);
            hits = statsRepository.findStatsByUrisByUniqueIp(start, end, uris);
        }
        return hits;
    }

    private List<StatsDto> getAllHits(LocalDateTime start, LocalDateTime end, List<String> uris) {
        List<StatsDto> hits;
        if (uris == null) {
            log.info("Uris is null");
            hits = statsRepository.findAll(start, end);
        } else {
            log.info("Uris: {}", uris);
            hits = statsRepository.findAllByUris(start, end, uris);
        }
        return hits;
    }

}
