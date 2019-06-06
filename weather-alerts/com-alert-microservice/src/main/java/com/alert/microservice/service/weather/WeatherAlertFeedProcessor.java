package com.alert.microservice.service.weather;

import com.alert.microservice.api.WeatherAlert;
import com.alert.microservice.service.weather.exception.AlertProcessorException;
import com.alert.microservice.util.TransformUtil;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.ParsingFeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class handles transforming Weather Alert Atom feed data into a objects that the application can more easily
 * interface with.
 */
public class WeatherAlertFeedProcessor {
    // Class constants
    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String SUMMARY_FIELD = "summary";
    private static final String UPDATED_DATE_FIELD = "updatedDate";

    // Final variables must be set in the constructor
    private final String url;
    private final Set<String> capFieldSet;
    private final boolean dataFromFile;

    /**
     * Constructor for a {@link WeatherAlertFeedProcessor} this defaults to retrieving the data feed of alerts
     * from the web and NOT a file.
     *
     * @param url         String Atom feed URL
     * @param capFieldSet Set of String fields that must be extracted explicitly
     */
    public WeatherAlertFeedProcessor(String url, Set<String> capFieldSet) {
        this(url, capFieldSet, false);
    }

    /**
     * Constructor for a {@link WeatherAlertFeedProcessor}
     *
     * @param url         String Atom feed URL or path to file to process
     * @param capFieldSet Set of String fields that must be extracted explicitly
     * @param dataFromFile boolean that states if the data to be pulled in is from
     *                     a file or should be fetched from the web
     */
    public WeatherAlertFeedProcessor(String url, Set<String> capFieldSet, boolean dataFromFile) {
        this.url = url;
        this.dataFromFile = dataFromFile;
        this.capFieldSet = capFieldSet;
    }

    /**
     * Pulls in Atom Weather Alert feed and transforms it into a Collection of {@link WeatherAlert} objects.
     *
     * @return Collection of {@link WeatherAlert} objects parsed from Atom feed
     * @throws AlertProcessorException if something bad happens when processing the data
     */
    public Collection<WeatherAlert> process() throws AlertProcessorException {
        try {
            return convertFeedToWeatherAlerts(createDataFeed(url));
        } catch (ParsingFeedException parsingFeedException) {
            throw new AlertProcessorException("Cannot Parse Alert Feed! Unable to process Weather Alerts", parsingFeedException);
        } catch (Exception ex) {
            throw new AlertProcessorException("Unable to process Weather Alerts", ex);
        }
    }

    /**
     * Helper method to convert entries contained in the provided {@link SyndFeed} into a Collection
     * of {@link WeatherAlert} objects for easier processing.
     *
     * @param feed SyndFeed interface for all types of feeds from weather alert feed
     * @return Collection of {@link WeatherAlert} data extracted from feed entries
     */
    private Collection<WeatherAlert> convertFeedToWeatherAlerts(SyndFeed feed) {
        return feed.getEntries().stream()
                .map(this::processSyndEntry)
                .collect(Collectors.toList());
    }

    /**
     * Constructs a {@link SyndFeed} from the provided URL
     *
     * @param url String to build feed
     * @return SyndFeed interface for all types of feeds
     * @throws IOException
     * @throws FeedException
     */
    private SyndFeed createDataFeed(final String url) throws IOException, FeedException {
        SyndFeedInput input = new SyndFeedInput();
        // If the input is from a file then read it into the synd feed
        if (dataFromFile) {
            return input.build(new File(url));
        }
        // Build URL to resource since we'll need to fetch it from the web
        // then retrieve the feed
        return input.build(new XmlReader(new URL(url)));
    }

    /**
     * Transforms a {@link SyndEntry} into a {@link WeatherAlert} by extracting relevant data from the entry and
     * populating it into the return object.
     *
     * @param entry Bean interface for entries of a {@link SyndFeed}
     * @return WeatherAlert constructed from the entry
     */
    private WeatherAlert processSyndEntry(final SyndEntry entry) {
        Map<String, Object> objectMap = processEntryToMap(entry);
        // Convert map to alert
        WeatherAlert weatherAlert = TransformUtil.convert(objectMap, WeatherAlert.class);
        // Process active flag
        Date expiresDate = weatherAlert.getExpires();
        weatherAlert.setActive(Objects.nonNull(expiresDate) && expiresDate.after(new Date()));
        // return value
        return weatherAlert;
    }

    /**
     * Pulls out base level and "cap" elements contained in the provided {@link SyndEntry} to be processed
     * later by Jackson to be transformed into an object.
     *
     * @param entry Bean interface for entries of a {@link SyndFeed} to extract Map of values from
     * @return Map of String keys to their corresponding object value pulled from the provided entry.
     */
    private Map<String, Object> processEntryToMap(final SyndEntry entry) {
        Map<String, Object> objectMap = new HashMap<>();
        // Extract ID from the URI which is the value after the last period in the entry URI
        String id = entry.getUri().substring(entry.getUri().lastIndexOf(".") + 1);
        // Set base information that we can grab from the entry directly and put in map
        objectMap.put(ID_FIELD, id);
        objectMap.put(TITLE_FIELD, entry.getTitle());
        objectMap.put(SUMMARY_FIELD, entry.getDescription().getValue());
        objectMap.put(UPDATED_DATE_FIELD, entry.getUpdatedDate());

        // Loop through foreign markup and retrieve relevant alert fields
        entry.getForeignMarkup().stream()
                .filter(elm -> capFieldSet.contains(elm.getName()))
                .forEach(elm -> objectMap.put(elm.getName(), elm.getValue()));

        return objectMap;
    }
}
