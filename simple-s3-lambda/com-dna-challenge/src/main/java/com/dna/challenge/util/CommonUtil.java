package com.dna.challenge.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Class provides useful methods to help ease development. If more verbose functionality is needed
 * please refer to Apache's common util packages.
 *
 * Annotations Explained:
 * <ul>
 *     <li>
 *         SafeVarargs = A programmer assertion that the body of the annotated method or constructor does not perform
 *         potentially unsafe operations on its varargs parameter.
 *     </li>
 * </ul>
 *
 *
 * @author Jack Phillips
 */
public class CommonUtil {
    /**
     * Private default constructor
     */
    private CommonUtil() {
        // Private constructor to keep static code analysis happy
    }

    @SafeVarargs
    public static <T> boolean isAnyNull(T ... objects) {
        return Objects.isNull(objects) || Arrays.stream(objects).anyMatch(Objects::isNull);
    }

    /**
     * Checks the provided left hand argument to see if it is null, if so, then it will return the default value,
     * otherwise return the left hand argument.
     *
     * @param lhsObject T to validate
     * @param defaultObject T default object to return if left hand object is null
     * @param <T> Generic Type
     * @return T the left hand variable if not null, default object otherwise
     */
    public static <T> T defaultIfNull(T lhsObject, T defaultObject) {
        return Objects.nonNull(lhsObject) ? lhsObject : defaultObject;
    }

    /**
     * Throws exception if the provided left hand object is null.
     *
     * @param object T to check for null
     * @param throwable E throwable error to invoke if left hand object is null
     * @param <T> Generic Type of the left hand object
     * @param <E> Child class of Throwable
     * @return <T> object that will be returned if not null
     * @throws E Generic Exception to throw if the provided object is null
     */
    public static <T, E extends Throwable> T ifNullThrowException(T object, E throwable) throws E {
        if (Objects.isNull(object)) {
            throw throwable;
        }
        return object;
    }

    /**
     * If the provided left hand String is null or empty then the default String is returned, otherwise, the
     * left hand String is returned.
     *
     * @param str String to check if its null or empty
     * @param defaultStr String to default to if left hand String is empty or null
     * @return String either the populated left hand String or default String
     */
    public static String defaultIfNullOrEmpty(final String str, final String defaultStr) {
        return isNullOrEmpty(str) ? defaultStr : str;
    }

    /**
     * Determines if the provided String can be be sliced or not, if so, it will return the substring, otherwise it
     * will return the original String value.
     *
     * @param size Integer end index, exclusive
     * @param str String to retrieve substring value
     * @return String that is either the substring value or original value
     */
    public static String safeSubstring(final Integer size, final String str) {
        // If the string is null/empty or the size is less than whats provided return
        // the provided String, otherwise, slice it to the passed in size
        return (isNullOrEmpty(str) || str.length() < size) ? str : str.substring(0, size);
    }

    /**
     * Determines if the provided String is null or empty.
     *
     * @param str String to evaluate
     * @return boolean true if the provided String is null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Parses the provided Object to its String representation using Jackson
     *
     * @param object T to parse to String
     * @param <T> Generic Type
     * @return String if the object is populated, null if not
     * @throws JsonProcessingException Intermediate base class for all problems encountered when processing
     * (parsing, generating) JSON content
     */
    public static <T> String writeAsJSONString(T object) throws JsonProcessingException {
        if (Objects.isNull(object)) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Determines if the provided String is NOT null or empty.
     *
     * @param str String to evaluate
     * @return boolean true if the provided String is null or empty, false otherwise
     */
    public static boolean isNotEmpty(final String str) {
        return !isNullOrEmpty(str);
    }

    /**
     * Convert Iterator to Non-Parallel Stream.
     *
     * @param iterator The source of data of type T
     * @param <T>      Generic Type T.
     * @return Stream of Type T.
     */
    public static <T> Stream<T> streamOn(Iterator<T> iterator) {
        return streamOn(iterator, false);
    }

    /**
     * Convert Iterator to Stream.
     *
     * @param iterator The source of data of type T
     * @param parallel Determines if the Stream is Parallel or not.
     * @param <T>      Generic Type T.
     * @return Stream of Type T.
     */
    public static <T> Stream<T> streamOn(Iterator<T> iterator, boolean parallel) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                parallel
        );
    }

    /**
     * Obtains the first element from a List if it is not empty. If there are no elements
     * then a null value is returned.
     *
     * @param <T> Generic Object to collect
     * @return Collector to extract first element of list if presents, otherwise, it will
     * return null.
     */
    public static <T> Collector<T, ?, T> singletonCollector() {
        return singletonCollector(null);
    }

    /**
     * Obtains the first element from a List if it is not empty. If there are no elements
     * then the provided default value is returned.
     *
     * @param defaultValue of generic type T to be returned in the event the Collection is empty.
     * @param <T>          Generic Object to collect
     * @return Collector to extract first element of list if presents, otherwise, it will
     * return the provided default value which can potentially be null.
     */
    public static <T> Collector<T, ?, T> singletonCollector(T defaultValue) {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (isNotEmpty(list)) {
                        return list.get(0);
                    }
                    return defaultValue;
                }
        );
    }

    /**
     * Inspects the provided {@link Collection} to determine if it is empty.
     *
     * @param collection Collection of any type
     * @return boolean true if the Collection is empty, false otherwise
     */
    public static boolean isEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Inspects the provided {@link Collection} to determine if it is NOT empty.
     *
     * @param collection Collection of any type
     * @return boolean true if the Collection is NOT empty, false otherwise
     */
    public static boolean isNotEmpty(final Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static <T> boolean isEmpty(T[] array) {
        return Objects.isNull(array) || array.length == 0;
    }

    /**
     * Helper method to retrieve environment variables defaulting to the default String parameter if nothing is found.
     *
     * @param key String environment variable key to look for
     * @param defaultStr String the default value if nothing is set for the provided variable key
     * @return String either the environment variable or default String if nothing is found.
     */
    public static String retrieveEnvironmentVariable(final String key, final String defaultStr) {
        return Optional.ofNullable(System.getenv(key)).orElse(defaultStr);
    }
}
