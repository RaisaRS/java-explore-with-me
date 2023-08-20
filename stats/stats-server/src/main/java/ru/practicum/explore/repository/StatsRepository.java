package ru.practicum.explore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.model.ModelHit;
import ru.practicum.explore.useDto.dto.StatsDto;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface StatsRepository extends JpaRepository<ModelHit, Long> {
    @Query("select new ru.practicum.explore.useDto.dto.StatsDto(mh.app, mh.uri, count(mh.ip)) " +
            "from ModelHit mh " +
            "where mh.timestamp between ?1 and ?2 " +
            "group by mh.app, mh.uri " +
            "order by count(mh.ip) desc")
    List<StatsDto> findAll(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.explore.useDto.dto.StatsDto(mh.app, mh.uri, count(distinct mh.ip)) " +
            "from ModelHit mh " +
            "where mh.timestamp between ?1 and ?2 " +
            "and mh.uri in (?3) " +
            "group by mh.app, mh.uri " +
            "order by count(distinct mh.ip) desc")
    List<StatsDto> findAllUniqueIp(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.explore.useDto.dto.StatsDto(mh.app, mh.uri, count(mh.ip)) " +
            "from ModelHit mh " +
            "where mh.timestamp between ?1 and ?2 " +
            "and mh.uri in (?3) " +
            "group by mh.app, mh.uri " +
            "order by count(mh.ip) desc")
    List<StatsDto> findAllByUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.explore.useDto.dto.StatsDto(mh.app, mh.uri, count(distinct mh.ip)) " +
            "from ModelHit mh " +
            "where mh.timestamp between ?1 and ?2 " +
            "and mh.uri in (?3) " +
            "group by mh.app, mh.uri " +
            "order by count(distinct mh.ip) desc")
    List<StatsDto> findStatsByUrisByUniqueIp(LocalDateTime start, LocalDateTime end, List<String> uris);
}
