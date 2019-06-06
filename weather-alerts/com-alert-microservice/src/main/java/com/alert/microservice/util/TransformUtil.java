package com.alert.microservice.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class provides useful methods to Transform data in a application to help ease development when transforming data.
 *
 * @author Jack Phillips
 */
public class TransformUtil {
    /**
     * Private default constructor
     */
    private TransformUtil() {
        // Making static code analysis happy with a constructor comment
    }

    /**
     * Parses the provided Object to its String representation using the Jackson library.
     *
     * @param object T to parse to String
     * @param <T>    Generic Type
     * @return String if the object is populated, null if not
     * @throws JsonProcessingException Intermediate base class for all problems encountered when processing
     * (parsing, generating) JSON content
     */
    public static <T> String writeAsJSONString(T object) throws JsonProcessingException {
        if (Objects.isNull(object)) {
            return null;
        }
        return mapper().writeValueAsString(object);
    }

    /**
     * Helper method to create a ObjectMapper used to parse data from different sources.
     *
     * @return ObjectMapper provides functionality for reading and writing JSON, either to and from basic
     * POJOs (Plain Old Java Objects), or to and from a general-purpose JSON Tree Model, as well as related
     * functionality for performing conversions.
     */
    public static ObjectMapper mapper() {
        return new ObjectMapper();
    }

    /**
     * Converts a Object to another instance, ex. a Map to a POJO.
     *
     * @param object Object to convert to specified Class
     * @param clazz Class to convert the Object too
     * @param <T> Generic Type
     * @return T converted from the provided Object
     */
    public static <T> T convert(Object object, Class<T> clazz) {
        // Null sanity checks
        CommonUtil.ifNullThrowException(object, new NullPointerException("Cannot convert null object!"));
        CommonUtil.ifNullThrowException(clazz, new NullPointerException("Cannot convert object to null class!"));
        return mapper().convertValue(object, clazz);
    }

    /**
     * Builds a Map of Key K and Value V from the provided entity T
     *
     * @param entity T to create field name to field value map from
     * @param <K> Generic Type K that corresponds to the Map Key type
     * @param <V> Generic Type V that corresponds to the Map Value type
     * @param <T> Generic Type T that corresponds to the entity type
     * @return Map of entity key fields K to entity field value V
     */
    public static <K, V, T> Map<K, V> mapEntity(final T entity) {
        // Build type reference to map
        TypeReference<HashMap<K, V>> mapTypeReference = new TypeReference<HashMap<K, V>>() {};
        // Instantiate object mapper to convert entity to a map of field names -> field values
        // then convert values in entity to map of K keys to V values
        return mapper().convertValue(entity, mapTypeReference);
    }
}