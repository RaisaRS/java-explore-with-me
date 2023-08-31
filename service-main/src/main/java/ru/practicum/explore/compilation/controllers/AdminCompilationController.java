package ru.practicum.explore.compilation.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.dto.CompilationNewDto;
import ru.practicum.explore.compilation.dto.CompilationUpdateDto;
import ru.practicum.explore.compilation.service.CompilationService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("/admin/compilations")
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@Valid @RequestBody CompilationNewDto compilationDto) {
        log.info("Получен POST- запрос: /admin/compilations (Admin). Добавлена подборка событий (dto) {}",
                compilationDto);
        return compilationService.addCompilation(compilationDto);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                            @Valid @RequestBody CompilationUpdateDto compilationDto) {
        log.info("Получен PATCH- запрос: /admin/compilations{compId} (Admin). Подборка событий (id) {}" +
                " обновлена (dto): {}", compId, compilationDto);
        return compilationService.updateCompilation(compId, compilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("Получен DELETE- запрос: /admin/compilations{compId} (Admin). Удалена подборка событий (id): {}",
                compId);
        compilationService.deleteCompilation(compId);
    }
}
