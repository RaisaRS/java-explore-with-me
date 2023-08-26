package ru.practicum.explore.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explore.compilation.Compilation;
import ru.practicum.explore.compilation.CompilationRepository;
import ru.practicum.explore.compilation.dto.CompilationDto;
import ru.practicum.explore.compilation.dto.CompilationMapper;
import ru.practicum.explore.compilation.dto.CompilationNewDto;
import ru.practicum.explore.compilation.dto.CompilationUpdateDto;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.event.repositories.EventRepository;
import ru.practicum.explore.exceptions.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto addCompilation(CompilationNewDto compilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(compilationDto);

        if (!Objects.nonNull(compilationDto.getPinned())) {
            compilation.setPinned(false);
        }

        if (Objects.nonNull(compilationDto.getEvents())) {
            List<Event> events = eventRepository.findAllById(compilationDto.getEvents());
            compilation.setEvents(events);
        }
        Compilation saveCompilation = compilationRepository.save(compilation);
        return CompilationMapper.toCompilationDto(saveCompilation);
    }

    @Override
    public CompilationDto updateCompilation(Long compilationId, CompilationUpdateDto compilationDto) {
        Compilation updatedCompilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException(
                        "Подборка событий не найдена, id = " + compilationId));

        if (compilationDto.getTitle() != null) {
            updatedCompilation.setTitle(compilationDto.getTitle());
        }
        if (compilationDto.getPinned() != null) {
            updatedCompilation.setPinned(compilationDto.getPinned());
        }
        if (compilationDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(compilationDto.getEvents());
            updatedCompilation.setEvents(events);
        }

        Compilation afterUpdate = compilationRepository.save(updatedCompilation);
        return CompilationMapper.toCompilationDto(afterUpdate);
    }

    @Override
    public void deleteCompilation(Long compilationId) {
        compilationRepository.deleteById(compilationId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable);

        return compilations.size() == 0 ? Collections.emptyList() : CompilationMapper
                .listCompilationDtos(compilations);
    }

    @Override
    public CompilationDto getCompilationsById(Long compilationId) {
        Compilation compilation = compilationRepository.findById(compilationId).orElseThrow(() ->
                new NotFoundException("Подборка событий не найдена, id = " + compilationId));

        return CompilationMapper.toCompilationDto(compilation);
    }
}
