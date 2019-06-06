package com.alert.microservice.service.weather;

import com.alert.microservice.api.WeatherAlert;
import com.alert.microservice.service.weather.exception.AlertProcessorException;
import com.alert.microservice.util.CollectionUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;

public class WeatherAlertFeedProcessorTest {

    private static final String XML_DATA = "src/test/resources/warning_feed.xml";
    private static final String INVALID_DATA = "src/test/resources/invalid_feed.xml";
    private static final String DOES_NOT_EXIST_DATA = "src/test/resources/THIS_SHOULD_NOT_EXIST.xml";
    private static final Set<String> CAP_FIELDS = CollectionUtil.setOf("effective","expires","category","urgency","severity","certainty","areaDesc");

    @Test
    public void testWeatherAlertProcessorFromFile() {
        WeatherAlertFeedProcessor weatherAlertFeedProcessor = new WeatherAlertFeedProcessor(XML_DATA, CAP_FIELDS, true);
        Collection<WeatherAlert> weatherAlertCollection = weatherAlertFeedProcessor.process();
        Assert.assertEquals(406, weatherAlertCollection.size());
    }

    @Test(expected = AlertProcessorException.class)
    public void testInvalidDataAlertProcessorExceptionInvalidData() {
        new WeatherAlertFeedProcessor(INVALID_DATA, CAP_FIELDS, true).process();
    }

    @Test(expected = AlertProcessorException.class)
    public void testInvalidDataAlertProcessorExceptionNoFileData() {
        new WeatherAlertFeedProcessor(DOES_NOT_EXIST_DATA, CAP_FIELDS, true).process();
    }
}
