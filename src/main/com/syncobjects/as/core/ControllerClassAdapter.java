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
public class ControllerClassAdapter extends ClassAdapter implements Opcodes {
	private ControllerReflector cr;
	private boolean clinitEnhanced;

	public ControllerClassAdapter(ClassVisitor cv, ControllerReflector cr) {
		super(cv);
		this.cr = cr;
		this.clinitEnhanced = false;
	}

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		String ninterfaces[] = new String[interfaces.length + 1];
		System.arraycopy(interfaces, 0, ninterfaces, 0, interfaces.length);
		ninterfaces[ninterfaces.length - 1] = Type.getInternalName(IController.class);
		cv.visit(version, access, name, signature, superName, ninterfaces);
	}
	
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String exceptions[]) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		if(name.equals("<clinit>")) {
			clinitEnhanced = true;
			mv = new ControllerStaticMethodAdapter(mv, cr);
		}
		return mv;
	}

	public void visitEnd() {
		FieldVisitor fv;
		MethodVisitor mv;
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asActions", "Ljava/util/Map;",
					"Ljava/util/Map<Ljava/lang/String;Ljava/lang/reflect/Method;>;", null);
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
		{
			fv = cv.visitField(ACC_PRIVATE + ACC_STATIC, "_asUrl",
					"Ljava/lang/String;", null, null);
			fv.visitEnd();
		}
		
		/* 
		 * static {} / <clinit> METHOD
		 */
		if(!clinitEnhanced)
		{
			mv = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			mv = new ControllerStaticMethodAdapter(mv, cr);
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		
		/*
		 * AS ACTIONS
		 */
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asActions", "()Ljava/util/Map;",
					"()Ljava/util/Map<Ljava/lang/String;Ljava/lang/reflect/Method;>;", null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asActions", "Ljava/util/Map;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asFields", "()Ljava/util/Map;",
					"()Ljava/util/Map<Ljava/lang/String;Ljava/lang/Class<*>;>;", null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asFields", "Ljava/util/Map;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGetters", "()Ljava/util/Map;", 
					"()Ljava/util/Map<Ljava/lang/String;Ljava/lang/reflect/Method;>;", null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asGetters", "Ljava/util/Map;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGettersApplicationContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asGettersApplicationContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGettersCookieContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asGettersCookieContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGettersErrorContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asGettersErrorContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGettersMessageContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asGettersMessageContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGettersRequestContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asGettersRequestContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asGettersSessionContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asGettersSessionContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSetters", "()Ljava/util/Map;", "()Ljava/util/Map<Ljava/lang/String;Ljava/lang/reflect/Method;>;", null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asSetters", "Ljava/util/Map;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSettersApplicationContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asSettersApplicationContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSettersCookieContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asSettersCookieContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSettersErrorContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asSettersErrorContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSettersMessageContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asSettersMessageContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSettersRequestContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asSettersRequestContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asSettersSessionContext", "()Ljava/lang/reflect/Method;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asSettersSessionContext", "Ljava/lang/reflect/Method;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cv.visitMethod(ACC_PUBLIC, "_asUrl", "()Ljava/lang/String;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, cr.getClazzInternalName(), "_asUrl", "Ljava/lang/String;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", cr.getClazzDescriptor(), null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		cv.visitEnd();
	}
}
