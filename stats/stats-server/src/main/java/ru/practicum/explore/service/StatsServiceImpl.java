package ru.practicum.explore.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.explore.useDto.dto.HitDto;
import ru.practicum.explore.useDto.dto.StatsDto;
import ru.practicum.explore.model.ModelHit;
import ru.practicum.explore.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.explore.model.ModelMapper.toModelHit;

@Service
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

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
    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<StatsDto> hits;
        if (unique) {
            hits = getUniqueHits(start, end, uris);
        } else {
            hits = getAllHits(start, end, uris);
        }
        return hits;
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