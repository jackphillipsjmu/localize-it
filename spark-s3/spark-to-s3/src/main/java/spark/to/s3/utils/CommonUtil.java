package spark.to.s3.utils;

import java.util.Objects;
import java.util.Optional;

/**
 * Class provides useful methods to help ease development. If more verbose functionality is needed
 * please refer to Apache's common util packages.
 *
 * @author Jack Phillips
 */
public class CommonUtil {
    /**
     * Private default constructor, singleton.
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
     * Determines if the provided String is null or empty.
     *
     * @param str String to evaluate
     * @return boolean true if the provided String is null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Helper method to retrieve system properties defaulting to the default String parameter if nothing is found.
     *
     * @param key String environment variable key to look for
     * @param defaultStr String the default value if nothing is set for the provided variable key
     * @return String either the environment variable or default String if nothing is found.
     */
    public static String retrieveSystemProperty(final String key, final String defaultStr) {
        return Optional.ofNullable(System.getProperty(key)).orElse(defaultStr);
    }
}
