package ru.practicum.explore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;


@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
public class ModelStats {
    @Column
    private String app;
    @Column
    private String uri;
    @Column(name = "ip", nullable = false)
    //@Column
    private Long hits;
}
