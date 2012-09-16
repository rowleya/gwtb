package com.googlecode.gwtb.generator.gwt;

import com.google.gwt.json.client.JSONValue;

public class SerializationException extends Exception {

    private static final long serialVersionUID = 1L;

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(String message, JSONValue value) {
        super(message + "\n    " + value.toString());
    }

    public SerializationException(String message, JSONValue value,
            Throwable cause) {
        super(message + "\n    " + value.toString(), cause);
    }

}
