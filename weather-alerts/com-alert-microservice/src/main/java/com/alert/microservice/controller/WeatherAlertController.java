package com.alert.microservice.controller;

import com.alert.microservice.api.WeatherAlert;
import com.alert.microservice.service.weather.WeatherAlertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/**
 * REST Controller to handle Weather Alert related requests.
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
 *        ApiOperation = Describes an operation or typically a HTTP method against a specific path.
 *     </li>
 *     <li>
 *        PathVariable = Indicates that a method parameter should be bound to a URI template variable.
 *     </li>
 *     <li>
 *        RequestParam = Indicates that a method parameter should be bound to a web request parameter.
 *     </li>
 *     <li>
 *         RequestBody = Indicates a method parameter should be bound to the body of the web request (ex. POST requests).
 *     </li>
 * </ul>
 */
@Api(tags = "Weather Alerts API")
@RestController
@RequestMapping("/weather")
public class WeatherAlertController {

    // Final variables that are injected in the constructor
    private final WeatherAlertService weatherAlertService;

    /**
     * Constructor for this REST Controller
     *
     * @param weatherAlertService {@link WeatherAlertService}
     */
    public WeatherAlertController(WeatherAlertService weatherAlertService) {
        this.weatherAlertService = weatherAlertService;
    }

    @ApiOperation(
            value = "Processes Weather Alert Data End-to-End",
            notes = "This endpoint will pull in a Atom Feed, transform and send the to Kafka which is then picked up by " +
                    "a Kafka consumer that will then push the data to Elasticsearch. Also, this will push a file to an " +
                    "S3 source bucket where a AWS Lambda Function will be invoked to copy it to another S3 sink bucket.",
            tags = { "End-to-End Process" },
            response = ResponseEntity.class
    )
    @GetMapping("/process")
    public ResponseEntity processAlertsEndToEnd() throws IOException {
        weatherAlertService.executeEndToEndProcess();
        return ResponseEntity.ok().build();
    }

    /**
     * Performs in essence a SELECT * query on weather alert data limiting the number of results if specified in the
     * request parameter.
     *
     * @param limit Optional Integer to set a limit on the number of results returned. If no value is set then it will
     *              default to 10.
     * @return Collection of WeatherAlert data from the weather alert Elasticsearch index
     * @throws IOException
     */
    @GetMapping("/search")
    @ApiOperation(
            value = "Searches Elasticsearch Weather Alert data and Limits the Number of Results to the Specified Limit",
            notes = "This operates in a similar fashion to a SQL \"SELECT *\" query. If no limit is specified it defaults to 10",
            tags = { "Query Weather Data" },
            response = Collection.class
    )
    public Collection<WeatherAlert> retrieveWeatherAlerts(@RequestParam(name = "limit", required = false) Optional<Integer> limit) throws IOException {
        return weatherAlertService.retrieveElasticsearchData(limit.orElse(10));
    }

    /**
     * Performs an Elasticsearch Query on Weather Alert Data.
     *
     * @param isFuzzySearch Boolean to dictate if we should perform a fuzzy search or not
     * @param weatherAlert WeatherAlert to extract query information from
     * @return Collection of WeatherAlert responses that match from Elasticsearch
     * @throws IOException
     */
    @PostMapping("/search/{isFuzzySearch}")
    @ApiOperation(
            value = "Performs an Elasticsearch Query on Weather Alert Data",
            notes = "Fuzzy Match capabilities follow the default Elasticsearch Fuzziness parameters in terms of edit distance",
            tags = { "Query Weather Data" },
            response = Collection.class
    )
    public Collection<WeatherAlert> typedSearch(
            @PathVariable("isFuzzySearch") Boolean isFuzzySearch,
            @RequestBody WeatherAlert weatherAlert) throws IOException {
        return weatherAlertService.searchWeatherAlerts(weatherAlert, isFuzzySearch);
    }
}
