package com.example.microservice.controller.example;

import com.example.microservice.service.ExampleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Example REST Controller showing CRUD operations.
 *
 * @author Jack Phillips
 */
@RestController
@RequestMapping("/example")
public class ExampleController {

    private final ExampleService exampleService;

    @Autowired
    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @GetMapping
    @ApiOperation(value = "Example to show GET request that calls underlying service",
            notes = "The Service will call an external URL showing the use of Springs Rest Template",
            response = String.class)
    public String performExternalCall() {
        return exampleService.executeGetRequest();
    }

    @ApiOperation(value = "Example to show GET request with a Path Variable")
    @GetMapping(value = "/{id}")
    public String getExampleWithParam(@PathVariable("id") String id) {
        return "Get Endpoint Invoked with ID = " + id;
    }

    @PostMapping
    @ApiOperation(value = "Example to show POST request with a Request Body", response = String.class)
    public String postExample(@RequestBody Map<String, String> inputMap) {
        return "POST Endpoint Invoked";
    }

    @PutMapping
    @ApiOperation(value = "Example to show PUT request with a Request Body", response = String.class)
    public String putExample(@RequestBody Map<String, String> inputMap) {
        return "PUT Endpoint Invoked";
    }
}
