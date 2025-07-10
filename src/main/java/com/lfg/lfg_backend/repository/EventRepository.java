package com.lfg.lfg_backend.repository;

import com.lfg.lfg_backend.model.Event;
import com.lfg.lfg_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    Page<Event> findAll(Pageable pageable);
    List<Event> findByCreator(User creator);
    long countByDateAfter(LocalDate date);
    List<Event> findTop5ByOrderByCreatedAtDesc();

    // Query per trovare eventi entro un certo raggio dalla posizione utente (in km)
    @Query(
            value = "SELECT * FROM events " +
                    "WHERE (6371 * acos(cos(radians(:userLat)) * cos(radians(latitude)) " +
                    "* cos(radians(longitude) - radians(:userLng)) + sin(radians(:userLat)) * sin(radians(latitude)))) <= :radius " +
                    "AND date >= CURRENT_DATE",
            nativeQuery = true)
    List<Event> findEventsWithinRadius(
            @Param("userLat") Double userLat,
            @Param("userLng") Double userLng,
            @Param("radius") Double radius
    );
}
