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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author dfroz
 */
public class OInterceptorStaticMethodVisitor extends MethodVisitor {
	private OInterceptorReflector reflector;

	public OInterceptorStaticMethodVisitor(MethodVisitor mv, OInterceptorReflector reflector) {
		super(Opcodes.ASM5, mv);
		this.reflector = reflector;
	}
	
	public void visitInsn(int opcode) {
		if(opcode != Opcodes.RETURN) {
			mv.visitInsn(opcode);
			return;
		}
		
		Label start = new Label();
		Label l1 = new Label();
		Label l2 = new Label();
		
		mv.visitCode();
		mv.visitTryCatchBlock(start, l1, l2, "java/lang/Throwable");
		
		/*
		 * _asAfterType = "application/json";
		 */
		{
			mv.visitLabel(start);
			mv.visitLdcInsn(reflector.getAfterType());
			mv.visitFieldInsn(Opcodes.PUTSTATIC, reflector.getClazzInternalName(), "_asAfterType", "Ljava/lang/String;");
		}
		/*
		 * _asBeforeType = "text/html";
		 */
		{
			Label l = new Label();
			mv.visitLabel(l);
			mv.visitLdcInsn(reflector.getBeforeType());
			mv.visitFieldInsn(Opcodes.PUTSTATIC, reflector.getClazzInternalName(), "_asBeforeType", "Ljava/lang/String;");
		}
		/* 
		 * _asParameters = new HashMap<String,Class<?>>() 
		 */
		{
			Label l = new Label();
			mv.visitLabel(l);
			mv.visitTypeInsn(Opcodes.NEW, "java/util/HashMap");
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
			mv.visitFieldInsn(Opcodes.PUTSTATIC, reflector.getClazzInternalName(), "_asParameters", "Ljava/util/Map;");
		}
		/*
		 * _asParameters.put("name", Type.class);
		 */
		for(String name: reflector.getParameters().keySet()) {
			Label l = new Label();
			mv.visitLabel(l);
			mv.visitFieldInsn(Opcodes.GETSTATIC, reflector.getClazzInternalName(), "_asParameters", "Ljava/util/Map;");
			mv.visitLdcInsn(name);
			mv.visitLdcInsn(Type.getType(reflector.getParameters().get(name)));
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
			mv.visitInsn(Opcodes.POP);
		}
		
		/* 
		 * _asConverters = new HashMap<String,Class<?>>() 
		 */
		{
			Label l = new Label();
			mv.visitLabel(l);
			mv.visitTypeInsn(Opcodes.NEW, "java/util/HashMap");
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
			mv.visitFieldInsn(Opcodes.PUTSTATIC, reflector.getClazzInternalName(), "_asConverters", "Ljava/util/Map;");
		}
		/*
		 * _asConverters.put("name", Type.class);
		 */
		for(String name: reflector.getParameters().keySet()) {
			if(reflector.getConverters().get(name) != null) {
				Class<?> converter = reflector.getConverters().get(name);
				Label l = new Label();
				mv.visitLabel(l);
				mv.visitFieldInsn(Opcodes.GETSTATIC, reflector.getClazzInternalName(), "_asConverters", "Ljava/util/Map;");
				mv.visitLdcInsn(name);
				mv.visitLdcInsn(Type.getType(converter));
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
				mv.visitInsn(Opcodes.POP);
			}
		}

		/*
		 * }
		 * catch(Throwable t) {
		 * 	throw t;
		 * }
		 */
		Label throwableStart = new Label();
		Label throwableEnd = new Label();
		
		mv.visitLabel(l1);
		mv.visitJumpInsn(Opcodes.GOTO, throwableEnd);
		
		mv.visitLabel(l2);
		mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{ "java/lang/Throwable" });
		mv.visitVarInsn(Opcodes.ASTORE, 0);
		
		mv.visitLabel(throwableStart);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitInsn(Opcodes.ATHROW);
		
		mv.visitLabel(throwableEnd);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitLocalVariable("t", "Ljava/lang/Throwable;", null, throwableStart, throwableEnd, 0);
	}
}
