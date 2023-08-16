package ru.practicum.explore.service;

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
        if(unique) {
            if(uris == null) {
                return statsRepository.findAllUniqueIp(start, end);
            } return statsRepository.findStatsByUrisWithUniqueIp(start, end, uris);
            } else {
            if(uris == null) {
                return statsRepository.findAll(start, end);
            }
            return statsRepository.findAllUris(start, end, uris);
        }
    }
}
