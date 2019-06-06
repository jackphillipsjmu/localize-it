package com.example.microservice.audit.repository;

import com.example.microservice.api.ControllerAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * JPA Repository to provide Database functions for the Controller Audit Log.
 *
 * Annotations Explained:
 * <ul>
 *     <li>
 *         Repository = Indicates that an annotated class is a "Repository".
 *     </li>
 *     <li>
 *         Query = Annotation to declare finder queries directly on repository methods.
 *     </li>
 * </ul>
 */
@Repository
public interface ControllerAuditRepository extends JpaRepository<ControllerAuditLog, Long> {
    /**
     * Example JPQL query which grabs data where the request mapping matches what is provided.
     *
     * @param requestMapValue String GET, POST, PUT, etc. to retrieve.
     * @return Collection of {@link ControllerAuditLog}
     */
    @Query("SELECT l FROM ControllerAuditLog l WHERE l.requestMapping = ?1")
    Collection<ControllerAuditLog> findAllByRequestMapping(String requestMapValue);
}
