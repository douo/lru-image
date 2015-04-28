/*
 * Copyright (c) 2015. Tiou Lims, All rights reserved.
 */

package info.dourok.lruimage;

/**
 * Created by DouO on 4/25/15.
 */
public class LruImageException extends Exception {
    private Throwable originalThrowable;
    private String msg;

    public LruImageException(String msg) {
        super();
        this.msg = msg;
    }

    public LruImageException(Throwable e) {
        super();
        originalThrowable = e;
    }

    @Override
    public String getMessage() {
        if (originalThrowable != null) {
            return originalThrowable.getMessage();
        } else {
            return msg;
        }
    }

    @Override
    public void printStackTrace() {
        if (originalThrowable != null) {
            originalThrowable.printStackTrace();
        } else {
            super.printStackTrace();
        }
    }
}
