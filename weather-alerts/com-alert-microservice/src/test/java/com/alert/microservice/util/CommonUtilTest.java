package com.alert.microservice.util;

import org.junit.Assert;
import org.junit.Test;

import static com.alert.microservice.util.CommonUtil.defaultIfNullOrEmpty;
import static com.alert.microservice.util.CommonUtil.ifEmptyThrowException;
import static com.alert.microservice.util.CommonUtil.ifNullThrowException;
import static com.alert.microservice.util.CommonUtil.isNotEmpty;
import static com.alert.microservice.util.CommonUtil.isNullOrEmpty;

public class CommonUtilTest {

    private static final String DEFAULT_STR = "TEST";
    private static final RuntimeException DEFAULT_EXCEPTION = new RuntimeException("ERROR");

    @Test(expected = RuntimeException.class)
    public void testIfNullThrowException() {
        ifNullThrowException(null, DEFAULT_EXCEPTION);
    }

    @Test
    public void testIfNullThrowExceptionHappyPath() {
        try {
            ifNullThrowException(DEFAULT_STR, DEFAULT_EXCEPTION);
        } catch (Exception ex) {
            Assert.fail("No Exception should be thrown with a non-null object");
        }
    }

    @Test(expected = RuntimeException.class)
    public void testIfEmptyThrowExceptionNull() {
        ifEmptyThrowException(null, DEFAULT_EXCEPTION);
    }

    @Test(expected = RuntimeException.class)
    public void testIfEmptyThrowExceptionEmpty() {
        ifEmptyThrowException("", DEFAULT_EXCEPTION);
    }

    @Test
    public void testIfEmptyThrowExceptionHappyPath() {
        try {
            ifEmptyThrowException(DEFAULT_STR, DEFAULT_EXCEPTION);
        } catch (Exception ex) {
            Assert.fail("No Exception should be thrown with a non-empty String");
        }
    }

    @Test
    public void testDefaultIfNullOrEmpty() {
        Assert.assertEquals(DEFAULT_STR, defaultIfNullOrEmpty(null, DEFAULT_STR));
        Assert.assertEquals(DEFAULT_STR, defaultIfNullOrEmpty("", DEFAULT_STR));
        Assert.assertEquals(DEFAULT_STR, defaultIfNullOrEmpty(DEFAULT_STR, DEFAULT_STR + "-TEST"));
    }

    @Test
    public void testIsNullOrEmpty() {
        Assert.assertTrue(isNullOrEmpty(null));
        Assert.assertTrue(isNullOrEmpty(""));
        Assert.assertFalse(isNullOrEmpty(DEFAULT_STR));
    }

    @Test
    public void testIsNotEmpty() {
        Assert.assertFalse(isNotEmpty(null));
        Assert.assertFalse(isNotEmpty(""));
        Assert.assertTrue(isNotEmpty(DEFAULT_STR));
    }
}
