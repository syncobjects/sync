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
import com.syncobjects.as.api.CookieContext;
import com.syncobjects.as.api.ErrorContext;
import com.syncobjects.as.api.MessageContext;
import com.syncobjects.as.api.RequestContext;
import com.syncobjects.as.api.SessionContext;
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
public class InterceptorStaticMethodAdapter extends MethodAdapter implements Opcodes {
	private InterceptorReflector ir;

	public InterceptorStaticMethodAdapter(MethodVisitor mv, InterceptorReflector ir) {
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
		
		/* _asAfter = getMethod("after"); */
		{
			Label l00 = new Label();
			mv.visitLabel(l00);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(ir.getAfter().getName());
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asAfter",
					"Ljava/lang/reflect/Method;");
		}
		/* _asBefore = getMethod("before"); */
		{
			Label l00 = new Label();
			mv.visitLabel(l00);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(ir.getBefore().getName());
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asBefore",
					"Ljava/lang/reflect/Method;");
		}
		/* _asFields = new HashMap<String,Class<?>(); */
		{
			Label l10 = new Label();
			mv.visitLabel(l10);
			mv.visitTypeInsn(NEW, "java/util/HashMap");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asFields", "Ljava/util/Map;");
		}
		/* _asGetters = new HashMap<String,Method>() */
		{
			Label l11 = new Label();
			mv.visitLabel(l11);
			mv.visitTypeInsn(NEW, "java/util/HashMap");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asGetters", "Ljava/util/Map;");
		}
		/* _asSetters = new HashMap<String,Method>() */
		{
			Label l12 = new Label();
			mv.visitLabel(l12);
			mv.visitTypeInsn(NEW, "java/util/HashMap");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asSetters", "Ljava/util/Map;");
		}
		/* Class<?> type = null; */
		Label l14 = new Label();
		mv.visitLabel(l14);
		mv.visitInsn(ACONST_NULL);
		mv.visitVarInsn(ASTORE, 0);

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
		method = ir.getGettersCookieContext();
		if(method != null) {
			Label l17 = new Label();
			mv.visitLabel(l17);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(method.getName());
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asGettersCookieContext",
					"Ljava/lang/reflect/Method;");
		}
		method = ir.getSettersCookieContext();
		if(method != null) {
			Label l18 = new Label();
			mv.visitLabel(l18);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(method.getName());
			mv.visitInsn(ICONST_1);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_0);
			mv.visitLdcInsn(Type.getType(CookieContext.class));
			mv.visitInsn(AASTORE);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", 
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asSettersCookieContext", 
					"Ljava/lang/reflect/Method;");
		}
		method = ir.getGettersErrorContext();
		if(method != null) {
			Label l19 = new Label();
			mv.visitLabel(l19);
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
			Label l20 = new Label();
			mv.visitLabel(l20);
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
			Label l21 = new Label();
			mv.visitLabel(l21);
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
			Label l22 = new Label();
			mv.visitLabel(l22);
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
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asSettersMessageContext", 
					"Ljava/lang/reflect/Method;");
		}
		method = ir.getGettersRequestContext();
		if(method != null) {
			Label l23 = new Label();
			mv.visitLabel(l23);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(method.getName());
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asGettersRequestContext",
					"Ljava/lang/reflect/Method;");
		}
		method = ir.getSettersRequestContext();
		if(method != null) {
			Label l24 = new Label();
			mv.visitLabel(l24);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(method.getName());
			mv.visitInsn(ICONST_1);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_0);
			mv.visitLdcInsn(Type.getType(RequestContext.class));
			mv.visitInsn(AASTORE);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", 
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asSettersRequestContext", 
					"Ljava/lang/reflect/Method;");
		}
		method = ir.getGettersSessionContext();
		if(method != null) {
			Label l25 = new Label();
			mv.visitLabel(l25);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(method.getName());
			mv.visitInsn(ICONST_0);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asGettersSessionContext",
					"Ljava/lang/reflect/Method;");
		}
		method = ir.getSettersSessionContext();
		if(method != null) {
			Label l26 = new Label();
			mv.visitLabel(l26);
			mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
			mv.visitLdcInsn(method.getName());
			mv.visitInsn(ICONST_1);
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_0);
			mv.visitLdcInsn(Type.getType(SessionContext.class));
			mv.visitInsn(AASTORE);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", 
					"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
			mv.visitFieldInsn(PUTSTATIC, ir.getClazzInternalName(), "_asSettersSessionContext", 
					"Ljava/lang/reflect/Method;");
		}

		/* 
		 * FIELDS 
		 */
		for(String fieldName: ir.getFields().keySet()) {
			if(fieldName.indexOf('.') != -1)
				continue;
			{
				Label l27 = new Label();
				mv.visitLabel(l27);
				mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
				mv.visitLdcInsn(fieldName);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField", 
						"(Ljava/lang/String;)Ljava/lang/reflect/Field;");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "getType", "()Ljava/lang/Class;");
				mv.visitVarInsn(ASTORE, 0);
			}
			{
				Label l28 = new Label();
				mv.visitLabel(l28);
				mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asFields", "Ljava/util/Map;");
				mv.visitLdcInsn(fieldName);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", 
						"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
				mv.visitInsn(POP);
			}
			{
				Label l29 = new Label();
				mv.visitLabel(l29);
				mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asGetters", "Ljava/util/Map;");
				mv.visitLdcInsn(fieldName);
				mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
				mv.visitLdcInsn(ir.getGetters().get(fieldName).getName());
				mv.visitInsn(ICONST_0);
				mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", 
						"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
				mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put",
						"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
				mv.visitInsn(POP);
			}
			{
				Label l30 = new Label();
				mv.visitLabel(l30);
				mv.visitFieldInsn(GETSTATIC, ir.getClazzInternalName(), "_asSetters", "Ljava/util/Map;");
				mv.visitLdcInsn(fieldName);
				mv.visitLdcInsn(Type.getType(ir.getClazzDescriptor()));
				mv.visitLdcInsn(ir.getSetters().get(fieldName).getName());
				mv.visitInsn(ICONST_1);
				mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
				mv.visitInsn(DUP);
				mv.visitInsn(ICONST_0);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitInsn(AASTORE);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", 
						"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
				mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put",
						"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
				mv.visitInsn(POP);
			}
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
