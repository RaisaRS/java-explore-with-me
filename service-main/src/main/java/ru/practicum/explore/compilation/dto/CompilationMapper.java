package ru.practicum.explore.compilation.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.explore.compilation.Compilation;
import ru.practicum.explore.event.dto.EventMapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {

    public Compilation toCompilation(CompilationDto compilationDto) {
        return Compilation.builder()
                .title(compilationDto.getTitle())
                .pinned(compilationDto.getPinned())
                .build();
    }

    public Compilation toCompilation(CompilationNewDto compilationNewDto) {
        return Compilation.builder()
                .title(compilationNewDto.getTitle())
                .pinned(compilationNewDto.getPinned())
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        return new CompilationDto(
                compilation.getId(),
                compilation.getTitle(),
                compilation.getPinned(),
                compilation.getEvents() == null ? Collections.emptyList() : EventMapper.listEventShortDto(
                        compilation.getEvents())
        );
    }

    public CompilationWithIdAndPinned toCompWithIdAndPinned(Compilation compilation) {
        return new CompilationWithIdAndPinned(
                compilation.getId(),
                compilation.getTitle(),
                compilation.getPinned(),
                compilation.getEvents() == null ? Collections.emptyList() : EventMapper.listEventShortDto(
                        compilation.getEvents())
        );
    }

    public List<CompilationDto> listCompilationDtos(List<Compilation> compilations) {
        return compilations.stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }
}
