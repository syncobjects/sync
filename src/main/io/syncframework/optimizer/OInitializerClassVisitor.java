/*
 * Copyright 2016 SyncObjects Ltda.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syncframework.optimizer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import io.syncframework.api.ApplicationContext;

/**
 * @author dfroz
 */
public class OInitializerClassVisitor extends ClassVisitor {
	private OInitializerReflector reflector;

	public OInitializerClassVisitor(ClassVisitor cv, OInitializerReflector reflector) {
		super(Opcodes.ASM5, cv);
		this.reflector = reflector;
	}
	
	/**
	 * This method will include the IController interface to the class definition.
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		String ninterfaces[] = new String[interfaces.length + 1];
		System.arraycopy(interfaces, 0, ninterfaces, 0, interfaces.length);
		ninterfaces[ninterfaces.length - 1] = Type.getInternalName(OInitializer.class);
		cv.visit(version, access, name, signature, superName, ninterfaces);
	}
	
	/**
	 * Add code to the end of the class. We are adding the IController methods
	 * @see org.objectweb.asm.ClassVisitor#visitEnd()
	 */
	@Override
	public void visitEnd() {		
		createContextMethod("_asApplicationContext", Type.getDescriptor(ApplicationContext.class), reflector.getApplicationContext());
		createInitMethod();
		createDestroyMethod();
	}
	
	/**
	 * Generates the code:
	 * 
	 * public void _asDestroy() {
	 * 	return destroy();
	 * }
	 */
	public void createDestroyMethod() {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asDestroy", "()V", null, null);
		Label l0 = new Label();
		if(reflector.getInit() != null) {
			mv.visitLabel(l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, reflector.getClazzInternalName(), "destroy", "()V", false);
			mv.visitInsn(Opcodes.RETURN);
		}
		else {
			mv.visitLabel(l0);
			mv.visitInsn(Opcodes.RETURN);
		}
		
		Label l1 = new Label();
		mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
	
	/**
	 * Generates the code:
	 * 
	 * public void _asInit() {
	 * 	return init();
	 * }
	 */
	public void createInitMethod() {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asInit", "()V", null, null);
		Label l0 = new Label();
		if(reflector.getInit() != null) {
			mv.visitLabel(l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, reflector.getClazzInternalName(), "init", "()V", false);
			mv.visitInsn(Opcodes.RETURN);
		}
		else {
			mv.visitLabel(l0);
			mv.visitInsn(Opcodes.RETURN);
		}
		Label l1 = new Label();
		mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
	
	/**
	 * Generates Contexts setter method: 
	 * 
	 * public void _as(Application|Error|Message|...)Context((Application|Error|Message|...)Context variable) {
	 * 	this.variable = variable
	 * 		or
	 * 	// do nothing
	 * }
	 */
	private void createContextMethod(String methodName, String typeDescriptor, String variableName) {
		if(variableName != null) {
			MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, methodName, "("+typeDescriptor+")V", null, null);
			Label l0 = new Label();
			Label l1 = new Label();
			Label l2 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitFieldInsn(Opcodes.PUTFIELD, reflector.getClazzInternalName(), variableName, typeDescriptor);
			mv.visitLabel(l1);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(l2);
			mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, l0, l2, 0);
			mv.visitLocalVariable(variableName, typeDescriptor, null, l0, l2, 1);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		else {
			MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, methodName, "("+typeDescriptor+")V", null, null);
			Label l0 = new Label();
			Label l1 = new Label();
			mv.visitLabel(l0);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitLocalVariable("argument", typeDescriptor, null, l0, l1, 1);
		}
	}
}