package com.alert.microservice.controller.aws;

import com.alert.microservice.api.AwsLambdaProperties;
import com.alert.microservice.service.aws.LambdaService;
import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.services.lambda.model.DeleteFunctionResult;
import com.amazonaws.services.lambda.model.GetFunctionResult;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collection;

/**
 * REST Controller to handle AWS Lambda related REST requests.
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
 * </ul>
 */
@Api(tags = "AWS Lambda API")
@RestController
@RequestMapping("/aws/lambda")
public class LambdaController {
    // Final variables that are injected in the constructor
    private final LambdaService lambdaService;

    /**
     * Constructor for this {@link LambdaController}
     *
     * @param lambdaService service to perform AWS Lambda related operations
     */
    public LambdaController(LambdaService lambdaService) {
        this.lambdaService = lambdaService;
    }

    @ApiOperation(value = "Retrieves the current List of AWS Lambda Functions", response = ListFunctionsResult.class)
    @GetMapping("/functions")
    public ListFunctionsResult getLambdaFunctions() {
        return lambdaService.listLambdaFunctions();
    }

    @ApiOperation(value = "Retrieves the current List of AWS Lambda Function Names", response = Collection.class)
    @GetMapping("/functions/names")
    public Collection<String> getLambdaFunctionNames() {
        return lambdaService.getLambdaFunctionNames();
    }

    @GetMapping("/functions/{functionName}")
    public GetFunctionResult getLambdaFunction(@PathVariable("functionName") String functionName) {
        return lambdaService.getLambdaFunction(functionName);
    }

    @ApiOperation(value = "Retrieves the current List of AWS Lambda Function Names", response = Long.class)
    @GetMapping("/functions/count")
    public Long getLambdaFunctionCount() {
        return lambdaService.lambdaFunctionCount();
    }

    @ApiOperation(value = "Checks known AWS Lambda Functions to see if it exists or not", response = Boolean.class)
    @GetMapping("/functions/{functionName}/exists")
    public Boolean lambdaExists(@PathVariable("functionName") String functionName) {
        return lambdaService.lambdaFunctionExists(functionName);
    }

    @ApiOperation(value = "Deletes all AWS Lambda Functions", response = Collection.class)
    @DeleteMapping("/functions")
    public Collection<DeleteFunctionResult> deleteAllLambdaFunctions() {
        return lambdaService.deleteAllLambdaFunctions();
    }

    @ApiOperation(value = "Deletes a AWS Lambda Function by Name", response = DeleteFunctionResult.class)
    @DeleteMapping("/functions/{functionName}")
    public DeleteFunctionResult deleteLambdaFunction(@PathVariable("functionName") String functionName) {
        return lambdaService.deleteLambdaFunction(functionName);
    }

    @ApiOperation(
            value = "Creates the AWS Lambda Function to Handle Copying S3 Data from one Bucket to Another",
            response = AmazonWebServiceResult.class
    )
    @PostMapping("/createAlertFunction")
    public AmazonWebServiceResult createLambda() throws IOException {
        return lambdaService.createLambdaFunction();
    }

    @ApiOperation(
            value = "Creates the AWS Lambda Function using the Provided Lambda Properties",
            response = AmazonWebServiceResult.class
    )
    @PostMapping
    public AmazonWebServiceResult createLambda(@RequestBody AwsLambdaProperties lambdaProperties) throws IOException {
        return lambdaService.createFunction(lambdaProperties);
    }

    @ApiOperation(
            value = "Creates or Updates an existing AWS Lambda Function using the Provided Lambda Properties",
            response = AmazonWebServiceResult.class
    )
    @PutMapping
    public AmazonWebServiceResult updateLambda(@RequestBody AwsLambdaProperties lambdaProperties) throws IOException {
        return lambdaService.createOrUpdateLambdaFunction(lambdaProperties);
    }
}
