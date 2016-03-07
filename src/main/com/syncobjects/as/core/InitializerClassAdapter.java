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
package com.syncobjects.as.core;

import com.syncobjects.asm.ClassAdapter;
import com.syncobjects.asm.ClassVisitor;
import com.syncobjects.asm.FieldVisitor;
import com.syncobjects.asm.Label;
import com.syncobjects.asm.MethodVisitor;
import com.syncobjects.asm.Opcodes;
import com.syncobjects.asm.Type;

/**
 * 
 * @author dfroz
 *
 */
public class InitializerClassAdapter extends ClassAdapter implements Opcodes {
	private InitializerReflector ir;
	private boolean clinitEnhanced;

	public InitializerClassAdapter(ClassVisitor cv, InitializerReflector ir) {
		super(cv);
		this.ir = ir;
		this.clinitEnhanced = false;
	}

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		String ninterfaces[] = new String[interfaces.length + 1];
		System.arraycopy(interfaces, 0, ninterfaces, 0, interfaces.length);
		ninterfaces[ninterfaces.length - 1] = Type.getInternalName(IInitializer.class);
		cv.visit(version, access, name, signature, superName, ninterfaces);
	}
	
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String exceptions[]) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		if(name.equals("<clinit>")) {
			clinitEnhanced = true;
			mv = new InitializerStaticMethodAdapter(mv, ir);
		}
		return mv;
	}

	public void visitEnd() {
		FieldVisitor fv;
		MethodVisitor mv;
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asDestroy",
					"Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asGettersApplicationContext",
					"Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asGettersErrorContext",
					"Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asGettersMessageContext", 
					"Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asInit",
					"Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asSettersApplicationContext",
					"Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asSettersErrorContext",
					"Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asSettersMessageContext",
					"Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		
		/* 
		 * static {} / <clinit> METHOD
		 */
		if(!clinitEnhanced)
		{
			mv = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			mv = new InitializerStaticMethodAdapter(mv, ir);
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		
		/*
		 * AS METHODS
		 */
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asDestroy", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asDestroy", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGettersApplicationContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asGettersApplicationContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGettersErrorContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asGettersErrorContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGettersMessageContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asGettersMessageContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asInit", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asInit", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSettersApplicationContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asSettersApplicationContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSettersErrorContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asSettersErrorContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSettersMessageContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asSettersMessageContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		cv.visitEnd();
	}
}
