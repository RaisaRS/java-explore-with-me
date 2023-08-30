package ru.practicum.explore.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore.compilation.Compilation;
import ru.practicum.explore.compilation.CompilationRepository;
import ru.practicum.explore.compilation.dto.*;
import ru.practicum.explore.event.Event;
import ru.practicum.explore.event.repositories.EventRepository;
import ru.practicum.explore.exceptions.NotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
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
    @Transactional
    public CompilationDto updateCompilation(Long compId, CompilationUpdateDto compilationDto) {
        Compilation updatedCompilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(
                        "Подборка событий не найдена, id = " + compId));

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
    @Transactional
    public void deleteCompilation(Long compilationId) {
        compilationRepository.deleteById(compilationId);
    }

    @Override
    public List<CompilationWithIdAndPinned> getCompilations(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return compilationRepository.findAll(pageable)
                .stream()
                .map(CompilationMapper::toCompWithIdAndPinned)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CompilationDto getCompilationsById(Long compilationId) {
        Compilation compilation = compilationRepository.findById(compilationId).orElseThrow(() ->
                new NotFoundException("Подборка событий не найдена, id = " + compilationId));

        return CompilationMapper.toCompilationDto(compilation);
    }
}
