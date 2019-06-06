package com.alert.microservice.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility class to help with common {@link Collection} operations
 * <p>
 * Annotations Explained:
 *
 * <ul>
 * <li>
 * SafeVarargs = A programmer assertion that the body of the annotated method or constructor does not perform
 * potentially unsafe operations on its varargs parameter.
 * </li>
 * </ul>
 */
public class CollectionUtil {

    /**
     * Checks the provided array to determine if it is null or empty.
     *
     * @param array of type T to check if null or empty
     * @param <T>   Generic Type
     * @return boolean true if the array is null or empty, false otherwise
     */
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length <= 0;
    }

    /**
     * Checks the provided array to determine if it is NOT null or empty.
     *
     * @param array of type T to check if NOT null or empty
     * @param <T>   Generic Type
     * @return boolean true if the array is NOT null or empty, false otherwise
     */
    public static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
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

    /**
     * Will either retrieve the first element in the provided {@link Collection} if it is populated with data.
     * Otherwise, it will return a null object.
     *
     * @param collection Collection of any type
     * @param <T>        Generic Type T
     * @return T or null if the collection is null/empty or nothing matches the provided predicate
     */
    public static <T> T firstOrNull(final Collection<T> collection) {
        return firstOrNull(collection, Objects::nonNull);
    }

    /**
     * Will either retrieve the first element in the provided {@link Collection} if it is populated with data and filter
     * it with the provided {@link Predicate}. Otherwise, it will return a null object.
     *
     * @param collection Collection of any type
     * @param predicate  Represents a predicate (boolean-valued function) of one argument.
     * @param <T>        Generic Type T
     * @return T or null if the collection is null/empty or nothing matches the provided predicate
     */
    public static <T> T firstOrNull(final Collection<T> collection, final Predicate<T> predicate) {
        // Check that the predicate is there, if not, return null
        if (Objects.isNull(predicate)) {
            return null;
        }
        // If we have a populated collection then process, otherwise, return null
        return isNotEmpty(collection) ? collection.stream().filter(predicate).findFirst().orElse(null) : null;
    }

    /**
     * Removes all null values from the provided Collection. If a null object is passed into the method then a
     * empty List is returned.
     *
     * @param collection Collection of any type
     * @param <T> Generic Type T
     * @return Collection of non-null T objects, if the Collection is null a empty List will be returned
     */
    public static <T> List<T> removeNulls(final Collection<T> collection) {
        if(Objects.isNull(collection)) {
            return listOf();
        } else {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Removes all null values from the provided Collection. If a null object is passed into the method then a
     * empty Set is returned.
     *
     * @param collection Collection of any type
     * @param <T> Generic Type T
     * @return Collection of non-null T objects, if the Collection is null a empty Set will be returned
     */
    public static <T> Set<T> removeNullsAndDuplicates(final Collection<T> collection) {
        if(Objects.isNull(collection)) {
            return setOf();
        } else {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Returns a mutable {@link List} of values that are passed into the method.
     *
     * @param values 1..N T values to append to result List
     * @param <T>    the type of the elements of the List
     * @return List of T objects built from the values parameter
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... values) {
        return Objects.nonNull(values) ? new ArrayList<>(Arrays.asList(values)) : new ArrayList<>();
    }

    /**
     * Returns a mutable {@link List} from the provided {@link Collection} provided to the method.
     *
     * @param collection Nullable {@link Collection} to transform to a list
     * @param <T>    the type of the elements of the List
     * @return List of T objects built from the collection parameter
     */
    public static <T> List<T> listOf(Collection<T> collection) {
        return Objects.nonNull(collection) ? new ArrayList<>(collection) : new ArrayList<>();
    }

    /**
     * Returns a {@link Set} of values that are passed into the method.
     *
     * @param values 1..N T values to append to result Set
     * @param <T>    the type of the elements to append to Set
     * @return Set of T objects built from the values parameter
     */
    @SafeVarargs
    public static <T> Set<T> setOf(T... values) {
        return Objects.nonNull(values) ? new HashSet<>(Arrays.asList(values)) : new HashSet<>();
    }

    /**
     * Returns a mutable {@link Set} from the provided {@link Collection} provided to the method.
     *
     * @param collection Nullable {@link Collection} to transform to a Set
     * @param <T>    the type of the elements of the Set
     * @return Set of T objects built from the collection parameter
     */
    public static <T> Set<T> setOf(Collection<T> collection) {
        return Objects.nonNull(collection) ? new HashSet<>(collection) : new HashSet<>();
    }

    /**
     * Returns the size of the collection in a safe manner by checking for nullability/emptiness explicitly
     * before retrieving the size.
     *
     * @param collection Collection to pull size information from
     * @param <T>        Generic Type T contained in the Collection
     * @return int the size of the provided Collection
     */
    public static <T> int size(Collection<T> collection) {
        return isNotEmpty(collection) ? collection.size() : 0;
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
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), parallel);
    }
}
