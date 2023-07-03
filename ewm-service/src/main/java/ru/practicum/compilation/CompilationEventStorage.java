package ru.practicum.compilation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.compilation.model.CompilationEvent;

import java.util.Collection;
import java.util.List;

public interface CompilationEventStorage extends JpaRepository<CompilationEvent, Long> {
    long deleteByCompilation_Id(Long compId);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"event", "event.category", "event.initiator"})
    List<CompilationEvent> findByCompilation_Id(Long id);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"event", "event.category", "event.initiator"})
    List<CompilationEvent> findByCompilation_IdIn(Collection<Long> compilationIds);


}
