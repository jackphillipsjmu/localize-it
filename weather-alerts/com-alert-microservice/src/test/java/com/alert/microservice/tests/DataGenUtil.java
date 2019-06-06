package com.alert.microservice.tests;

import com.alert.microservice.api.WeatherAlert;
import com.amazonaws.services.s3.model.Bucket;

import java.time.LocalDate;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class to help with data generation for tests
 */
public class DataGenUtil {
    // category = [Met]
    // severity = [Moderate, Unknown, Minor, Severe]
    // urgency = [Expected, Unknown, Immediate, Future]
    public static final String[] TITLES = {"Special Weather Statement", "Flood Warning", "Rip Current Statement"};
    public static final String[] CATEGORIES = {"Met", "Unknown"};
    public static final String[] CERTAINTIES = {"Possible", "Likely", "Observed", "Unknown"};

    public static final String[] SEVERITIES = {"Minor", "Moderate", "Severe", "Unknown"};
    public static final String[] URGENCIES = {"Expected", "Immediate", "Future", "Unknown"};
    public static final String[] AREA_DESCRIPTIONS = {"Palo Alto", "Madison; Warren", "Humboldt", "Arlington; VA", "N/A"};

    /**
     * Creates a randomized {@link WeatherAlert} for testing
     *
     * @return WeatherAlert with random values set in its class member fields
     */
    public static WeatherAlert randomWeatherAlert() {
        WeatherAlert weatherAlert = new WeatherAlert();
        weatherAlert.setId(randomId());
        weatherAlert.setTitle(randomString(TITLES));
        weatherAlert.setCategory(randomString(CATEGORIES));
        weatherAlert.setAreaDesc(randomString(AREA_DESCRIPTIONS));
        weatherAlert.setSeverity(randomString(SEVERITIES));
        weatherAlert.setActive(randomBoolean());
        weatherAlert.setUrgency(randomString(URGENCIES));
        weatherAlert.setCertainty(randomString(CERTAINTIES));
        weatherAlert.setEffective(randomDate());
        weatherAlert.setUpdatedDate(new Date());
        weatherAlert.setExpires(randomDate());

        return weatherAlert;
    }

    /**
     * Creates a random UUID for testing purposes
     *
     * @return String random UUID
     */
    public static String randomId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Creates a randomized AWS {@link Bucket}
     * @return AWS Bucket for testing
      */
    public static Bucket randomBucket() {
        return new Bucket(randomId());
    }

    /**
     * Selects a random value from the provided array
     *
     * @param array String array
     * @return String that is pulled from the provided array
     */
    public static String randomString(String[] array) {
        return array[ThreadLocalRandom.current().nextInt(0, array.length)];
    }

    /**
     * Creates a random Boolean
     *
     * @return randomized {@link Boolean}
     */
    public static Boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /**
     * Creates a random {@link Date}
     *
     * @return random Date
     */
    public static Date randomDate() {
        Random random = new Random();
        int minDay = (int) LocalDate.of(1900, 1, 1).toEpochDay();
        int maxDay = (int) LocalDate.of(2015, 1, 1).toEpochDay();
        return new Date(minDay + random.nextInt(maxDay - minDay));
    }
}
