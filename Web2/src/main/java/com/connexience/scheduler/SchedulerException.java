package com.connexience.scheduler;

/**
 * Created by Jacek on 15/12/2015.
 */
public class SchedulerException extends Exception
{
    public SchedulerException() {
        super();
    }

    public SchedulerException(String message) {
        super(message);
    }

    public SchedulerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchedulerException(String messageFormat, Object... args) {
        super(String.format(messageFormat, args));
    }

    public SchedulerException(Throwable cause) {
        super(cause);
    }

    protected SchedulerException(String message, Throwable cause,
                                            boolean enableSuppression,
                                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
