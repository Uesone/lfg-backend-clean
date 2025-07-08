package com.lfg.lfg_backend.repository;

import com.lfg.lfg_backend.model.Event;
import com.lfg.lfg_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    Page<Event> findAll(Pageable pageable);
    List<Event> findByCreator(User creator);
    long countByDateAfter(LocalDate date);
    List<Event> findTop5ByOrderByCreatedAtDesc();
}
