package com.alert.microservice.service.exception;

import org.junit.Assert;
import org.junit.Test;

public class AlertServiceExceptionTest {

    private static final String DEFAULT_MSG = "ERROR!";
    @Test
    public void testConstructors() {
        // String message constructor
        AlertServiceException alertServiceException = new AlertServiceException(DEFAULT_MSG);
        Assert.assertEquals(DEFAULT_MSG, alertServiceException.getMessage());
        // Throwable constructor
        alertServiceException = new AlertServiceException(new RuntimeException(DEFAULT_MSG));
        Assert.assertTrue(alertServiceException.getMessage().contains(DEFAULT_MSG));
        // Combo constructor
        alertServiceException = new AlertServiceException(DEFAULT_MSG, new RuntimeException(DEFAULT_MSG));
        Assert.assertEquals(DEFAULT_MSG, alertServiceException.getMessage());
    }
}
