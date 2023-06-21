package ru.practicum.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.stats.model.EndpointHit;

public interface StatsStorage extends JpaRepository<EndpointHit, Long>, QuerydslPredicateExecutor<EndpointHit> {
}
