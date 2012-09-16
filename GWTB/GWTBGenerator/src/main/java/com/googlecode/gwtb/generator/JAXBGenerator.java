package com.googlecode.gwtb.generator;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JArrayType;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.JWildcardType;
import com.google.gwt.core.ext.typeinfo.JWildcardType.BoundType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public class JAXBGenerator extends Generator {

    private JClassType collectionType = null;

    private JClassType listType = null;

    private JClassType setType = null;

    private JClassType queueType = null;

    private JClassType jaxbSerializable = null;

    @Override
    public String generate(TreeLogger logger, GeneratorContext context,
            String typeName) throws UnableToCompleteException {
        try {
            TypeOracle typeOracle = context.getTypeOracle();
            JClassType type = typeOracle.getType(typeName);
            Map<String, SerializedItem> items = new HashMap<String, SerializedItem>();
            XmlAccessorType accessorType = getAnnotation(XmlAccessorType.class,
                    type);
            XmlAccessType accessType = XmlAccessType.PUBLIC_MEMBER;
            if (accessorType != null) {
                accessType = accessorType.value();
            }
            logger.log(Type.DEBUG, "Generating serializer for "
                + type.getQualifiedSourceName());
            logger.log(Type.DEBUG, "Access type = " + accessType.toString());
            getItems(logger, typeOracle, type, items, accessType);

            String packageName = type.getPackage().getName();
            String className = type.getName() + "JSONSerializer";

            collectionType = typeOracle.getType("java.util.Collection");
            listType = typeOracle.getType("java.util.List");
            setType = typeOracle.getType("java.util.Set");
            queueType = typeOracle.getType("java.util.Queue");
            jaxbSerializable = typeOracle.getType(
                    "com.googlecode.gwtb.gwt.JAXBSerializable");

            ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(
                    packageName, className);
            composer.addImplementedInterface("JSONSerializer<"
                    + type.getName() + ">");
            composer.addImport("com.googlecode.gwtb.generator.gwt.JSONSerializer");
            composer.addImport(type.getQualifiedSourceName());
            composer.addImport("com.googlecode.gwtb.generator.gwt.SerializationException");
            composer.addImport("com.google.gwt.json.client.JSONArray");
            composer.addImport("com.google.gwt.json.client.JSONBoolean");
            composer.addImport("com.google.gwt.json.client.JSONNull");
            composer.addImport("com.google.gwt.json.client.JSONNumber");
            composer.addImport("com.google.gwt.json.client.JSONObject");
            composer.addImport("com.google.gwt.json.client.JSONString");
            composer.addImport("com.google.gwt.json.client.JSONValue");
            composer.addImport("com.google.gwt.core.client.GWT");
            composer.addImport("com.google.gwt.core.client.UnsafeNativeLong");
            composer.addImport("java.util.Collection");
            composer.addImport("java.util.List");
            composer.addImport("java.util.Queue");
            composer.addImport("java.util.Set");
            composer.addImport("java.util.ArrayList");
            composer.addImport("java.util.HashSet");
            composer.addImport("java.util.LinkedList");
            composer.addImport(jaxbSerializable.getQualifiedSourceName());

            PrintWriter printWriter = context.tryCreate(logger, packageName,
                    className);
            if (printWriter != null) {
                SourceWriter writer = composer.createSourceWriter(context,
                        printWriter);
                writeAccessors(writer, logger, type, items);
                writeDeserializer(writer, logger, type, items);
                writeSerializer(writer, logger, type, items);
                writer.commit(logger);
                context.commit(logger, printWriter);
            }
            return packageName + "." + className;
        } catch (UnableToCompleteException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Type.ERROR, e.getMessage());
            throw new UnableToCompleteException();
        }
    }

    private void writeAccessors(SourceWriter writer, TreeLogger logger,
            JClassType type, Map<String, SerializedItem> items) {
        writer.println("private native " + type.getQualifiedSourceName()
                + " create" + type.getName() + "() /*-{");
        writer.indent();
        writer.println("return @" + type.getQualifiedSourceName()
                + "::new()();");
        writer.outdent();
        writer.println("}-*/;");
        for (SerializedItem item : items.values()) {
            JType actualType = item.getActualType();

            if (item.getActualType().equals(JPrimitiveType.LONG)) {
                writer.println("@UnsafeNativeLong");
            }
            writer.println("private native "
                    + actualType.getParameterizedQualifiedSourceName()
                    + " getJS" + item.getName() + "("
                    + type.getQualifiedSourceName() + " item) /*-{");
            writer.indent();
            if (item.getGetMethod() != null) {
                writer.println("return item.@" + type.getQualifiedSourceName()
                        + "::" + item.getGetMethod().getName() + "()();");
            } else {
                writer.println("return item.@" + type.getQualifiedSourceName()
                        + "::" + item.getField().getName() + ";");
            }
            writer.outdent();
            writer.println("}-*/;");

            if (item.getActualType().equals(JPrimitiveType.LONG)) {
                writer.println("@UnsafeNativeLong");
            }
            writer.println("private native void setJS" + item.getName() + "("
                    + type.getQualifiedSourceName() + " item, "
                    + actualType.getQualifiedSourceName() + " value) /*-{");
            writer.indent();
            if (item.getSetMethod() != null) {
                writer.println("item.@"
                        + type.getQualifiedSourceName()
                        + "::"
                        + item.getSetMethod().getName()
                        + "("
                        + item.getSetMethod().getParameterTypes()[0]
                                .getJNISignature() + ")(value);");
            } else {
                writer.println("item.@" + type.getQualifiedSourceName() + "::"
                        + item.getField().getName() + " = value;");
            }
            writer.outdent();
            writer.println("}-*/;");
        }
    }

    private void writeDeserializer(SourceWriter writer, TreeLogger logger,
            JClassType type, Map<String, SerializedItem> items)
            throws UnableToCompleteException {
        writer.println("public " + type.getName()
                + " fromJSON(JSONObject object)"
                + " throws SerializationException {");
        writer.indent();
        writer.println(type.getName() + " objectInstance = create"
                + type.getName() + "();");

        for (SerializedItem item : items.values()) {
            if (item.isTransient()) {
                continue;
            }

            // Get the JSONValue for the item
            String name = item.getName();
            writer.println();
            writer.println("// " + name);
            writer.println("JSONValue " + name + "Value" + " = object.get(\""
                    + name + "\");");

            if (item.isRequired()) {

                // If the item is required, fail if it doesn't exist
                writer.println("if (" + name + "Value == null) {");
                writer.indent();
                writer.println("throw new SerializationException(\"" + "Item "
                        + name + " is required, but is missing in "
                        + type.getQualifiedSourceName() + "\", object);");
                writer.outdent();
                writer.println("}");
            } else {

                // If the item is not required, only continue if it exists
                writer.println("if (" + name + "Value != null) {");
                writer.indent();
                writer.println("");
            }

            if (!item.isNillable()) {

                // If the item is not nillable, fail if it is set to null
                writer.println("if (" + name + "Value.isNull() != null) {");
                writer.indent();
                writer.println("throw new SerializationException(\"" + "Item "
                        + name + " is null but is not nillable in "
                        + type.getQualifiedSourceName() + "\", object);");
                writer.outdent();
                writer.println("}");
            } else {

                // If the item is nillable, only continue if it is not null
                // i.e. if .isNull() is null(!)
                writer.println("if (" + name + "Value.isNull() == null) {");
                writer.indent();
            }

            List<JClassType> types = item.getTypes();
            if (types.isEmpty()) {
                JType itemType = item.getActualType();
                deserializeType(writer, logger, itemType, name + "Value",
                        "setJS" + item.getName() + "(objectInstance, ", ");");
            } else {
                // TODO: Process multiple possible types
            }

            // Close bracket from nillable check
            if (item.isNillable()) {
                writer.outdent();
                writer.println("}");
            }

            // Close bracket from required check
            if (!item.isRequired()) {

                writer.outdent();
                writer.println("}");
            }
        }
        writer.println("return objectInstance;");
        writer.outdent();
        writer.println("}");
    }

    private void deserializeType(SourceWriter writer, TreeLogger logger,
            JType itemType, String name, String beforeSet, String afterSet)
            throws UnableToCompleteException {
        String itemName = itemType.getQualifiedSourceName();
        if (itemName.equals("java.lang.Integer")
                || itemType.equals(JPrimitiveType.INT)
                || itemName.equals("java.lang.Long")
                || itemType.equals(JPrimitiveType.LONG)
                || itemName.equals("java.lang.Short")
                || itemType.equals(JPrimitiveType.SHORT)
                || itemName.equals("java.lang.Byte")
                || itemType.equals(JPrimitiveType.BYTE)
                || itemName.equals("java.lang.Float")
                || itemType.equals(JPrimitiveType.FLOAT)
                || itemName.equals("java.lang.Double")
                || itemType.equals(JPrimitiveType.DOUBLE)) {
            writer.println("JSONNumber " + name + "Number" + " = " + name
                    + ".isNumber();");
            writer.println("if (" + name + "Number == null) {");
            writer.indent();
            writer.println("throw new SerializationException(\"" + name
                    + " in JSON is not a number\", object);");
            writer.outdent();
            writer.println("}");

            String cast = null;
            if (itemName.equals("java.lang.Integer")
                    || itemType.equals(JPrimitiveType.INT)) {
                cast = "(int) ";
            } else if (itemName.equals("java.lang.Long")
                    || itemType.equals(JPrimitiveType.LONG)) {
                cast = "(long) ";
            } else if (itemType.equals("java.lang.Short")
                    || itemType.equals(JPrimitiveType.SHORT)) {
                cast = "(short) ";
            } else if (itemType.equals("java.lang.Byte")
                    || itemType.equals(JPrimitiveType.BYTE)) {
                cast = "(byte) ";
            } else if (itemType.equals("java.lang.Float")
                    || itemType.equals(JPrimitiveType.FLOAT)) {
                cast = "(float) ";
            } else if (itemType.equals("java.lang.Double")
                    || itemType.equals(JPrimitiveType.DOUBLE)) {
                cast = "(double) ";
            }
            writer.println(beforeSet + cast + name + "Number.doubleValue()"
                    + afterSet);
        } else if (itemName.equals("java.lang.Boolean")
                || itemType.equals(JPrimitiveType.BOOLEAN)) {
            writer.println("JSONBoolean " + name + "Boolean" + " = " + name
                    + ".isBoolean();");
            writer.println("if (" + name + "Boolean == null) {");
            writer.indent();
            writer.println("throw new SerializationException(\"" + name
                    + " in JSON is not a boolean\", object);");
            writer.outdent();
            writer.println("}");
            writer.println(beforeSet + name + "Boolean.booleanValue()"
                    + afterSet);
        } else if (itemName.equals("java.lang.Char")
                || itemType.equals(JPrimitiveType.CHAR)
                || itemName.equals("java.lang.String")) {
            writer.println("JSONString " + name + "String" + " = " + name
                    + ".isString();");
            writer.println("if (" + name + "String == null) {");
            writer.indent();
            writer.println("throw new SerializationException(\"" + name
                    + " in JSON is not a String\", object);");
            writer.outdent();
            writer.println("}");
            if (itemName.equals("java.lang.String")) {
                writer.println(beforeSet + name + "String.stringValue()"
                        + afterSet);
            } else {
                writer.println("String " + name + "Chars = " + name
                        + "String.stringValue();");
                writer.println("if (" + name + "Chars.length() != 1) {");
                writer.indent();
                writer.println("throw new SerializationException(\""
                        + name
                        + " in JSON is too long or short for a char\", object);");
                writer.outdent();
                writer.println("}");
                writer.println(beforeSet + name
                        + "String.stringValue().charAt(0)" + afterSet);
            }
        } else if (itemType.isArray() != null) {
            JArrayType arrayType = (JArrayType) itemType;
            writer.println("JSONArray " + name + "JSArray" + " = " + name
                    + ".isArray();");
            writer.println("if (" + name + "JSArray == null) {");
            writer.indent();
            writer.println("throw new SerializationException(\"" + name
                    + " in JSON is not an Array\", object);");
            writer.outdent();
            writer.println("}");
            JType componentType = arrayType.getComponentType();
            String component = componentType.getQualifiedSourceName();
            writer.println(component + "[] " + name + "Array" + " = new "
                    + component + "[" + name + "JSArray.size()];");
            writer.println("for (int " + name + "Count = 0; " + name
                    + "Count < " + name + "JSArray.size(); " + name
                    + "Count++) {");
            writer.indent();
            writer.println("JSONValue " + name + "ArrayItem = "
                    + name + "JSArray.get(" + name + "Count);");
            deserializeType(writer, logger, arrayType.getComponentType(), name
                    + "ArrayItem", name + "Array[" + name + "Count] = ", ";");
            writer.outdent();
            writer.println("}");
            writer.println(beforeSet + name + "Array" + afterSet);
        } else if (itemType instanceof JClassType) {
            JClassType classType = (JClassType) itemType;
            if (classType.isAssignableTo(collectionType)) {
                JParameterizedType paramType = classType.isParameterized();
                if (paramType == null) {
                    logger.log(Type.ERROR, "Cannot deserialize a collection "
                            + name + " with an unknown item type");
                    throw new UnableToCompleteException();
                }
                JClassType typeParam = paramType.getTypeArgs()[0];
                JWildcardType wildcardParam = typeParam.isWildcard();
                if (wildcardParam != null) {
                    if (wildcardParam.getBoundType() == BoundType.UNBOUND) {
                        logger.log(Type.ERROR,
                                "Cannot deserialize a collection " + name
                                        + " with an unknown item type");
                        throw new UnableToCompleteException();
                    }
                    if (wildcardParam.getBoundType() == BoundType.SUPER) {
                        logger.log(Type.ERROR,
                                "Cannot deserialize a collection" + name
                                        + " of super types");
                        throw new UnableToCompleteException();
                    }
                    typeParam = wildcardParam.getBaseType();
                }

                writer.println("JSONArray " + name + "JSArray" + " = " + name
                        + ".isArray();");
                writer.println("if (" + name + "JSArray == null) {");
                writer.indent();
                writer.println("throw new SerializationException(\"" + name
                        + " in JSON is not an Array\", object);");
                writer.outdent();
                writer.println("}");

                if (classType.isAssignableTo(listType)) {
                    writer.println("List<" + typeParam.getQualifiedSourceName()
                            + "> " + name + "Collection = new ArrayList<"
                            + typeParam.getQualifiedSourceName() + ">();");
                } else if (classType.isAssignableTo(setType)) {
                    writer.println("Set<" + typeParam.getQualifiedSourceName()
                            + "> " + name + "Collection = new HashSet<"
                            + typeParam.getQualifiedSourceName() + ">();");
                } else if (classType.isAssignableTo(queueType)) {
                    writer.println("Queue<"
                            + typeParam.getQualifiedSourceName() + "> " + name
                            + "Collection = new LinkedList<"
                            + typeParam.getQualifiedSourceName() + ">();");
                } else {
                    writer.println("Collection<"
                            + typeParam.getQualifiedSourceName() + "> " + name
                            + "Collection = new ArrayList<"
                            + typeParam.getQualifiedSourceName() + ">();");
                }

                writer.println("for (int " + name + "Count = 0; " + name
                        + "Count < " + name + "JSArray.size(); " + name
                        + "Count++) {");
                writer.indent();
                writer.println("JSONValue " + name + "CollectionItem = " + name
                        + "JSArray.get(" + name + "Count);");
                deserializeType(writer, logger, typeParam, name
                        + "CollectionItem", name + "Collection.add(", ");");
                writer.outdent();
                writer.println("}");
                writer.println(beforeSet + name + "Collection" + afterSet);
            } else if (classType.isAssignableTo(jaxbSerializable)) {
                writer.println("JSONObject " + name + "JSObject = " + name
                        + ".isObject();");
                writer.println("if (" + name + "JSObject == null) {");
                writer.indent();
                writer.println("throw new SerializationException(\"" + name
                        + " in JSON is not an Object\", object);");
                writer.outdent();
                writer.println("}");
                writer.println("JSONSerializer<"
                        + classType.getQualifiedSourceName() + "> " + name
                        + "Serializer = GWT.create("
                        + classType.getQualifiedSourceName() + ".class);");
                writer.println(classType.getQualifiedSourceName() + " " + name
                        + "Object = " + name + "Serializer.fromJSON(" + name
                        + "JSObject);");
                writer.println(beforeSet + name + "Object" + afterSet);
            } else {
                logger.log(Type.ERROR, "Unable to deserialize " + name
                        + " in " + itemType.getQualifiedSourceName()
                        + " unknown type "+ classType);
                throw new UnableToCompleteException();
            }
        }
    }

    private void writeSerializer(SourceWriter writer, TreeLogger logger,
            JClassType type, Map<String, SerializedItem> items)
            throws UnableToCompleteException {
        writer.println("public JSONObject toJSON(" + type.getName()
                + " objectInstance)" + " throws SerializationException {");
        writer.indent();
        writer.println("JSONObject object = new JSONObject();");

        for (SerializedItem item : items.values()) {
            if (item.isTransient()) {
                continue;
            }

            // Get the JSONValue for the item
            String name = item.getName();
            writer.println();
            writer.println("// " + name);
            writer.println(
                item.getActualType().getParameterizedQualifiedSourceName()
                + " " + name + "Value = getJS" + name + "(objectInstance);");

            if (item.getActualType().isPrimitive() == null) {
                if (item.isRequired()) {

                    // If the item is required, fail if it doesn't exist
                    writer.println("if (" + name + "Value == null) {");
                    writer.indent();
                    writer.println(
                            "throw new SerializationException(\"" + "Item "
                            + name + " is required, but is missing in "
                            + type.getQualifiedSourceName() + "\", object);");
                    writer.outdent();
                    writer.println("}");
                } else {

                    // If the item is not required, only continue if it exists
                    writer.println("if (" + name + "Value != null) {");
                    writer.indent();
                    writer.println("");
                }

                if (!item.isNillable()) {

                    // If the item is not nillable, fail if it is set to null
                    writer.println("if (" + name + "Value == null) {");
                    writer.indent();
                    writer.println("throw new SerializationException(\"" + "Item "
                            + name + " is null but is not nillable in "
                            + type.getQualifiedSourceName() + "\", object);");
                    writer.outdent();
                    writer.println("}");
                } else {

                    // If the item is nillable, only continue if it is not null
                    // i.e. if .isNull() is null(!)
                    writer.println("if (" + name + "Value == null) {");
                    writer.indent();
                    writer.println("object.put(" + name
                            + ", JSONNull.getInstance());");
                    writer.outdent();
                    writer.println("} else {");
                    writer.indent();
                }
            }

            List<JClassType> types = item.getTypes();
            if (types.isEmpty()) {
                JType itemType = item.getActualType();
                serializeType(writer, logger, itemType, name + "Value",
                        "object.put(\"" + name + "\", ", ");");
            } else {
                // TODO: Process multiple possible types
            }

            if (item.getActualType().isPrimitive() == null) {
                // Close bracket from nillable check
                if (item.isNillable()) {
                    writer.outdent();
                    writer.println("}");
                }

                // Close bracket from required check
                if (!item.isRequired()) {

                    writer.outdent();
                    writer.println("}");
                }
            }
        }
        writer.println("return object;");
        writer.outdent();
        writer.println("}");
    }

    private void serializeType(SourceWriter writer, TreeLogger logger,
            JType itemType, String name, String beforeSet, String afterSet)
            throws UnableToCompleteException {
        String itemName = itemType.getQualifiedSourceName();
        if (itemName.equals("java.lang.Integer")
                || itemType.equals(JPrimitiveType.INT)
                || itemName.equals("java.lang.Long")
                || itemType.equals(JPrimitiveType.LONG)
                || itemName.equals("java.lang.Short")
                || itemType.equals(JPrimitiveType.SHORT)
                || itemName.equals("java.lang.Byte")
                || itemType.equals(JPrimitiveType.BYTE)
                || itemName.equals("java.lang.Float")
                || itemType.equals(JPrimitiveType.FLOAT)
                || itemName.equals("java.lang.Double")
                || itemType.equals(JPrimitiveType.DOUBLE)) {
            writer.println("JSONNumber " + name + "Number"
                + " = new JSONNumber((double) " + name + ");");
            writer.println(beforeSet + name + "Number" + afterSet);
        } else if (itemName.equals("java.lang.Boolean")
                || itemType.equals(JPrimitiveType.BOOLEAN)) {
            writer.println("JSONBoolean " + name + "Boolean"
                + " = new JSONBoolean(" + name + ");");
            writer.println(beforeSet + name + "Boolean" + afterSet);
        } else if (itemName.equals("java.lang.Char")
                || itemType.equals(JPrimitiveType.CHAR)
                || itemName.equals("java.lang.String")) {
            writer.println("JSONString " + name + "String"
                + " = new JSONString(String.valueOf(" + name + "));");
            writer.println(beforeSet + name + "String" + afterSet);
        } else if (itemType.isArray() != null) {
            JArrayType arrayType = (JArrayType) itemType;
            writer.println("JSONArray " + name + "JSArray"
                + " = new JSONArray();");
            JType componentType = arrayType.getComponentType();
            String component = componentType.getQualifiedSourceName();
            writer.println("for (int " + name + "Count = 0; " + name
                    + "Count < " + name + ".length; " + name
                    + "Count++) {");
            writer.indent();
            writer.println(component + " " + name + "ArrayItem = "
                    + name + "[" + name + "Count];");
            serializeType(writer, logger, arrayType.getComponentType(), name
                    + "ArrayItem", name + "JSArray.set(" + name + "Count, ",
                    ");");
            writer.outdent();
            writer.println("}");
            writer.println(beforeSet + name + "JSArray" + afterSet);
        } else if (itemType instanceof JClassType) {
            JClassType classType = (JClassType) itemType;
            if (classType.isAssignableTo(collectionType)) {
                JParameterizedType paramType = classType.isParameterized();
                if (paramType == null) {
                    logger.log(Type.ERROR, "Cannot serialize a collection "
                            + name + " with an unknown item type");
                    throw new UnableToCompleteException();
                }
                JClassType typeParam = paramType.getTypeArgs()[0];
                JWildcardType wildcardParam = typeParam.isWildcard();
                if (wildcardParam != null) {
                    if (wildcardParam.getBoundType() == BoundType.UNBOUND) {
                        logger.log(Type.ERROR,
                                "Cannot serialize a collection " + name
                                        + " with an unknown item type");
                        throw new UnableToCompleteException();
                    }
                    if (wildcardParam.getBoundType() == BoundType.SUPER) {
                        logger.log(Type.ERROR,
                                "Cannot serialize a collection" + name
                                        + " of super types");
                        throw new UnableToCompleteException();
                    }
                    typeParam = wildcardParam.getBaseType();
                }

                writer.println("JSONArray " + name + "JSArray"
                        + " = new JSONArray();");
                writer.println("int " + name + "Count = 0;");
                writer.println("for (" + typeParam.getQualifiedSourceName()
                        + " " + name + "CollectionItem : " + name + ") {");
                writer.indent();
                serializeType(writer, logger, typeParam, name
                        + "CollectionItem", name + "JSArray.set(" + name
                        + "Count++, ", ");");
                writer.outdent();
                writer.println("}");
                writer.println(beforeSet + name + "JSArray" + afterSet);
            } else if (classType.isAssignableTo(jaxbSerializable)) {
                writer.println("JSONSerializer<"
                        + classType.getQualifiedSourceName() + "> " + name
                        + "Serializer = GWT.create("
                        + classType.getQualifiedSourceName() + ".class);");
                writer.println("JSONObject " + name + "JSObject = "
                        + name + "Serializer.toJSON(" + name + ");");
                writer.println(beforeSet + name + "JSObject" + afterSet);
            } else {
                logger.log(Type.ERROR, "Unable to serialize " + name
                        + " in " + itemType.getQualifiedSourceName()
                        + " unknown type "+ classType);
                throw new UnableToCompleteException();
            }
        }
    }

    private Map<String, SerializedItem> getItems(TreeLogger logger,
            TypeOracle typeOracle, JClassType cls,
            Map<String, SerializedItem> items, XmlAccessType accessType)
            throws UnableToCompleteException {
        if (cls == null) {
            return items;
        }
        for (JField field : cls.getFields()) {
            SerializedItem item = SerializedItem.get(field, logger, typeOracle,
                    cls);
            String name = item.getName();
            if (name == null) {
                name = item.getItemName(logger);
            }
            logger.log(Type.DEBUG, "Looking at field " + name);
            logger.log(Type.DEBUG, "    Annotated? " + item.isAnnotated());
            logger.log(Type.DEBUG, "    accessType? " + accessType);
            logger.log(Type.DEBUG, "    public? " + field.isPublic());
            if (!item.isTransient()
                    && (item.isAnnotated()
                    || (accessType == XmlAccessType.FIELD)
                    || ((accessType == XmlAccessType.PUBLIC_MEMBER)
                            && field.isPublic()))) {
                SerializedItem childItem = items.get(name);
                if (childItem == null) {
                    items.put(name, item);
                } else {
                    childItem.setSuperItem(item, logger, typeOracle, cls);
                }
            }
        }

        for (JMethod method : cls.getMethods()) {
            SerializedItem item = SerializedItem.get(method, logger,
                    typeOracle, cls);
            if (!item.isTransient()
                    && (item.isAnnotated()
                        || ((accessType == XmlAccessType.PROPERTY)
                            && (item.getSetMethod() != null)
                            && (item.getGetMethod() != null))
                        || ((accessType == XmlAccessType.PUBLIC_MEMBER)
                            && method.isPublic()
                            && (item.getGetMethod() != null)
                            && (item.getSetMethod() != null)))) {
                if ((item.getGetMethod() != null)
                        && (item.getSetMethod() == null)
                        && (item.getField() == null)) {
                    logger.log(Type.ERROR, "No matching set method or field"
                            + " for get method "
                            + method.getName()
                            + " in "
                            + method.getEnclosingType()
                                    .getQualifiedSourceName());
                } else if ((item.getSetMethod() != null)
                        && (item.getGetMethod() == null)
                        && (item.getField() == null)) {
                    logger.log(Type.ERROR, "No matching get method or field"
                            + " for set method "
                            + method.getName()
                            + " in "
                            + method.getEnclosingType()
                                    .getQualifiedSourceName());
                }

                String name = item.getName();
                if (name == null) {
                    name = item.getItemName(logger);
                }
                SerializedItem childItem = items.get(name);
                if (childItem == null) {
                    items.put(name, item);
                } else {
                    childItem.setSuperItem(item, logger, typeOracle, cls);
                }
            }
        }

        items = getItems(logger, typeOracle, cls.getSuperclass(), items,
                accessType);
        for (SerializedItem item : items.values()) {
            item.setDefaults(logger);
        }
        return items;
    }

    private <T extends Annotation> T getAnnotation(Class<T> annotationClass,
            JClassType cls) {
        if (cls == null) {
            return null;
        }
        T annotation = cls.getAnnotation(annotationClass);
        if (annotation == null) {
            annotation = getAnnotation(annotationClass, cls.getSuperclass());
        }
        if (annotation == null) {
            for (JClassType superCls : cls.getImplementedInterfaces()) {
                annotation = getAnnotation(annotationClass, superCls);
                if (annotation != null) {
                    break;
                }
            }
        }
        return annotation;
    }

}
