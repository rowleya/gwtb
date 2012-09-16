package com.googlecode.gwtb.test.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.UnsafeNativeLong;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.googlecode.gwtb.generator.gwt.JSONSerializer;

public class Test implements EntryPoint {

    @Override
    public void onModuleLoad() {
        JSONSerializer<TestObject> serializer = GWT.create(TestObject.class);
        TestObject object = new TestObject();
        GWT.log("Value: " + object.getTestLong());
        long value = getTestObjectLong(object);
        GWT.log("Value: " + value);
        setTestObjectLong(object, 12345);
        GWT.log("Value: " + object.getTestLong());
    }

    @UnsafeNativeLong
    public native long getTestObjectLong(TestObject testObject)/*-{
        return testObject.@com.googlecode.gwtb.test.client.TestObject::testLong;
    }-*/;

    @UnsafeNativeLong
    public native void setTestObjectLong(TestObject testObject, long value) /*-{
        testObject.@com.googlecode.gwtb.test.client.TestObject::testLong = value;
    }-*/;
}
