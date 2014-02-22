package com.github.Saposhiente.ClassDefSubstitutor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * With ASM, can generate the bytecode required to update ClassDefSubstitutor, if a Java update breaks it.
 * @author Saposhiente
 */
public class SubstitutorTester {
    static final Logger logger = Logger.getLogger("SubstitutorTester");
    public static void printBytes(byte[] bytes, Logger logger) {
        StringBuilder builder = new StringBuilder("{");
        int last = bytes.length - 1;
        for (int i = 0; i < last; i++) {
            builder.append(bytes[i]).append(", ");
        }
        builder.append(bytes[last]).append("};");
        logger.info(builder.toString());
    }
    public static void main(String[] args) throws IOException {//testing method
        ClassDefSubstitutor instance = new ClassDefSubstitutor(logger);
        /*//net.cerberusstudios.llama.runecraft.Runecraft.class.getDeclaredFields() throws NoClassDefFoundError
        instance.substituteClassDefs(net.cerberusstudios.llama.runecraft.Runecraft.class);
        net.cerberusstudios.llama.runecraft.Runecraft.class.getDeclaredFields(); //now works*/
        byte[][] surroundingBytes = getClassTemplate();
        printBytes(surroundingBytes[0], logger);
        printBytes(surroundingBytes[1], logger);
        logger.log(Level.INFO, "{0}", java.util.Arrays.equals(surroundingBytes[0], ClassDefSubstitutor.classTemplateStartBytes));
        logger.log(Level.INFO, "{0}", java.util.Arrays.equals(surroundingBytes[1], ClassDefSubstitutor.classTemplateEndBytes));
    }
    private static final String randomInternalName = "LYgltwEOOED/gNQrfssiDjSVJuTKg/ZHjrZuDPbkurpigGquM/ToHaWxebTukXoAU"; //random class name inputted and searched for by getClassTemplate()
    /**
     * requires ASM, determines the correct values for classTemplateStartBytes and classTemplateEndBytes
     * @return byte[][]{classTemplateStartBytes, classTemplateEndBytes}
     * @throws IOException
     */
    public static byte[][] getClassTemplate() throws IOException { 
        final String targetBytecodeName = randomInternalName.substring(1);
        final String targetClassFullName = randomInternalName.replace('/', '.');
        final String targetClassName = targetClassFullName.substring(1);
        final org.objectweb.asm.ClassWriter writer = new org.objectweb.asm.ClassWriter(0);
        //writer.visit(51, 33, targetBytecodeName, null, "java/lang/Object", new String[]{});//may break with Java updates, use log below for correct numbers
        //writer.visitEnd(); 
        if (substReader == null) {
            loadSubstReader();
        }
        final org.objectweb.asm.ClassVisitor renamer = new org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM4, null) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                logger.log(Level.INFO, "visit({0}, {1}, targetBytecodeName, {2}, {3}, new String[]{})", new Object[]{version, access, signature, superName});
                writer.visit(version, access, targetBytecodeName, signature, superName, interfaces);
            }
            @Override
            public void visitEnd() {
                writer.visitEnd();
            }
            
        };
        substReader.accept(renamer, org.objectweb.asm.ClassReader.SKIP_DEBUG + org.objectweb.asm.ClassReader.SKIP_FRAMES);
        final byte[] bytes = writer.toByteArray();
        logger.info("Testing...");
        ClassDefSubstitutor.loadClass(targetClassName, bytes);
        logger.info("Test succeeded.");
        final byte[] searchBytes = targetBytecodeName.getBytes(Charset.forName("UTF8"));
        int foundPos = -1;
        outer:
        for (int i = 0; i < bytes.length; i++) { // loop over source array
            for (int j = 0; ; j++) { // check if target matches source from element i
                if (bytes[i + j] != searchBytes[j]) {
                    break;
                }
                if (j >= 63) { //length is always 64
                    foundPos = i;
                    break outer;
                }
            }
        }
        if (foundPos == -1) {
            logger.info(new String(bytes));
            throw new RuntimeException("Did not find bytes!");
        }
        final byte[] startBytes = new byte[foundPos - 2]; //skip the length number
        int endPos = foundPos + 64;
        final int endLength = bytes.length - endPos;
        final byte[] endBytes = new byte[endLength];
        System.arraycopy(bytes, 0, startBytes, 0, foundPos - 2);
        System.arraycopy(bytes, endPos, endBytes, 0, endLength);
        return new byte[][]{startBytes, endBytes};
    }
    static org.objectweb.asm.ClassReader substReader = null;
    public static void loadSubstReader() throws IOException {
        substReader = new org.objectweb.asm.ClassReader(SubstitutorTester.class.getResourceAsStream("/com/github/Saposhiente/ClassDefSubstitutor/SubstituteClass.class"));
        //substReader = new org.objectweb.asm.ClassReader(new java.io.FileInputStream("/path/to/SubstituteClass.class"));// SubstituteClass.class source code: public class A {}
    }
}