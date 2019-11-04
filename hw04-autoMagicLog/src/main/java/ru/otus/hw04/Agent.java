package ru.otus.hw04;


import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public class Agent {


        public static void premain(String agentArgs, Instrumentation inst) {
            inst.addTransformer(new ClassFileTransformer() {
                @Override
                public byte[] transform(ClassLoader loader, String className,
                                        Class<?> classBeingRedefined,
                                        ProtectionDomain protectionDomain,
                                        byte[] classfileBuffer) {
                    if (className.equals("ru/otus/hw04/TestLogging")) {
                        return addProxyMethod(classfileBuffer);
                    }
                    return classfileBuffer;
                }
            });

        }

        private static byte[] addProxyMethod(byte[] originalClass) {
            ClassReader cr = new ClassReader(originalClass);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if (name.equals("calculation")) {
                        return super.visitMethod(access, "calculationProxied", descriptor, signature, exceptions);
                    } else {
                        return super.visitMethod(access, name, descriptor, signature, exceptions);
                    }
                }
            };
            cr.accept(cv, Opcodes.ASM5);

            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "calculation", "(Ljava/lang/Integer;)V", null, null);

            Handle handle = new Handle(
                    H_INVOKESTATIC,
                    Type.getInternalName(java.lang.invoke.StringConcatFactory.class),
                    "makeConcatWithConstants",
                    MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class, String.class, Object[].class).toMethodDescriptorString(),
                    false);

            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitInvokeDynamicInsn("makeConcatWithConstants", "(Ljava/lang/Integer;)Ljava/lang/String;", handle, "executed method: calculation, param:\u0001");

            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "ru/otus/hw04/TestLogging", "calculationProxied", "(Ljava/lang/Integer;)V", false);

            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();


            byte[] finalClass = cw.toByteArray();

            try (OutputStream fos = new FileOutputStream("proxyASM.class")) {
                fos.write(finalClass);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return finalClass;
        }

    }

