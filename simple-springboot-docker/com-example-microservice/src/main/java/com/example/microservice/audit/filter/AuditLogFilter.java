package com.example.microservice.audit.filter;

import com.example.microservice.api.ControllerAuditLog;
import com.example.microservice.audit.repository.ControllerAuditRepository;
import com.example.microservice.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Filter to log all request and response data to data store.
 *
 * Annotations:
 * <ul>
 *   <li>
 *       Component = Established auto-detection for Spring to find.
 *   </li>
 *   <li>
 *       ConditionalOnProperty = checks if the specified properties have a specific value. By
 *       default the properties must be present in the Environment and <strong>not</strong> equal to false.
 *   </li>
 *   <li>
 *       Value = Annotation at the field or method/constructor parameter level
 *       that indicates a default value expression for the affected argument.
 *   </li>
 *   <li>
 *       Autowired = Marks a constructor, field, setter method or config method
 *       as to be autowired by Spring's dependency injection facilities.
 *   </li>
 * </ul>
 *
 * @author Jack Phillips
 */
@Component
@ConditionalOnProperty(name = "audit.log.enabled")
public class AuditLogFilter extends OncePerRequestFilter {
    // Class constant
    private static final String LOG_ALL_PATTERN = "/**";

    // Logger which can be altered to log all request/responses to console
    private Logger log = LoggerFactory.getLogger(this.getClass());

    // Set of URI patterns to log
    @Value("${audit.log.inclusions:#{null}}")
    private Set<String> inclusionSet;

    // Limit request/response message body to a specific size if necessary.
    @Value("${audit.log.message.body.limit:#{null}}")
    private Integer auditLogMessageBodyLimit;

    // JPA Repository to push data to DB
    private final ControllerAuditRepository controllerAuditRepository;

    /**
     * Constructor for Audit Log Filter
     *
     * @param controllerAuditRepository
     */
    @Autowired
    public AuditLogFilter(ControllerAuditRepository controllerAuditRepository) {
        this.controllerAuditRepository = controllerAuditRepository;
        init();
    }

    /**
     * Sets up other checked values in class
     */
    private void init() {
        // If no maximum value is set for the message body limit then use max value. Note that in memory data stores
        // such as H2 will limit the amount of data you store in VARCHAR fields causing errors to be thrown unless
        // restricted.
        this.auditLogMessageBodyLimit = CommonUtil.defaultIfNull(auditLogMessageBodyLimit, Integer.MAX_VALUE);
        // Set of String patterns to log to audit table
        this.inclusionSet = CommonUtil.isNotEmpty(inclusionSet) ? inclusionSet: CommonUtil.setOf(LOG_ALL_PATTERN);
    }

    /**
     * Stores a request attribute for "already filtered", proceeding without filtering again if the attribute is
     * already there. This guarantees this function is only invoked once per request within a single request thread.
     *
     * @param request Extends the {@link javax.servlet.ServletRequest} interface to provide request information for
     *                HTTP servlets.
     * @param response Extends the {@link ServletResponse} interface to provide HTTP-specificfunctionality in sending
     *                 a response.
     * @param filterChain object provided by the servlet container to the developer giving a view into the invocation
     *                    chain of a filtered request for a resource.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // If we should perform logging filter then do so, otherwise, let the request/response proceed down the chain
        if (isFilterable(request) && !isAsyncDispatch(request)) {
            doFilterWrapped(new ContentCachingRequestWrapper(request),
                    new ContentCachingResponseWrapper(response),
                    filterChain);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Takes wrapped request/response data and performs filter to save to backend data store.
     *
     * @param request {@link ContentCachingRequestWrapper} wrapper over HTTP request
     * @param response {@link ContentCachingResponseWrapper} wrapper over HTTP response
     * @param filterChain object provided by the servlet container to the developer giving a view into the invocation
     *                    chain of a filtered request for a resource.
     * @throws ServletException
     * @throws IOException
     */
    private void doFilterWrapped(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // Perform filtering as usual
            filterChain.doFilter(request, response);
        } finally {
            // Create and save payload information
            controllerAuditRepository.save(createControllerAuditLog(request, response));
            response.copyBodyToResponse();
        }
    }

    /**
     * Creates a {@link ControllerAuditLog} with the relevant information populated
     *
     * @param request {@link ContentCachingRequestWrapper} wrapper over HTTP request
     * @param response {@link ContentCachingResponseWrapper} wrapper over HTTP response
     * @return ControllerAuditLog built usign the provided data
     */
    private ControllerAuditLog createControllerAuditLog(ContentCachingRequestWrapper request,
                                                        ContentCachingResponseWrapper response) {
        ControllerAuditLog controllerAuditLog = new ControllerAuditLog();
        // Process request data
        controllerAuditLog.setRequestMapping(request.getMethod());
        controllerAuditLog.setUri(request.getRequestURI());
        controllerAuditLog.setRequestBody(processMessageBody(request.getContentAsByteArray(), request.getContentType(),
                request.getCharacterEncoding()));
        controllerAuditLog.setInboundAddress(request.getRemoteAddr());
        // Process response data
        controllerAuditLog.setResponseStatus(response.getStatusCode());
        controllerAuditLog.setResponseBody(processMessageBody(response.getContentAsByteArray(), response.getContentType(),
                        response.getCharacterEncoding()));

        return controllerAuditLog;
    }

    /**
     * Creates String representation of request/response body.
     *
     * @param content         byte array containing contents of request/response
     * @param contentType     content type of request/response, ex. application/json
     * @param contentEncoding String name of a supported {@link java.nio.charset.Charset}
     * @return String message body of request/response
     */
    private String processMessageBody(byte[] content, String contentType, String contentEncoding) {
        StringBuilder builder = new StringBuilder();
        if (content.length > 0) {
            try {
                String contentString = new String(content, contentEncoding);
                Stream.of(contentString.split("\r\n|\r|\n")).forEach(builder::append);
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage());
            }
        }
        // Slice off end of String if data store is setup to only store a certain amount of characters
        return safeSubstring(auditLogMessageBodyLimit, builder.toString());
    }

    /**
     * Method to check to see if we should perform filtering or not.
     *
     * @param request
     * @return boolean true if we should perform filtering, false otherwise.
     */
    private boolean isFilterable(final HttpServletRequest request) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        antPathMatcher.setCaseSensitive(false);
        if (CommonUtil.isNotEmpty(inclusionSet)) {
            for (String urlPattern : inclusionSet) {
                if (antPathMatcher.match(urlPattern, request.getRequestURI())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the provided String can be be sliced or not, if so, it will return the substring, otherwise it
     * will return the original String value.
     *
     * @param size Integer end index, exclusive
     * @param str String to retrieve substring value
     * @return String that is either the substring value or original value
     */
    private String safeSubstring(final Integer size, final String str) {
        // If the string is null/empty or the size is less than whats provided return
        // the provided String, otherwise, slice it to the passed in size
        return (CommonUtil.isNullOrEmpty(str) || str.length() < size) ? str : str.substring(0, size);
    }
}
