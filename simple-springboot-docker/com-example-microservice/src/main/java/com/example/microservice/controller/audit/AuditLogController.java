package com.example.microservice.controller.audit;

import com.example.microservice.api.ControllerAuditLog;
import com.example.microservice.config.APIConstants;
import com.example.microservice.service.audit.AuditLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * REST Controller to handle Controller Audit Log related requests.
 *
 * Annotations Explained:
 * <ul>
 *     <li>
 *         RestController = A convenience annotation that is itself annotated with Controller  and ResponseBody.
 *     </li>
 *     <li>
 *         RequestMapping = Annotation for mapping web requests onto methods in request-handling classes with
 *         flexible method signatures.
 *     </li>
 *     <li>
 *         Api = Marks a class as a Swagger resource used for documentation purposes.
 *     </li>
 *     <li>
 *        Autowired = Marks a constructor, field, setter method or config method
 *        as to be autowired by Spring's dependency injection facilities.
 *     </li>
 * </ul>
 */
@RestController
@RequestMapping(APIConstants.AUDIT_LOG_BASE_URL)
@Api(value = APIConstants.AUDIT_LOG_BASE_URL)
public class AuditLogController {
    // Service layer to handle business operations
    private final AuditLogService auditLogService;

    /**
     * Constructor for the Controller.
     *
     * Since the Spring Team recommends: "Always use constructor based dependency injection in your beans" we've done
     * that here. You can also Autowire the dependencies in the class field members.
     *
     * @param auditLogService Service that operates on Audit Log data.
     */
    @Autowired
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Retrieves all {@link ControllerAuditLog} entries from data store.
     *
     * @return Collection of {@link ControllerAuditLog}
     */
    @GetMapping
    @ApiOperation(value = "Calls underlying Log JPA Repository to retrieve all entries.", response = Collection.class)
    public Collection<ControllerAuditLog> getAllControllerAuditLogs() {
        return auditLogService.getAllControllerAuditLogs();
    }

    /**
     * Retrieves the {@link ControllerAuditLog} with the specified ID
     *
     * @param id Long unique identifier for audit log data
     * @return Controller Audit Log entry or HTTP 404 exception
     */
    @GetMapping(value = "/{id}")
    @ApiOperation(value = "Example to show GET request that calls underlying Log JPA Repository.",
            notes = "Will throw a custom NotFoundException in the event the provided ID is invalid.")
    public ControllerAuditLog getControllerAuditLogById(@PathVariable("id") Long id) {
        // Attempt to get the log by ID, if it doesn't exist, throw exception mapped to HTTP 404 status
        return auditLogService.getControllerAuditLogById(id);
    }

    /**
     * Retrieves count of controller audit logs.
     *
     * @return Long count of records in log repository.
     */
    @GetMapping(value = "/count")
    @ApiOperation(value = "Calls underlying Log JPA Repository to retrieve Log count.")
    public Long getCount() {
        return auditLogService.getLogCount();
    }

    /**
     * Deletes the log record that corresponds with the provided ID.
     *
     * @param id Long identifier for controller audit log to delete
     * @return String result of delete operation
     */
    @DeleteMapping(value = "/{id}")
    @ApiOperation(value = "Calls underlying Log JPA Repository to remove the matching log.")
    public String deleteControllerAuditLogById(@PathVariable("id") Long id) {
        return auditLogService.deleteAuditLogById(id);
    }

    /**
     * Retrieves Audit Logs matching the corresponding {@link RequestMethod}, i.e. GET, POST, PUT, etc.
     *
     * @param requestMethod {@link RequestMethod} enumeration of HTTP request methods.
     * @return Collection of {@link ControllerAuditLog} records that match the provided request method.
     */
    @GetMapping(value = "/ofMapping/{mapping}")
    @ApiOperation(value = "Calls underlying Log JPA Repository to find log data with the request mapping.",
            notes = "Uses Enum value for mapping input and also a JPQL Query.",
            response = Collection.class)
    public Collection<ControllerAuditLog> getLogsByRequestMapping(@PathVariable("mapping") RequestMethod requestMethod) {
        return auditLogService.getLogsByRequestMapping(requestMethod);
    }

    /**
     * Ensures Spring will throw a HTTP 404 exception when a {@link EmptyResultDataAccessException} exception occurs.
     *
     * Annotations Explained:
     * <ul>
     *     <li>
     *         ResponseStatus = Marks a method or exception class with the status code and reason that should be returned.
     *     </li>
     *     <li>
     *         ExceptionHandler = Annotation for handling exceptions in specific handler classes and/or handler methods.
     *     </li>
     * </ul>
     *
     * @param exception {@link EmptyResultDataAccessException} to extract error info from
     * @return String error message
     */
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler({EmptyResultDataAccessException.class})
    public String databaseEmptyResultDataAccessException(EmptyResultDataAccessException exception) {
        // You can add in additional error handling logic here, also, you can utilize Controller Advice
        // offered by Spring as well!
        return "ERROR: Database operation failed due to empty result set. " + exception.getMessage();
    }
}
