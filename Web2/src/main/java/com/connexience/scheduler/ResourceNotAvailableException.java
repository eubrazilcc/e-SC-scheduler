package com.connexience.scheduler;

/**
 * Created by Jacek on 15/12/2015.
 */
public class ResourceNotAvailableException extends SchedulerException
{
    public ResourceNotAvailableException() {
        super();
    }

    public ResourceNotAvailableException(String message) {
        super(message);
    }

    public ResourceNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceNotAvailableException(String messageFormat, Object... args) {
        super(String.format(messageFormat, args));
    }

    public ResourceNotAvailableException(Throwable cause) {
        super(cause);
    }

    protected ResourceNotAvailableException(String message, Throwable cause,
                        boolean enableSuppression,
                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
