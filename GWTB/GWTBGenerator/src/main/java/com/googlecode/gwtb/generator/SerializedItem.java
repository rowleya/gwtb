package com.googlecode.gwtb.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.typeinfo.HasAnnotations;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

public class SerializedItem {

    private static final Map<HasAnnotations, SerializedItem> KNOWN_ITEMS =
            new HashMap<HasAnnotations, SerializedItem>();

    private HasAnnotations item = null;

    private XmlValue xmlValue = null;

    private XmlElement xmlElement = null;

    private XmlAttribute xmlAttribute = null;

    private XmlElementRef xmlElementRef = null;

    private XmlElements xmlElements = null;

    private XmlElementRefs xmlElementRefs = null;

    private XmlElementWrapper xmlElementWrapper = null;

    private XmlIDREF xmlIdRef = null;

    private XmlJavaTypeAdapter xmlJavaTypeAdapter = null;

    private XmlTransient xmlTransient = null;

    private SerializedItem objectIdItem = null;

    private String wrapperName = null;

    private String name = null;

    private String defaultValue = null;

    private Boolean nillable = null;

    private Boolean required = null;

    private List<JClassType> types = new ArrayList<JClassType>();

    private JType actualType = null;

    private boolean trans = false;

    private boolean annotated = false;

    private boolean isMethod = false;

    private JMethod setMethod = null;

    private JMethod getMethod = null;

    private JField field = null;

    public static final SerializedItem get(HasAnnotations item,
            TreeLogger logger, TypeOracle typeOracle, JClassType containerType)
                    throws UnableToCompleteException {
        SerializedItem value = KNOWN_ITEMS.get(item);
        if (value == null) {
            value = new SerializedItem(item, logger, typeOracle, containerType);
            KNOWN_ITEMS.put(item, value);
        }
        return value;
    }

    private void extractFromXmlElement(XmlElement xmlElement,
            TypeOracle typeOracle) {
        if (defaultValue == null) {
            defaultValue = xmlElement.defaultValue();
            if (defaultValue.equals("\u0000")) {
                defaultValue = null;
            }
        }
        if (name == null) {
            String elementName = xmlElement.name();
            if (!elementName.equals("##default")) {
                name = elementName;
            }
        }
        if (nillable == null) {
            nillable = xmlElement.nillable();
        }
        if (required == null) {
            required = xmlElement.required();
        }
        Class<?> type = xmlElement.type();
        if (!type.equals(XmlElement.DEFAULT.class)) {
            types.add(typeOracle.findType(type.getName()));
        }
    }

    private void extractFromXmlAttribute(XmlAttribute xmlAttribute) {
        if (name == null) {
            String attributeName = xmlAttribute.name();
            if (!attributeName.equals("##default")) {
                name = attributeName;
            }
        }
        if (required == null) {
            required = xmlAttribute.required();
        }
    }

    private void extractFromXmlElementRef(XmlElementRef xmlElementRef,
            TypeOracle typeOracle, TreeLogger logger,
            JClassType containerType) throws UnableToCompleteException {
        if (name == null) {
            JType refType = null;
            if (!xmlElementRef.type().equals(XmlElementRef.DEFAULT.class)) {
                try {
                    refType = typeOracle.getType(xmlElementRef.type().getName());
                } catch (NotFoundException e) {
                    logger.log(Type.ERROR, "Referenced type "
                        + xmlElementRef.type().getName() + " not found", e);
                    throw new UnableToCompleteException();
                }
            } else if (item instanceof JField) {
                refType = ((JField) item).getType();
            } else if (item instanceof JMethod) {
                refType = ((JMethod) item).getReturnType();
            }
            if (!(refType instanceof JClassType)) {
                logger.log(Type.ERROR, "Invalid type "
                    + refType.getQualifiedSourceName()
                    + " for XmlElementRef in "
                    + containerType.getQualifiedSourceName());
                throw new UnableToCompleteException();
            }

            XmlRootElement rootElement = ((JClassType)
                    refType).getAnnotation(XmlRootElement.class);
            if (rootElement == null) {
                logger.log(Type.ERROR,
                        "Missing XmlRootElement on XmlElementRef'd type "
                        + refType.getQualifiedSourceName() + " from "
                        + containerType.getQualifiedSourceName());
                throw new UnableToCompleteException();
            }

            String elementName = rootElement.name();
            if (elementName.equals("##default")) {
                name = refType.getSimpleSourceName();
                name = name.substring(0, 1).toLowerCase() + name.substring(1);
            } else {
                name = elementName;
            }
        }
    }

    private void extractFromXmlIdRef(TypeOracle typeOracle, TreeLogger logger,
            JClassType containerType) throws UnableToCompleteException {
        if (objectIdItem == null) {
            JType refType = null;
            if (item instanceof JField) {
                refType = ((JField) item).getType();
            } else if (item instanceof JMethod) {
                refType = ((JMethod) item).getReturnType();
            }
            if (!(refType instanceof JClassType)) {
                logger.log(Type.ERROR, "Invalid type "
                        + refType.getQualifiedSourceName()
                        + " for XmlIDREF in "
                        + containerType.getQualifiedSourceName());
                throw new UnableToCompleteException();
            }

            objectIdItem = getXmlId((JClassType) refType, logger, typeOracle);
            if (objectIdItem == null) {
                logger.log(Type.ERROR, "Missing XmlID in class "
                    + refType.getQualifiedSourceName()
                    + " referenced from XmlIDREF in "
                    + containerType.getQualifiedSourceName());
            }

            JType xmlIdType = null;
            if (objectIdItem instanceof JField) {
                xmlIdType = ((JField) objectIdItem).getType();
            } else if (objectIdItem instanceof JMethod) {
                xmlIdType = ((JMethod) objectIdItem).getReturnType();
            }
            if (!xmlIdType.getQualifiedSourceName().equals(
                    "java.lang.String")) {
                logger.log(Type.ERROR, "XmlID must be a java.lang.String "
                        + " in " + xmlIdType.getQualifiedSourceName());
                throw new UnableToCompleteException();
            }
        }
    }

    private void extractFromXmlElementWrapper(
            XmlElementWrapper xmlElementWrapper, TreeLogger logger)
                    throws UnableToCompleteException {
        if (wrapperName == null) {
            wrapperName = xmlElementWrapper.name();
            if (wrapperName.equals("##default")) {
                wrapperName = getItemName(logger) + "s";
            }
        }
    }

    private JMethod findSetMethod(JClassType type, String name,
            JType paramType) {
        if (type == null) {
            return null;
        }
        for (JMethod method : type.getMethods()) {
            if (method.getName().equals(name)) {
                if (method.getParameterTypes().length == 1) {
                    if (method.getParameterTypes()[0].equals(paramType)) {
                        return method;
                    }
                }
            }
        }
        return findSetMethod(type.getSuperclass(), name, paramType);
    }

    private JMethod findGetMethod(JClassType type, String name,
            JType returnType) {
        if (type == null) {
            return null;
        }
        for (JMethod method : type.getMethods()) {
            if (method.getName().equals(name)) {
                if (method.getParameterTypes().length == 0) {
                    if (method.getReturnType().equals(returnType)) {
                        return method;
                    }
                }
            }
        }
        return findGetMethod(type.getSuperclass(), name, returnType);
    }

    private JField findField(JClassType type, String name, JType fieldType) {
        if (type == null) {
            return null;
        }
        for (JField field : type.getFields()) {
            if (field.getName().equals(name)) {
                if (field.getType().equals(fieldType)) {
                    return field;
                }
            }
        }
        return findField(type.getSuperclass(), name, fieldType);
    }

    private SerializedItem(HasAnnotations item, TreeLogger logger,
            TypeOracle typeOracle, JClassType containerType)
                    throws UnableToCompleteException {
        this.item = item;

        if (item instanceof JMethod) {
            isMethod = true;
            JMethod method = (JMethod) item;
            actualType = method.getReturnType();
            if ((method.getName().startsWith("get")
                    || method.getName().startsWith("is"))
                    && (method.getParameterTypes().length == 0)) {
                getMethod = method;
                String itemName = null;
                if (method.getName().startsWith("get")) {
                    itemName = method.getName().substring(3);
                } else {
                    itemName = method.getName().substring(2);
                }
                setMethod = findSetMethod(method.getEnclosingType(),
                        "set" + itemName,
                            method.getReturnType());
                if (setMethod == null) {
                    field = findField(method.getEnclosingType(),
                            itemName.substring(0, 1).toLowerCase()
                                + itemName.substring(1),
                            method.getReturnType());
                }
            } else if (method.getName().startsWith("set")
                    && (method.getParameterTypes().length == 1)) {
                setMethod = method;
                String itemName = method.getName().substring(3);
                getMethod = findGetMethod(method.getEnclosingType(),
                        "get" + itemName, method.getParameterTypes()[0]);
                if (getMethod == null) {
                    field = findField(method.getEnclosingType(),
                            itemName.substring(0, 1).toLowerCase()
                                + itemName.substring(1),
                            method.getParameterTypes()[0]);
                }
            }
        } else if (item instanceof JField) {
            isMethod = false;
            actualType = ((JField) item).getType();
            field = (JField) item;
            trans = ((JField) item).isTransient();
        } else {
            logger.log(Type.ERROR, "Item is not a method or field: " + item);
            throw new UnableToCompleteException();
        }

        xmlValue = item.getAnnotation(XmlValue.class);
        xmlElement = item.getAnnotation(XmlElement.class);
        xmlAttribute = item.getAnnotation(XmlAttribute.class);
        xmlElementRef = item.getAnnotation(XmlElementRef.class);
        xmlElements = item.getAnnotation(XmlElements.class);
        xmlElementRefs = item.getAnnotation(XmlElementRefs.class);
        xmlIdRef = item.getAnnotation(XmlIDREF.class);
        xmlElementWrapper = item.getAnnotation(XmlElementWrapper.class);
        xmlJavaTypeAdapter = item.getAnnotation(XmlJavaTypeAdapter.class);
        xmlTransient = item.getAnnotation(XmlTransient.class);

        if (xmlValue != null) {
            name = "value";
            annotated = true;
        }

        if (xmlElement != null) {
            extractFromXmlElement(xmlElement, typeOracle);
            annotated = true;
        }

        if (xmlAttribute != null) {
            extractFromXmlAttribute(xmlAttribute);
            annotated = true;
        }

        if (xmlElementRef != null) {
            extractFromXmlElementRef(xmlElementRef, typeOracle, logger,
                    containerType);
            annotated = true;
        }

        if (xmlIdRef != null) {
            extractFromXmlIdRef(typeOracle, logger, containerType);
            annotated = true;
        }

        if (xmlElementWrapper != null) {
           extractFromXmlElementWrapper(xmlElementWrapper, logger);
           annotated = true;
        }

        if (xmlTransient != null) {
            if (annotated) {
                logger.log(Type.ERROR,
                        "An item cannot be both transient and not transient!");
            }
            trans = true;
        }
    }

    public void setSuperItem(SerializedItem item, TreeLogger logger,
            TypeOracle typeOracle, JClassType containerType)
                    throws UnableToCompleteException {
        if (!trans) {
            if ((xmlValue == null) && (item.xmlValue != null)) {
                if (name == null) {
                    name = "value";
                }
                this.xmlValue = item.xmlValue;
                annotated = true;
            }

            if ((xmlElement == null) && (item.xmlElement != null)) {
                extractFromXmlElement(item.xmlElement, typeOracle);
                this.xmlElement = item.xmlElement;
                annotated = true;
            }

            if ((xmlAttribute == null) && (item.xmlAttribute != null)) {
                extractFromXmlAttribute(item.xmlAttribute);
                this.xmlAttribute = item.xmlAttribute;
                annotated = true;
            }

            if ((xmlElementRef == null) && (item.xmlElementRef != null)) {
                extractFromXmlElementRef(item.xmlElementRef, typeOracle, logger,
                        containerType);
                this.xmlElementRef = item.xmlElementRef;
                annotated = true;
            }

            if ((xmlIdRef == null) && (item.xmlIdRef != null)) {
                extractFromXmlIdRef(typeOracle, logger, containerType);
                this.xmlIdRef = item.xmlIdRef;
                annotated = true;
            }

            if ((xmlElementWrapper == null)
                    && (item.xmlElementWrapper != null)) {
                extractFromXmlElementWrapper(item.xmlElementWrapper, logger);
                this.xmlElementWrapper = item.xmlElementWrapper;
                annotated = true;
            }

            if ((xmlTransient == null) && (item.xmlTransient != null)) {
                if (!annotated) {
                    trans = true;
                    this.xmlTransient = item.xmlTransient;
                }
            }
        }
    }

    public String getItemName(TreeLogger logger)
            throws UnableToCompleteException {
        if (item instanceof JField) {
            return ((JField) item).getName();
        } else if (item instanceof JMethod) {
            JMethod method = (JMethod) item;
            String itemName = method.getName();
            if (itemName.startsWith("get") || itemName.startsWith("set")) {
                itemName = itemName.substring(3);
            } else if (itemName.startsWith("is")) {
                itemName = itemName.substring(2);
            } else {
                logger.log(Type.ERROR, "Method " + method.getName() + " in "
                    + method.getEnclosingType().getQualifiedSourceName()
                    + " is not a bean method");
                throw new UnableToCompleteException();
            }
            itemName = itemName.substring(0, 1).toLowerCase()
                    + itemName.substring(1);
            return itemName;
        }

        logger.log(Type.ERROR, "Item " + item + " is not a field or method");
        throw new UnableToCompleteException();
    }

    public void setDefaults(TreeLogger logger)
            throws UnableToCompleteException {
        if (name == null) {
            name = getItemName(logger);
        }

        if (nillable == null) {
            nillable = false;
        }

        if (required == null) {
            required = false;
        }
    }

    private SerializedItem getXmlId(JClassType type, TreeLogger logger,
            TypeOracle typeOracle) throws UnableToCompleteException {
        if (type == null) {
            return null;
        }
        for (JField field : type.getFields()) {
            if (field.getAnnotation(XmlID.class) != null) {
                return get(field, logger, typeOracle, type);
            }
        }

        for (JMethod method : type.getMethods()) {
            if (method.getAnnotation(XmlID.class) != null) {
                return get(method, logger, typeOracle, type);
            }
        }

        SerializedItem xmlId = getXmlId(type.getSuperclass(), logger,
                typeOracle);
        if (xmlId != null) {
            return xmlId;
        }

        return null;
    }

    public HasAnnotations getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isNillable() {
        return nillable;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isValue() {
        return xmlValue != null;
    }

    public List<JClassType> getTypes() {
        return types;
    }

    public boolean isAnnotated() {
        return annotated || (xmlTransient != null);
    }

    public boolean isTransient() {
        return trans;
    }

    public boolean isMethod() {
        return isMethod;
    }

    public JType getActualType() {
        return actualType;
    }

    public JMethod getSetMethod() {
        return setMethod;
    }

    public JMethod getGetMethod() {
        return getMethod;
    }

    public JField getField() {
        return field;
    }
}
