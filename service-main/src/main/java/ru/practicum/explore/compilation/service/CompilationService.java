package ru.practicum.explore.compilation.service;

import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.dto.CompilationNewDto;
import ru.practicum.explore.compilation.dto.CompilationUpdateDto;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilation(CompilationNewDto compilationDto);

    CompilationDto updateCompilation(Long compilationId, CompilationUpdateDto compilationDto);

    void deleteCompilation(Long compilationId);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilationsById(Long compilationId);
}
