package com.alert.microservice.service;

/**
 * Abstract class to aid in toggleable components such as Service classes.
 */
public abstract class ToggleComponent {
    /**
     * Method to dictate if the component extending this class is enabled or not.
     *
     * @return boolean true if enabled, false otherwise
     */
    public abstract boolean isEnabled();

    /**
     * Method that will check the {@code isEnabled} method to see if the component is enabled. If it is NOT enabled
     * then the exception will be thrown.
     *
     * @param throwable E throwable error to invoke if left hand object is null
     * @param <E> Child class of Throwable
     * @throws E Generic Exception to throw if the provided object is null
     */
    public <E extends Throwable> void ifNotEnabledThrow(E throwable) throws E {
        if (!isEnabled()) {
            throw throwable;
        }
    }
}
