package ru.practicum.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.model.CompilationEvent;
import ru.practicum.compilation.model.CompilationMapper;
import ru.practicum.event.EventStorage;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventMapper;
import ru.practicum.utils.Constants;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationStorage compilationStorage;
    private final CompilationEventStorage compilationEventStorage;
    private final EventStorage eventStorage;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationStorage.save(CompilationMapper.toCompilation(newCompilationDto));
        List<CompilationEvent> compilationEvents = new ArrayList<>();

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            compilationEvents = createCompilationEvents(compilation, newCompilationDto.getEvents());
        }

        log.info("Created compilation with id = {}", compilation.getId());
        return CompilationMapper.toCompilationDto(compilation, compilationEvents);
    }

    @Override
    public void deleteCompilation(long compId) {
        try {
            compilationStorage.deleteById(compId);
            log.info("Compilation with id = {} was deleted", compId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Attempt to delete nonexistent compilation by id = {}", compId);
            throw new EntityNotFoundException(String.format(Constants.COMPILATION_NOT_FOUND_MESSAGE, compId));
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CompilationDto updateCompilation(long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = getCompilation(compId);
        updateCompilationWithNotNullFields(compilation, updateCompilationRequest);
        List<CompilationEvent> compilationEvents;

        if (updateCompilationRequest.getEvents() != null) {
            compilationEvents = updateCompilationEvent(compilation, updateCompilationRequest.getEvents());
        } else {
            compilationEvents = getCompilationEvents(compId);
        }

        compilationStorage.save(compilation);
        log.info("Updated compilation with id = {}", compId);
        return CompilationMapper.toCompilationDto(compilation, compilationEvents);
    }

    @Override
    public CompilationDto getCompilationById(long compId) {
        Compilation compilation = getCompilation(compId);
        log.info("Received compilation with id = {}", compId);
        return CompilationMapper.toCompilationDto(compilation, getCompilationEvents(compId));
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(from / size, size, sortById);
        List<Compilation> compilations;

        if (pinned != null) {
            compilations = compilationStorage.findByPinned(pinned, page).getContent();
        } else {
            compilations = compilationStorage.findAll(page).getContent();
        }

        List<CompilationDto> compilationDtos = CompilationMapper.toCompilationDto(compilations);
        fillCompilationEvents(compilationDtos);
        log.info("Received {} compilations", compilationDtos.size());
        return compilationDtos;
    }

    private void updateCompilationWithNotNullFields(Compilation compilation, UpdateCompilationRequest updateCompilationRequest) {
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        if (!StringUtils.isBlank(updateCompilationRequest.getTitle())) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
    }

    private void fillCompilationEvents(List<CompilationDto> compilationDtos) {
        Map<Long, CompilationDto> compilations = compilationDtos.stream()
                .collect(Collectors.toMap(CompilationDto::getId, Function.identity()));
        List<CompilationEvent> compilationEvents = compilationEventStorage.findByCompilation_IdIn(compilations.keySet());

        for (CompilationEvent compilationEvent : compilationEvents) {
            EventShortDto event = EventMapper.toEventShortDto(compilationEvent.getEvent());
            compilations.get(compilationEvent.getCompilation().getId()).getEvents().add(event);
        }
    }

    private List<CompilationEvent> getCompilationEvents(long compId) {
        List<CompilationEvent> compilationEvents = compilationEventStorage.findByCompilation_Id(compId);
        log.info("Received {} compilation events", compilationEvents.size());
        return compilationEvents;
    }

    private List<CompilationEvent> updateCompilationEvent(Compilation compilation, Set<Long> newEventIds) {
        long deletedCount = compilationEventStorage.deleteByCompilation_Id(compilation.getId());
        log.info("Deleted {} compilation events", deletedCount);

        return createCompilationEvents(compilation, newEventIds);
    }

    private List<CompilationEvent> createCompilationEvents(Compilation compilation, Set<Long> eventIds) {
        List<CompilationEvent> compilationEvents = new ArrayList<>();
        List<Event> events = getEvents(eventIds);

        for (Event event : events) {
            CompilationEvent compilationEvent = new CompilationEvent(compilation, event);
            compilationEvents.add(compilationEvent);
        }

        log.info("Created {} compilation events", compilationEvents.size());
        return compilationEventStorage.saveAll(compilationEvents);
    }

    private List<Event> getEvents(Set<Long> eventIds) {
        List<Event> events = eventStorage.findAllById(eventIds);

        if (events.size() != eventIds.size()) {
            log.warn("Attempt to create compilation with nonexistent events");
            throw new EntityNotFoundException(Constants.NOT_ALL_EVENTS_FOUND_MESSAGE);
        }

        return events;
    }

    private Compilation getCompilation(long compId) {
        Optional<Compilation> compilation = compilationStorage.findById(compId);

        if (compilation.isEmpty()) {
            log.warn("Attempt to get nonexistent compilation by id = {}", compId);
            throw new EntityNotFoundException(String.format(Constants.COMPILATION_NOT_FOUND_MESSAGE, compId));
        }

        return compilation.get();
    }
}
