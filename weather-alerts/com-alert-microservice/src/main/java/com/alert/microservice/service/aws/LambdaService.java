package com.alert.microservice.service.aws;

import com.alert.microservice.api.AwsLambdaProperties;
import com.alert.microservice.service.exception.AlertServiceException;
import com.alert.microservice.util.CollectionUtil;
import com.alert.microservice.util.CommonUtil;
import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.DeleteFunctionRequest;
import com.amazonaws.services.lambda.model.DeleteFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.GetFunctionRequest;
import com.amazonaws.services.lambda.model.GetFunctionResult;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.Runtime;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationRequest;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.LambdaConfiguration;
import com.amazonaws.services.s3.model.S3Event;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service to perform AWS Lambda related operations
 * <p>
 * Annotations Explained:
 *
 * <ul>
 *  <li>
 *      Service = Indicates that an annotated class is a "Service".
 *  </li>
 * </ul>
 */
@Service
public class LambdaService {
    // Class constant for local lambda functionS
    private static final String LAMBDA_ARN_BASE = "arn:aws:lambda:us-east-1:000000000000:function:";
    private static final String LAMBDA_CONFIG_NAME = "LambdaFunctionConfigurations";

    // Final variables that are injected in the service constructor
    private final AwsLambdaProperties lambdaProperties;
    private final AWSLambda awsLambda;

    /**
     * Constructor for this {@link LambdaService}
     *
     * @param lambdaProperties object to hold AWS Lambda related information
     * @param awsLambda        Interface for accessing AWS Lambda.
     */
    public LambdaService(AwsLambdaProperties lambdaProperties, AWSLambda awsLambda) {
        this.lambdaProperties = lambdaProperties;
        this.awsLambda = awsLambda;
    }

    /**
     * Retrieves the number of AWS Lambda functions.
     *
     * @return long count of AWS Lambda functions
     */
    public long lambdaFunctionCount() {
        return listLambdaFunctions().getFunctions().size();
    }

    /**
     * Returns a wrapped List of AWS Lambda Functions in a {@link ListFunctionsResult}
     *
     * @return ListFunctionResult A list of Lambda functions.
     */
    public ListFunctionsResult listLambdaFunctions() {
        return awsLambda.listFunctions();
    }

    /**
     * Retrieves a AWS Lambda {@link ListFunctionsResult} and then collects the names of each function into the result
     * {@link Collection} response.
     *
     * @return Collection of AWS Lambda Function String Names
     */
    public Collection<String> getLambdaFunctionNames() {
        // Retrieve all lambda function information from AWS then collect the function names to a list to return
        return listLambdaFunctions().getFunctions().stream()
                .map(FunctionConfiguration::getFunctionName)
                .collect(Collectors.toList());
    }

    /**
     * Creates a AWS Lambda function using the underlying {@link AwsLambdaProperties} that is injected into this class.
     *
     * @return AmazonWebServiceResult Details about a function's configuration.
     * @throws IOException in the event reading JAR of Lambda code information does not work properly
     */
    public AmazonWebServiceResult createLambdaFunction() throws IOException {
        return createFunction(lambdaProperties);
    }

    /**
     * Creates a AWS Lambda function using the provided {@link AwsLambdaProperties} or return the existing function.
     *
     * @param lambdaFunctionProperties {@link AwsLambdaProperties} used to create AWS Lambda Function
     * @return AmazonWebServiceResult Details about a function's configuration.
     * @throws IOException in the event reading JAR of Lambda code information does not work properly
     */
    public AmazonWebServiceResult createFunction(final AwsLambdaProperties lambdaFunctionProperties) throws IOException {
        // Null sanity check to avoid NPE
        CommonUtil.ifNullThrowException(lambdaFunctionProperties, new AlertServiceException("Cannot Create Lambda Function from Null Object"));
        // If the function already exists then return it
        if (lambdaFunctionExists(lambdaFunctionProperties.getFunctionName())) {
            return getLambdaFunction(lambdaFunctionProperties.getFunctionName());
        }

        // Create byte buffer of JVM Lambda code
        ByteBuffer byteBuffer = ByteBuffer.wrap(Files.readAllBytes(new File(lambdaFunctionProperties.getJarPath()).toPath()));
        // Create function code for new lambda function
        FunctionCode functionCode = new FunctionCode().withZipFile(byteBuffer);
        // Instantiate create function request
        CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
                .withFunctionName(lambdaFunctionProperties.getFunctionName())
                .withHandler(lambdaFunctionProperties.getHandler())
                .withCode(functionCode)
                .withRuntime(Runtime.Java8)
                .withRole(lambdaFunctionProperties.getRole())
                .withTimeout(lambdaFunctionProperties.getTimeout());
        // Create function
        return awsLambda.createFunction(createFunctionRequest);
    }

    /**
     * Creates a AWS Lambda function using the provided {@link AwsLambdaProperties} or will update the function
     * if it already exists.
     *
     * @param lambdaFunctionProperties {@link AwsLambdaProperties} used to create AWS Lambda Function
     * @return AmazonWebServiceResult Details about a function's configuration.
     * @throws IOException in the event reading JAR of Lambda code information does not work properly
     */
    public AmazonWebServiceResult createOrUpdateLambdaFunction(final AwsLambdaProperties lambdaFunctionProperties) throws IOException {
        // Null sanity check to avoid NPE
        CommonUtil.ifNullThrowException(lambdaFunctionProperties, new AlertServiceException("Cannot Create Lambda Function from Null Object"));

        // Check to see if the function exists, if so, then update it
        if (lambdaFunctionExists(lambdaFunctionProperties.getFunctionName())) {
            System.out.println("UPDATING LAMBDA");
            // if the lambda is not updated, i.e. a non HTTP 200 response was returned then throw an exception
            if (!updateLambda(lambdaFunctionProperties)) {
                throw new AlertServiceException("Cannot update AWS Lambda " + lambdaFunctionProperties.getFunctionName());
            }
            return getLambdaFunction(lambdaFunctionProperties.getFunctionName());
        } else {
            return createFunction(lambdaFunctionProperties);
        }
    }

    /**
     * Retrieves the specified Lambda function
     *
     * @param functionName String function to retrieve
     * @return GetFunctionResult Returns information about the function or function version, with a link to download
     * the deployment package that's valid for 10 minutes.
     */
    public GetFunctionResult getLambdaFunction(final String functionName) {
        return awsLambda.getFunction(new GetFunctionRequest().withFunctionName(functionName));
    }

    /**
     * Checks known AWS Lambda functions to see if the provided function exists or not.
     *
     * @param functionName String AWS Lambda function to check existence of
     * @return boolean true if the function exists, false otherwise
     */
    public boolean lambdaFunctionExists(final String functionName) {
        // Sanity check
        CommonUtil.ifEmptyThrowException(functionName, new AlertServiceException("Cannot check existence of Lambda Function with null/empty name"));
        ListFunctionsResult listFunctionsResult = listLambdaFunctions();
        return Objects.nonNull(CollectionUtil.firstOrNull(listFunctionsResult.getFunctions(), (func) -> func.getFunctionName().equals(functionName)));
    }

    /**
     * Deletes a AWS Lambda function by name
     *
     * @param functionName String function name to delete
     * @return DeleteFunctionResult result of Lambda function deletion
     */
    public DeleteFunctionResult deleteLambdaFunction(final String functionName) {
        // Instantiate initial delete function request
        DeleteFunctionRequest deleteFunctionRequest = new DeleteFunctionRequest().withFunctionName(functionName);
        // Execute delete request and return the result
        return awsLambda.deleteFunction(deleteFunctionRequest);
    }

    /**
     * Deletes all functions known to AWS.
     *
     * @return Collection of {@link DeleteFunctionResult} results from Lambda function deletion
     */
    public Collection<DeleteFunctionResult> deleteAllLambdaFunctions() {
        // Get a list of AWS Lambda function names and transform to list
        List<String> names = CollectionUtil.listOf(getLambdaFunctionNames());
        // Delete functions by name and add results to list
        return names.stream().map(this::deleteLambdaFunction).collect(Collectors.toList());
    }

    /**
     * Creates an S3 bucket notification configuration using the underlying {@link AwsLambdaProperties} that is injected
     * into this class.
     *
     * @param lambdaFunctionProperties {@link AwsLambdaProperties} used to create notification configuration
     * @return BucketNotificationConfiguration Represents a bucket's notification configuration. The notification
     * configuration is used to control reception of notifications for specific events for Amazon S3 buckets.
     */
    BucketNotificationConfiguration bucketNotificationConfiguration(final AwsLambdaProperties lambdaFunctionProperties) {
        // Instantiate bucket config object
        BucketNotificationConfiguration bucketNotificationConfiguration = new BucketNotificationConfiguration();
        // Create ARN String representation
        final String lambdaArn = LAMBDA_ARN_BASE + lambdaFunctionProperties.getFunctionName();
        // Build configuration and return
        LambdaConfiguration lambdaConfiguration = new LambdaConfiguration(lambdaArn, EnumSet.of(S3Event.ObjectCreated));
        bucketNotificationConfiguration.addConfiguration(LAMBDA_CONFIG_NAME, lambdaConfiguration);

        return bucketNotificationConfiguration;
    }

    /**
     * Getter to expose/pass along the Weather Alert Lambda Properties to other classes
     *
     * @return AwsLambdaProperties simple object to hold AWS Lambda related information
     */
    AwsLambdaProperties getWeatherAlertLambdaProperties() {
        return lambdaProperties;
    }

    /**
     * Helper method to update both an AWS Lambdas configuration and underlying code in a single call. The HTTP
     * response codes are then evaluated to see if the updates were successful or not.
     *
     * NOTE: This method assumes that you have checked that the function exists beforehand!
     *
     * @param lambdaFunctionProperties {@link AwsLambdaProperties} used to update an AWS Lambda.
     * @return boolean TRUE then the updates were a success, FALSE otherwise
     * @throws IOException if issues occur when building byte buffer of code for AWS Lambda code update
     */
    private boolean updateLambda(final AwsLambdaProperties lambdaFunctionProperties) throws IOException {
        // Null sanity check to avoid NPE
        CommonUtil.ifNullThrowException(lambdaFunctionProperties, new AlertServiceException("Cannot Create Lambda Function from Null Object"));
        // Update Lambda config
        AmazonWebServiceResult configUpdate = updateLambdaConfiguration(lambdaFunctionProperties);
        // Update Lambda code
        AmazonWebServiceResult lambdaCodeUpdate = updateLambdaCode(lambdaFunctionProperties);
        // Check the response code of each response to ensure everything went well
        return configUpdate.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.OK.value()
                && lambdaCodeUpdate.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.OK.value();
    }

    /**
     * Updates the underlying AWS Lambda configuration using the provided {@link AwsLambdaProperties}.
     *
     * @param lambdaFunctionProperties {@link AwsLambdaProperties} used to update AWS Lambda Configuration
     * @return AmazonWebServiceResult of the UpdateFunctionConfiguration operation returned by the service.
     */
    private AmazonWebServiceResult updateLambdaConfiguration(final AwsLambdaProperties lambdaFunctionProperties) {
        UpdateFunctionConfigurationRequest updateFunctionConfigurationRequest = new UpdateFunctionConfigurationRequest()
                .withFunctionName(lambdaFunctionProperties.getFunctionName())
                .withHandler(lambdaFunctionProperties.getHandler())
                .withRuntime(Runtime.Java8)
                .withRole(lambdaFunctionProperties.getRole())
                .withTimeout(lambdaFunctionProperties.getTimeout());
        return awsLambda.updateFunctionConfiguration(updateFunctionConfigurationRequest);
    }

    /**
     * Updates the underlying AWS Lambda code using the provided {@link AwsLambdaProperties} to set things up.
     *
     * @param lambdaFunctionProperties {@link AwsLambdaProperties} used to update AWS Lambda Code
     * @return AmazonWebServiceResult of the UpdateFunctionCode operation returned by the service.
     * @throws IOException if issues occur while creating JAR byte buffer
     */
    private AmazonWebServiceResult updateLambdaCode(final AwsLambdaProperties lambdaFunctionProperties) throws IOException {
        // Create byte buffer of JVM Lambda code
        ByteBuffer byteBuffer = ByteBuffer.wrap(Files.readAllBytes(new File(lambdaFunctionProperties.getJarPath()).toPath()));
        // Instantiate update code request
        UpdateFunctionCodeRequest updateFunctionCodeRequest = new UpdateFunctionCodeRequest()
                .withFunctionName(lambdaFunctionProperties.getFunctionName())
                .withZipFile(byteBuffer);
        // Update function code
        return awsLambda.updateFunctionCode(updateFunctionCodeRequest);
    }

}
