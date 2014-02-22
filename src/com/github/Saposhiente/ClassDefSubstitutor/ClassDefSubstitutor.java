package com.github.Saposhiente.ClassDefSubstitutor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Saposhiente
 */
public class ClassDefSubstitutor {
    private final Logger logger;

    public ClassDefSubstitutor(Logger logger) {
        this.logger = logger;
    }
    
    private static final Method defineClass; //probably going to call it a lot, cache it.
    static {
        try {
            defineClass = ClassLoader.class.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class, int.class});
            defineClass.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException("Could not load class definer: ", ex);
        }
    }
    public static Class loadClass(String className, byte[] bytecode) {
        try {
            return (Class) defineClass.invoke(ClassLoader.getSystemClassLoader(), new Object[]{className, bytecode, 0, bytecode.length});
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException("Could not load class " + className + ": ", ex);
        }
    }
    /**
     * Start of the template for a blank class to use in class substitution.
     */
    static final byte[] classTemplateStartBytes = new byte[]{-54, -2, -70, -66, 0, 0, 0, 51, 0, 5, 1};
    /**
     * End of the template for a blank class to use in class substitution.
     * @see #classTemplateStartBytes
     */
    static final byte[] classTemplateEndBytes = new byte[]{7, 0, 1, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 7, 0, 3, 0, 33, 0, 2, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0};
    /**
     * Creates a the bytecode for a blank class with the specified name
     * @param name The name of the class
     * @see #classTemplateStartBytes
     */
    public static byte[] createBytecodeFromTemplate(final String name) {
        final byte[] nameBytes = name.getBytes(Charset.forName("UTF8"));
        if (nameBytes.length > 255) {
            throw new IllegalArgumentException("Can't handle class names longer than 255 bytes! (" + name + ")");
        }
        final byte[] bytes = new byte[classTemplateStartBytes.length + 2 + nameBytes.length + classTemplateEndBytes.length];
        System.arraycopy(classTemplateStartBytes, 0, bytes, 0, classTemplateStartBytes.length);
        bytes[classTemplateStartBytes.length] = (byte) (nameBytes.length >>> 8); 
        bytes[classTemplateStartBytes.length + 1] = (byte) nameBytes.length; 
        System.arraycopy(nameBytes, 0, bytes, classTemplateStartBytes.length + 2, nameBytes.length);
        System.arraycopy(classTemplateEndBytes, 0, bytes, classTemplateStartBytes.length + nameBytes.length + 2, classTemplateEndBytes.length);
        return bytes;
    }
    /**
     * Creates a new, blank class with the missing class's name and package
     * 
     * @param error The NoClassDefFoundError produced because of the missing class
     */
    public void substituteClassDef(NoClassDefFoundError error) {
        substituteClassDef(error, logger);
    }
    public static void substituteClassDef(NoClassDefFoundError error, Logger logger) {
        final String target = error.toString();
        final String targetBytecodeName;
        if (target.endsWith(";")) {
            /*final String targetInternalName = target.substring(target.lastIndexOf(" ") + 1, target.length()-1); Old version code
            targetBytecodeName = targetInternalName.substring(1);*/
            targetBytecodeName = target.substring(target.lastIndexOf(" ") + 2, target.length() - 1);
        } else {
            targetBytecodeName = target.substring(target.lastIndexOf(" ") + 1);
        }
        /*final String targetClassFullName = targetInternalName.replace('/', '.');
        final String targetClassName = targetClassFullName.substring(1);*/
        final String targetClassName = targetBytecodeName.replace('/', '.');
        logger.log(Level.INFO, "Creating substitution class {0}", targetBytecodeName);
        loadClass(targetClassName, createBytecodeFromTemplate(targetBytecodeName));
    }
    public void substituteClassDefs(Class clazz) {
        substituteClassDefs(clazz, logger);
    }
    public static void substituteClassDefs(Class clazz, Logger logger) {
        substituteFieldDefs(clazz, 0, logger);
        substituteMethodDefs(clazz, 0, logger); //do one at a time so that getFields() isn't called repeatedly once it begins to succeed
        substituteConstructorDefs(clazz, 0, logger);
        if (clazz.getSuperclass() != null) {
            substituteClassDefs(clazz.getSuperclass(), logger);
        }
    }
    public static void substituteFieldDefs(Class clazz, int recursions, Logger logger) {
        try {
            clazz.getDeclaredFields(); 
        } catch (NoClassDefFoundError ex) {
            substituteClassDef(ex, logger);
            recursions++;
            if (recursions > 100) {
                throw new RuntimeException("Too many class def substitutions for fields within class " + clazz.getSimpleName() + "! Aborting substitution process.");
            }
            substituteFieldDefs(clazz, recursions, logger);
        }
    }
    public static void substituteMethodDefs(Class clazz, int recursions, Logger logger) {
        try {
            clazz.getDeclaredMethods(); 
        } catch (NoClassDefFoundError ex) {
            substituteClassDef(ex, logger);
            recursions++;
            if (recursions > 100) {
                throw new RuntimeException("Too many class def substitutions for methods within class " + clazz.getSimpleName() + "! Aborting substitution process.");
            }
            substituteMethodDefs(clazz, recursions, logger);
        }
    }
    public static void substituteConstructorDefs(Class clazz, int recursions, Logger logger) {
        try {
            clazz.getDeclaredConstructors(); 
        } catch (NoClassDefFoundError ex) {
            substituteClassDef(ex, logger);
            recursions++;
            if (recursions > 100) {
                throw new RuntimeException("Too many method def substitutions for constructors within class " + clazz.getSimpleName() + "! Aborting substitution process.");
            }
            substituteConstructorDefs(clazz, recursions, logger);
        }
    }
}
