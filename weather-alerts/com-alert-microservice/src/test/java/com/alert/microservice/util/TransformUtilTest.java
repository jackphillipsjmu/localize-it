package com.alert.microservice.util;

import com.alert.microservice.api.WeatherAlert;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.alert.microservice.util.TransformUtil.convert;
import static com.alert.microservice.util.TransformUtil.mapEntity;
import static com.alert.microservice.util.TransformUtil.mapper;
import static com.alert.microservice.util.TransformUtil.writeAsJSONString;

public class TransformUtilTest {

    @Test
    public void testWriteAsJSONString() throws JsonProcessingException {
        Assert.assertNull(writeAsJSONString(null));
        WeatherAlert weatherAlert = new WeatherAlert();
        weatherAlert.setId("1");
        Assert.assertEquals("{\"id\":\"1\"}", writeAsJSONString(weatherAlert));
    }

    @Test
    public void testMapper() {
        Assert.assertNotNull(mapper());
        Assert.assertTrue(mapper() instanceof ObjectMapper);
    }

    @Test(expected = NullPointerException.class)
    public void testConvertNPELeftHandParam() {
        convert(null, WeatherAlert.class);
    }

    @Test(expected = NullPointerException.class)
    public void testConvertNPERightHandParam() {
        convert(new WeatherAlert(), null);
    }

    @Test
    public void testConvert() {
        Map<String, String> weatherAlertMap = new HashMap<>();
        weatherAlertMap.put("id", "1");

        WeatherAlert weatherAlert = convert(weatherAlertMap, WeatherAlert.class);
        Assert.assertEquals("1",  weatherAlert.getId());
    }

    @Test
    public void testMapEntity() {
        WeatherAlert weatherAlert = new WeatherAlert();
        weatherAlert.setId("1");
        Map<String, String> map = mapEntity(weatherAlert);
        Assert.assertEquals("1", map.get("id"));
    }
}
