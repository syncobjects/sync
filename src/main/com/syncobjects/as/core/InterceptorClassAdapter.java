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
public class InterceptorClassAdapter extends ClassAdapter implements Opcodes {
	private InterceptorReflector ir;
	private boolean clinitEnhanced;

	public InterceptorClassAdapter(ClassVisitor cv, InterceptorReflector ir) {
		super(cv);
		this.ir = ir;
		this.clinitEnhanced = false;
	}

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		String ninterfaces[] = new String[interfaces.length + 1];
		System.arraycopy(interfaces, 0, ninterfaces, 0, interfaces.length);
		ninterfaces[ninterfaces.length - 1] = Type.getInternalName(IInterceptor.class);
		cv.visit(version, access, name, signature, superName, ninterfaces);
	}
	
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String exceptions[]) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		if(name.equals("<clinit>")) {
			clinitEnhanced = true;
			mv = new InterceptorStaticMethodAdapter(mv, ir);
		}
		return mv;
	}

	public void visitEnd() {
		FieldVisitor fv;
		MethodVisitor mv;
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asAfter", "Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asBefore", "Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asFields", "Ljava/util/Map;",
					"Ljava/util/Map<Ljava/lang/String;Ljava/lang/Class<*>;>;", null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asGetters", "Ljava/util/Map;",
					"Ljava/util/Map<Ljava/lang/String;Ljava/lang/reflect/Method;>;", null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asGettersApplicationContext",
					"Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asGettersCookieContext",
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
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asGettersRequestContext",
					"Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asGettersSessionContext",
					"Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asSetters", "Ljava/util/Map;",
					"Ljava/util/Map<Ljava/lang/String;Ljava/lang/reflect/Method;>;", null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asSettersApplicationContext",
					"Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asSettersCookieContext",
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
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asSettersRequestContext",
					"Ljava/lang/reflect/Method;", null, null);
			fv.visitEnd();
		}
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asSettersSessionContext",
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
			mv = new InterceptorStaticMethodAdapter(mv, ir);
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		
		/*
		 * AS ACTIONS
		 */
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asAfter", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asAfter", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asBefore", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asBefore", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asFields", "()Ljava/util/Map;",
					"()Ljava/util/Map<Ljava/lang/String;Ljava/lang/Class<*>;>;", null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asFields", "Ljava/util/Map;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGetters", "()Ljava/util/Map;", 
					"()Ljava/util/Map<Ljava/lang/String;Ljava/lang/reflect/Method;>;", null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asGetters", "Ljava/util/Map;");
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
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asGettersApplicationContext",
					"Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGettersCookieContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asGettersCookieContext", 
					"Ljava/lang/reflect/Method;");
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
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asGettersErrorContext",
					"Ljava/lang/reflect/Method;");
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
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asGettersMessageContext",
					"Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGettersRequestContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asGettersRequestContext",
					"Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGettersSessionContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asGettersSessionContext",
					"Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSetters", "()Ljava/util/Map;",
					"()Ljava/util/Map<Ljava/lang/String;Ljava/lang/reflect/Method;>;", null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asSetters", "Ljava/util/Map;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSettersApplicationContext", 
					"()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asSettersApplicationContext",
					"Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSettersCookieContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asSettersCookieContext",
					"Ljava/lang/reflect/Method;");
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
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asSettersErrorContext",
					"Ljava/lang/reflect/Method;");
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
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asSettersMessageContext", 
					"Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSettersRequestContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asSettersRequestContext", 
					"Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", ir.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSettersSessionContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asSettersSessionContext", 
					"Ljava/lang/reflect/Method;");
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
