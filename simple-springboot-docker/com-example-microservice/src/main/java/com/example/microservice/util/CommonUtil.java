package com.example.microservice.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
     * Determines if the provided String is null or empty.
     *
     * @param str String to evaluate
     * @return boolean true if the provided String is null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Returns a {@link Set} of values that are passed into the method.
     *
     * @param values 1..N T values to append to result Set
     * @param <T> the type of the elements to append to Set
     * @return Set of T objects built from the values parameter
     */
    @SafeVarargs
    public static <T> Set<T> setOf(T ... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    /**
     * Inspects the provided {@link Collection} to determine if it is NOT empty.
     *
     * @param collection Collection of any type
     * @return boolean true if the Collection is NOT empty, false otherwise
     */
    public static boolean isNotEmpty(final Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
}
