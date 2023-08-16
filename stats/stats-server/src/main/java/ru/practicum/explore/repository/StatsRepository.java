package ru.practicum.explore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.explore.useDto.dto.StatsDto;
import ru.practicum.explore.model.ModelHit;

import java.time.LocalDateTime;
import java.util.List;


@EnableJpaRepositories
public interface StatsRepository extends JpaRepository<ModelHit, Long> {
    @Query("select new ru.practicum.explore.useDto.dto.StatsDto(mh.app, mh.uri, count(mh.ip)) " +
            "from ModelHit mh " +
            "where mh.time between ?1 and ?2 " +
            "group by mh.app, mh.uri " +
            "order by count(mh.ip) desc")
    List<StatsDto> findAll(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.explore.useDto.dto.StatsDto(mh.app, mh.uri, count(distinct mh.ip)) " +
            "from ModelHit mh " +
            "where mh.dateTime between ?1 and ?2 " +
            "and mh.uri in (?3) " +
            "group by mh.app, mh.uri " +
            "order by count(distinct mh.ip) desc")
    List<StatsDto> findAllUniqueIp(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.explore.useDto.dto.StatsDto(mh.app, mh.uri, count(mh.ip)) " +
            "from ModelHit mh " +
            "where mh.time between ?1 and ?2 " +
            "and mh.uri in (?3) " +
            "group by mh.app, mh.uri " +
            "order by count(mh.ip) desc")
    List<StatsDto> findAllUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.explore.useDto.dto.StatsDto(mh.app, mh.uri, count(distinct mh.ip)) " +
            "from ModelHit mh " +
            "where mh.dateTime between ?1 and ?2 " +
            "and mh.uri in (?3) " +
            "group by mh.app, mh.uri " +
            "order by count(distinct mh.ip) desc")
    List<StatsDto> findStatsByUrisWithUniqueIp(LocalDateTime start, LocalDateTime end, List<String> uris);
}
