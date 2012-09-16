package com.googlecode.gwtb.test.client;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.googlecode.gwtb.gwt.JAXBSerializable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TestObject implements JAXBSerializable {

    private double testDouble = Double.MAX_VALUE;

    private float testFloat = Float.MAX_VALUE;

    private long testLong = Long.MAX_VALUE;

    private int testInt = Integer.MAX_VALUE;

    private short testShort = Short.MAX_VALUE;

    private byte testByte = Byte.MAX_VALUE;

    private String testString = "TestString";

    private char testChar = 'a';

    private TestOtherObject testObject = new TestOtherObject("test");

    private String[] testArray = new String[]{"a", "b", "c"};

    private List<TestOtherObject> testCollection =
            new ArrayList<TestOtherObject>();
    {
        testCollection.add(new TestOtherObject("test1"));
        testCollection.add(new TestOtherObject("test2"));
    }

    public double getTestDouble() {
        return testDouble;
    }

    public float getTestFloat() {
        return testFloat;
    }

    public long getTestLong() {
        return testLong;
    }

    public int getTestInt() {
        return testInt;
    }

    public short getTestShort() {
        return testShort;
    }

    public byte getTestByte() {
        return testByte;
    }

    public String getTestString() {
        return testString;
    }

    public char getTestChar() {
        return testChar;
    }

    public TestOtherObject getTestObject() {
        return testObject;
    }

    public String[] getTestArray() {
        return testArray;
    }

    public List<TestOtherObject> getTestCollection() {
        return testCollection;
    }
}
