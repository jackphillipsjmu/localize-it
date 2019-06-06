package com.alert.microservice.service.aws;

import com.alert.microservice.api.AwsLambdaProperties;
import com.alert.microservice.tests.AbstractMockitoTest;
import com.alert.microservice.util.CollectionUtil;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.DeleteFunctionResult;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.GetFunctionRequest;
import com.amazonaws.services.lambda.model.GetFunctionResult;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

public class LambdaServiceTest extends AbstractMockitoTest {

    private static final String DEFAULT_FUNCTION_NAME = "FUNCTION-NAME";

    @Mock
    private AwsLambdaProperties lambdaProperties;

    @Mock
    private AWSLambda awsLambda;

    @InjectMocks
    private LambdaService lambdaService;

    @Test
    public void testListLambdaFunctions() {
        Mockito.when(awsLambda.listFunctions()).thenReturn(defaultListFunctionsResult());
        ListFunctionsResult actualResult = lambdaService.listLambdaFunctions();
        Assert.assertEquals(DEFAULT_FUNCTION_NAME, actualResult.getFunctions().get(0).getFunctionName());
    }

    @Test
    public void testGetLambdaFunction() {
        GetFunctionResult mockResult = new GetFunctionResult();
        mockResult.setConfiguration(defaultFunctionConfiguration());
        Mockito.when(awsLambda.getFunction(Mockito.any(GetFunctionRequest.class))).thenReturn(mockResult);

        GetFunctionResult result = lambdaService.getLambdaFunction(DEFAULT_FUNCTION_NAME);
        Assert.assertEquals(DEFAULT_FUNCTION_NAME,result.getConfiguration().getFunctionName());
    }

    @Test
    public void testLambdaFunctionExists() {
        Mockito.when(awsLambda.listFunctions()).thenReturn(defaultListFunctionsResult());
        Assert.assertTrue(lambdaService.lambdaFunctionExists(DEFAULT_FUNCTION_NAME));
        Assert.assertFalse(lambdaService.lambdaFunctionExists(DEFAULT_FUNCTION_NAME + "-TEST"));
    }

    @Test
    public void testDeleteLambdaFunction() {
        DeleteFunctionResult deleteFunctionResult = lambdaService.deleteLambdaFunction(DEFAULT_FUNCTION_NAME);
        System.out.println(deleteFunctionResult);
    }

    private ListFunctionsResult defaultListFunctionsResult() {
        ListFunctionsResult listFunctionsResult = new ListFunctionsResult();
        listFunctionsResult.setFunctions(CollectionUtil.listOf(defaultFunctionConfiguration()));

        return listFunctionsResult;
    }

    private FunctionConfiguration defaultFunctionConfiguration() {
        FunctionConfiguration functionConfiguration = new FunctionConfiguration();
        functionConfiguration.setFunctionName(DEFAULT_FUNCTION_NAME);
        return functionConfiguration;
    }
}
