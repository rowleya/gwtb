package com.googlecode.gwtb.test.client;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.googlecode.gwtb.gwt.JAXBSerializable;

@XmlRootElement
public class TestOtherObject implements JAXBSerializable {

    private String otherObjectString = null;

    private TestOtherObject() {
        // Does Nothing
    }

    public TestOtherObject(String otherObjectString) {
        this();
        this.otherObjectString = otherObjectString;
    }

    @XmlValue
    @XmlID
    public String getOtherObjectString() {
        return otherObjectString;
    }

}
