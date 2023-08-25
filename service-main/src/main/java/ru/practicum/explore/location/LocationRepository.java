package ru.practicum.explore.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    //List<Location> findByLatAndLon(float lat, float lon);

    Optional<Location> findByLatAndLon(float lat, float lon);
}
