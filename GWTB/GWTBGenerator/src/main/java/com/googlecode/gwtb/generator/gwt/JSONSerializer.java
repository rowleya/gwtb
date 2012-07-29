package com.googlecode.gwtb.generator.gwt;

import com.google.gwt.json.client.JSONObject;

public interface JSONSerializer<T> {

    JSONObject toJSON(T object);

    T fromJSON(JSONObject object);
}
