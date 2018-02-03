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
 * Generates or add sync code to static {} method:
 * 
 * static {
 * 	try {
 * 		_asActions = new HashMap<String, Boolean>();
 *		_asActions.put("main", true);
 *		_asActionsType.put("main", "text/html");
 *		_asInterceptors = new HashMap<String, Class<?>[]>();
 *		_asInterceptors.put("upload", new Class<?>[] { LoginInterceptor.class, DummyInterceptor.class });
 *		_asInterceptors.put("save", new Class<?>[] { LoginInterceptor.class });
 * 		_asParameters = new HashMap<String, Class<?>>();
 * 		_asParameters.put("name", String.class);
 * 		_asParameters.put("date", Date.class);
 * 		_asConverters = new HashMap<String, Class<?>>();
 * 		_asConverters.put("date", ExampleSimpleDateConverter.class);
 * 	}
 * 	catch(Throwable t) {
 * 		throw t;
 * 	}
 * }
 * 
 * @author dfroz
 */
public class OControllerStaticMethodVisitor extends MethodVisitor {
	private OControllerReflector reflector;

	public OControllerStaticMethodVisitor(MethodVisitor mv, OControllerReflector reflector) {
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
		Label interceptorsLabel = new Label();
		
		mv.visitCode();
		mv.visitTryCatchBlock(start, l1, l2, "java/lang/Throwable");
		
		/*
		 * _asActions = new HashMap<String,Boolean>();
		 */
		{
			mv.visitLabel(start);
			mv.visitTypeInsn(Opcodes.NEW, "java/util/HashMap");
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
			mv.visitFieldInsn(Opcodes.PUTSTATIC, reflector.getClazzInternalName(), "_asActions", "Ljava/util/Map;");
		}
		/*
		 * _asActions.put("main", true);
		 * _asActions.put("action1", true);
		 */
		for(String name: reflector.getActions().keySet()) {
			Label l = new Label();
			mv.visitLabel(l);
			mv.visitFieldInsn(Opcodes.GETSTATIC, reflector.getClazzInternalName(), "_asActions", "Ljava/util/Map;");
			mv.visitLdcInsn(name);
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
			mv.visitInsn(Opcodes.POP);
		}
	    /*
		 * _asActionsType = new HashMap<String,String>();
		 */
		{
			Label l = new Label();
			mv.visitLabel(l);
			mv.visitTypeInsn(Opcodes.NEW, "java/util/HashMap");
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
			mv.visitFieldInsn(Opcodes.PUTSTATIC, reflector.getClazzInternalName(), "_asActionsType", "Ljava/util/Map;");
		}
		for(String name: reflector.getActions().keySet()) {
			Label l = new Label();
			mv.visitLabel(l);
			mv.visitFieldInsn(Opcodes.GETSTATIC, reflector.getClazzInternalName(), "_asActionsType", "Ljava/util/Map;");
			mv.visitLdcInsn(name);
			mv.visitLdcInsn(reflector.getActionsType().get(name));
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
			mv.visitInsn(Opcodes.POP);
		}
		
		/*
		 * _asInterceptors = new HashMap<String,Class<?>[]>()
		 */
		{
			Label l = new Label();
			mv.visitLabel(l);
			mv.visitTypeInsn(Opcodes.NEW, "java/util/HashMap");
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
			mv.visitFieldInsn(Opcodes.PUTSTATIC, reflector.getClazzInternalName(), "_asInterceptors", "Ljava/util/Map;");
		}
		
		/*
		 * List<Class<?>> l = new ArrayList<Class<?>>();
		 */
		mv.visitLabel(interceptorsLabel);
		mv.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
		mv.visitInsn(Opcodes.DUP);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
		mv.visitVarInsn(Opcodes.ASTORE, 0);
		
		for(String name: reflector.getActions().keySet()) {
			Class<?> interceptors[] = reflector.getInterceptors().get(name);
			if(interceptors == null || interceptors.length == 0)
				continue;
			
			/*
			 * l.clear();
			 */
			Label l01 = new Label();
			mv.visitLabel(l01);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "clear", "()V", true);
			
			for(Class<?> interceptor: interceptors) {
				/*
				 * l.add(LoginInterceptor.class);
				 */
				Label l02 = new Label();
				mv.visitLabel(l02);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitLdcInsn(Type.getType(interceptor));
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
			}
			
			/*
			 * _asInterceptors.put("upload", l.toArray(new Class[0]));
			 */
			Label l03 = new Label();
			mv.visitLabel(l03);
			mv.visitFieldInsn(Opcodes.GETSTATIC, reflector.getClazzInternalName(), "_asInterceptors", "Ljava/util/Map;");
			mv.visitLdcInsn(name);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class");
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;", true);
			mv.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Class;");
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
			mv.visitInsn(Opcodes.POP);
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
		mv.visitLocalVariable("l", "Ljava/util/List;", null, interceptorsLabel, l1, 0);
		mv.visitLocalVariable("t", "Ljava/lang/Throwable;", null, throwableStart, throwableEnd, 0);
	}
}
