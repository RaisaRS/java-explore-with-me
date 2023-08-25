package ru.practicum.explore.compilation.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.service.CompilationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("/compilations")
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(required = false, defaultValue = "0") int from,
                                                @RequestParam(required = false, defaultValue = "10") int size) {
        log.info("Получен GET-запрос: /compilations?pinned={pinned}&from={from}&size={size} (Public). " +
                "Просмотр подборки событий с параметрами: pinned {}, from: {}, size: {}.", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationsById(@PathVariable Long compId) {
        log.info("Получен GET- запрос: /compilations?pinned={pinned}&from={from}&size={size}] (Public). " +
                "Просмотр подборки событий (id): {}", compId);
        return compilationService.getCompilationsById(compId);
    }
}
