package com.googlecode.gwtb.generator;

import java.io.PrintWriter;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public class JAXBGenerator extends Generator {

    @Override
    public String generate(TreeLogger logger, GeneratorContext context,
            String typeName) throws UnableToCompleteException {
        try {
            TypeOracle typeOracle = context.getTypeOracle();
            JClassType type = typeOracle.getType(typeName);
            String packageName = type.getPackage().getName();
            String className = type.getName() + "JSONSerializer";

            JClassType interfaceType = typeOracle.getType(
                    "com.googlecode.gwtb.generator.gwt.JSONSerializer");

            ClassSourceFileComposerFactory composer =
                    new ClassSourceFileComposerFactory(
                            packageName, className);
            composer.addImplementedInterface(
                    interfaceType.getName() + "<" + type.getName() + ">");
            composer.addImport(interfaceType.getQualifiedSourceName());
            composer.addImport(type.getQualifiedSourceName());
            composer.addImport("com.google.gwt.json.client.JSONArray");
            composer.addImport("com.google.gwt.json.client.JSONBoolean");
            composer.addImport("com.google.gwt.json.client.JSONNull");
            composer.addImport("com.google.gwt.json.client.JSONNumber");
            composer.addImport("com.google.gwt.json.client.JSONObject");
            composer.addImport("com.google.gwt.json.client.JSONString");
            composer.addImport("java.util.Collection");
            composer.addImport("java.util.List");
            composer.addImport("java.util.Queue");
            composer.addImport("java.util.Set");

            PrintWriter printWriter = context.tryCreate(logger,
                    packageName, className);
            SourceWriter writer = composer.createSourceWriter(context,
                    printWriter);
            
            writer.println("public " + type.getName() 
            		+ " fromJSON(JSONObject object) {");
            writer.indent();
            
            writer.outdent();
            writer.println("}");

            return packageName + "." + className;
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Type.ERROR, e.getMessage());
            throw new UnableToCompleteException();
        }
    }


}
