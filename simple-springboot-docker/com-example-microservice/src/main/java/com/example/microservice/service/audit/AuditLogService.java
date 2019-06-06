package com.example.microservice.service.audit;

import com.example.microservice.api.ControllerAuditLog;
import com.example.microservice.audit.repository.ControllerAuditRepository;
import com.example.microservice.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collection;

/**
 * Class operates on Audit Log data and performs service operations.
 *
 * Annotations Explained:
 * <ul>
 *     <li>
 *         Service = Indicates that an annotated class is a "Service".
 *     </li>
 *     <li>
 *         Autowired = Marks a constructor, field, setter method or config method
 *         as to be autowired by Spring's dependency injection facilities.
 *     </li>
 * </ul>
 */
@Service
public class AuditLogService {

    // JPA Repository to handle audit log data
    private final ControllerAuditRepository controllerAuditRepository;

    /**
     * Constructor for the Service.
     *
     * Since the Spring Team recommends: "Always use constructor based dependency injection in your beans" we've done
     * that here. You can also Autowire the dependencies in the class field members.
     *
     * @param controllerAuditRepository JPA Repository to operate on audit log data
     */
    @Autowired
    public AuditLogService(ControllerAuditRepository controllerAuditRepository) {
        this.controllerAuditRepository = controllerAuditRepository;
    }

    /**
     * Retrieves all {@link ControllerAuditLog} entries from log data store.
     *
     * @return Collection of {@link ControllerAuditLog}
     */
    public Collection<ControllerAuditLog> getAllControllerAuditLogs() {
        return controllerAuditRepository.findAll();
    }

    /**
     * Retrieves the {@link ControllerAuditLog} with the specified ID. Will throw a custom NotFoundException in the
     * event the provided ID is invalid.
     *
     * @param id Long unique identifier for audit log data
     * @return Controller Audit Log entry or HTTP 404 exception
     */
    public ControllerAuditLog getControllerAuditLogById(final Long id) {
        // Attempt to get the log by ID, if it doesn't exist, throw exception mapped to HTTP 404 status
        return controllerAuditRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cannot find Audit Log with ID " + id));
    }

    /**
     * Retrieves count of Audit Logs.
     *
     * @return Long count of records in Log repository.
     */
    public Long getLogCount() {
        return controllerAuditRepository.count();
    }

    /**
     * Deletes the log record that corresponds with the provided ID.
     *
     * @param id Long identifier for controller audit log to delete
     * @return String result of delete operation.
     */
    public String deleteAuditLogById(final Long id) {
        controllerAuditRepository.deleteById(id);
        return id + " has been deleted.";
    }

    /**
     * Retrieves Audit Logs matching the corresponding {@link RequestMethod}, i.e. GET, POST, PUT, etc.
     *
     * @param requestMethod {@link RequestMethod} enumeration of HTTP request methods.
     * @return Collection of {@link ControllerAuditLog} records that match the provided request method.
     */
    public Collection<ControllerAuditLog> getLogsByRequestMapping(final RequestMethod requestMethod) {
        return controllerAuditRepository.findAllByRequestMapping(requestMethod.name());
    }
}
