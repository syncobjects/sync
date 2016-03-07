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

import java.lang.reflect.Method;

import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.ErrorContext;
import com.syncobjects.as.api.MessageContext;
import com.syncobjects.asm.Label;
import com.syncobjects.asm.MethodAdapter;
import com.syncobjects.asm.MethodVisitor;
import com.syncobjects.asm.Opcodes;
import com.syncobjects.asm.Type;

/**
 * 
 * @author dfroz
 *
 */
public class InitializerStaticMethodAdapter extends MethodAdapter implements Opcodes {
	private InitializerReflector ir;

	public InitializerStaticMethodAdapter(MethodVisitor mv, InitializerReflector ir) {
		super(mv);
		this.ir = ir;
	}

	public void visitInsn(int opcode) {
		if(opcode != RETURN) {
			mv.visitInsn(opcode);
			return;
		}

		// do the magic and then call RETURN/mv.visitInsn(opcode)
		Label l0 = new Label();
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
		mv.visitLabel(l0);
		mv.visitInsn(ACONST_NULL);
		mv.visitVarInsn(ASTORE, 0);

		// _asInit = getMethod("init");
		{
			Label l11 = new Label();
			mv.visitLabel(l11);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(ir.getInit().getName());
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asInit",
					"Ljava/lang/reflect/Method;");
		}
		// _asDestroy = getMethod("destroy");
		{
			Label l11 = new Label();
			mv.visitLabel(l11);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(ir.getDestroy().getName());
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asDestroy",
					"Ljava/lang/reflect/Method;");
		}

		/* 
		 * SPECIALS 
		 */
		Method method;
		method = ir.getGettersApplicationContext();
		if(method != null) {
			Label l15 = new Label();
			mv.visitLabel(l15);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(method.getName());
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asGettersApplicationContext",
					"Ljava/lang/reflect/Method;");
		}
		method = ir.getSettersApplicationContext();
		if(method != null) {
			Label l16 = new Label();
			mv.visitLabel(l16);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(method.getName());
			mv.visitInsn(ICONST_1);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_0);
			mv.visitLdcInsn(Type.getType(ApplicationContext.class));
			mv.visitInsn(AASTORE);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", 
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asSettersApplicationContext", 
					"Ljava/lang/reflect/Method;");
		}
		method = ir.getGettersErrorContext();
		if(method != null) {
			Label l17 = new Label();
			mv.visitLabel(l17);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(method.getName());
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asGettersErrorContext",
					"Ljava/lang/reflect/Method;");
		}
		method = ir.getSettersErrorContext();
		if(method != null) {
			Label l18 = new Label();
			mv.visitLabel(l18);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(method.getName());
			mv.visitInsn(ICONST_1);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_0);
			mv.visitLdcInsn(Type.getType(ErrorContext.class));
			mv.visitInsn(AASTORE);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", 
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asSettersErrorContext", 
					"Ljava/lang/reflect/Method;");
		}
		method = ir.getGettersMessageContext();
		if(method != null) {
			Label l19 = new Label();
			mv.visitLabel(l19);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(method.getName());
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asGettersMessageContext",
					"Ljava/lang/reflect/Method;");
		}
		method = ir.getSettersMessageContext();
		if(method != null) {
			Label l20 = new Label();
			mv.visitLabel(l20);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(method.getName());
			mv.visitInsn(ICONST_1);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_0);
			mv.visitLdcInsn(Type.getType(MessageContext.class));
			mv.visitInsn(AASTORE);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", 
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_sharkSettersMessageContext", 
					"Ljava/lang/reflect/Method;");
		}

		mv.visitLabel(l1);
		Label l4 = new Label();
		mv.visitJumpInsn(GOTO, l4);
		mv.visitLabel(l2);
		mv.visitFrame(F_SAME1, 0, null, 1, new Object[]{"java/lang/Throwable"});
		mv.visitVarInsn(ASTORE, 0);

		Label l5 = new Label();
		mv.visitLabel(l5);
		mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V");
		mv.visitInsn(ATHROW);
		mv.visitLabel(l4);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitInsn(RETURN);
		mv.visitLocalVariable("t", "Ljava/lang/Throwable;", null, l5, l4, 0);
	}
}
