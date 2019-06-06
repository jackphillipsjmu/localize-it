package com.alert.microservice.util;

import java.util.Objects;

/**
 * Class provides useful methods to help ease development. If more verbose functionality is needed
 * please refer to Apache's common util packages.
 *
 * Annotations Explained:
 *
 * <ul>
 *     <li>
 *         SafeVarargs = A programmer assertion that the body of the annotated method or constructor does not perform
 *         potentially unsafe operations on its varargs parameter.
 *     </li>
 * </ul>
 */
public class CommonUtil {
    /**
     * Private default constructor
     */
    private CommonUtil() {
        // Private constructor to keep static code analysis happy
    }

    /**
     * Throws exception if the provided left hand object is null.
     *
     * @param object T to check for null
     * @param throwable E throwable error to invoke if left hand object is null
     * @param <T> Generic Type of the left hand object
     * @param <E> Child class of Throwable
     * @throws E Generic Exception to throw if the provided object is null
     */
    public static <T, E extends Throwable> void ifNullThrowException(T object, E throwable) throws E {
        if (Objects.isNull(object)) {
            throw throwable;
        }
    }

    /**
     * Throws exception if the provided String is null or empty.
     *
     * @param str String to evaluate
     * @param throwable E throwable error to invoke if String is empty
     * @param <E> Child class of Throwable
     * @throws E Generic Exception to throw if String is empty
     */
    public static <E extends Throwable> void ifEmptyThrowException(String str, E throwable) throws E {
        if (isNullOrEmpty(str)) {
            throw throwable;
        }
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
     * Determines if the provided String is null or empty.
     *
     * @param str String to evaluate
     * @return boolean true if the provided String is null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty();
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
}
