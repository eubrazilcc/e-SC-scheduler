package com.connexience.scheduler;

/**
 * Created by Jacek on 15/12/2015.
 */
public class AlreadyRegisteredException extends SchedulerException
{
    public AlreadyRegisteredException() {
        super();
    }

    public AlreadyRegisteredException(String message) {
        super(message);
    }

    public AlreadyRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlreadyRegisteredException(String messageFormat, Object... args) {
        super(String.format(messageFormat, args));
    }

    public AlreadyRegisteredException(Throwable cause) {
        super(cause);
    }

    protected AlreadyRegisteredException(String message, Throwable cause,
                                            boolean enableSuppression,
                                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

