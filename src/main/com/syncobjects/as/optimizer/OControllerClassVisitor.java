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
package com.syncobjects.as.optimizer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.syncobjects.as.api.ApplicationContext;
import com.syncobjects.as.api.CookieContext;
import com.syncobjects.as.api.ErrorContext;
import com.syncobjects.as.api.MessageContext;
import com.syncobjects.as.api.RequestContext;
import com.syncobjects.as.api.Result;
import com.syncobjects.as.api.SessionContext;

/**
 * @author dfroz
 */
public class OControllerClassVisitor extends ClassVisitor {
	private OControllerReflector reflector;
	private boolean createdStaticMethod = false;

	public OControllerClassVisitor(ClassVisitor cv, OControllerReflector reflector) {
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
		ninterfaces[ninterfaces.length - 1] = Type.getInternalName(OController.class);
		cv.visit(version, access, name, signature, superName, ninterfaces);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String exceptions[]) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		if(name.equals("<clinit>")) {
			createdStaticMethod = true;
			//
			// just add code to the end of the static Method
			//
			mv = new OControllerStaticMethodVisitor(mv, reflector);
		}
		return mv;
	}

	/**
	 * Add code to the end of the class. We are adding the IController methods
	 * @see org.objectweb.asm.ClassVisitor#visitEnd()
	 */
	@Override
	public void visitEnd() {
		//
		// private static Map<String,Boolean> _asActions;
		//
		{
			// private static Ljava/util/Map; _asActions
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "_asActions", "Ljava/util/Map;", 
					"Ljava/util/Map<Ljava/lang/String;Ljava/lang/Boolean;>;", null);
			fv.visitEnd();
		}
		//
		// private static Map<String,Class<?>> _asConverters;
		//
		{
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "_asConverters", "Ljava/util/Map;",
					"Ljava/util/Map<Ljava/lang/String;Ljava/lang/Class<*>;>;", null);
			fv.visitEnd();
		}
		//
		// private static Map<String,Class<?>[]> _asInterceptors;
		//
		{
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "_asInterceptors", "Ljava/util/Map;",
					"Ljava/util/Map<Ljava/lang/String;[Ljava/lang/Class<*>;>;", null);
			fv.visitEnd();
		}
		//
		// private static Map<String,Class<?>> _asParameters;
		//
		{
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "_asParameters", "Ljava/util/Map;",
					"Ljava/util/Map<Ljava/lang/String;Ljava/lang/Class<*>;>;", null);
			fv.visitEnd();
		}

		if(!createdStaticMethod) {
			MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			mv = new OControllerStaticMethodVisitor(mv, reflector);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}

		createUrlMethod();

		createContextMethod("_asApplicationContext", Type.getDescriptor(ApplicationContext.class), reflector.getApplicationContext());
		createContextMethod("_asErrorContext", Type.getDescriptor(ErrorContext.class), reflector.getErrorContext());
		createContextMethod("_asCookieContext", Type.getDescriptor(CookieContext.class), reflector.getCookieContext());
		createContextMethod("_asMessageContext", Type.getDescriptor(MessageContext.class), reflector.getMessageContext());
		createContextMethod("_asRequestContext", Type.getDescriptor(RequestContext.class), reflector.getRequestContext());
		createContextMethod("_asSessionContext", Type.getDescriptor(SessionContext.class), reflector.getSessionContext());
		
		createParametersMethod();
		createParametersSetterMethod();
		createParametersGetterMethod();
		createParameterConverterMethod();
		
		createActionMethod();
		createActionInterceptorsMethod();
		createActionIsDefinedMethod();
	}
	
	/**
	 * Generates _asActions() method as following:
	 * 
	 * if(name.equals("upload"))
	 * 	return upload();
	 * else if(name.equals("save"))
	 * 	return save();
	 * else if(name.equals("main"))
	 * 	return main();
	 * else if(name.equals("redir"))
	 * 	return redir();
	 * else
	 * 	throw new RuntimeException("no action named "+name);
	 */
	private void createActionMethod() {
		StringBuilder sb = new StringBuilder();
		sb.append("(").append(Type.getType(String.class)).append(")").append(Type.getType(Result.class));
		String desc = sb.toString();
		
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asAction", desc, null, null);
		
		Label start = new Label();
		Label next = new Label();
		Label variable = new Label();
		boolean first = true;
		
		for(String name: reflector.getActions().keySet()) {
			Label l0 = null;
			Label l1 = new Label();
			
			if(first) {
				l0 = new Label();
				first = false;
			}
			else {
				l0 = next;
				next = new Label();
			}
			
			mv.visitLabel(l0);
			if(!first) {
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			}
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitLdcInsn(name);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
			mv.visitJumpInsn(Opcodes.IFEQ, next);
			
			mv.visitLabel(l1);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, reflector.getClazzInternalName(), name, "()"+Type.getType(Result.class), false);
			mv.visitInsn(Opcodes.ARETURN);
		}

		mv.visitLabel(next);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitTypeInsn(Opcodes.NEW, "java/lang/NoSuchMethodException");
		mv.visitInsn(Opcodes.DUP);
		mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn("no @Action named ");
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/NoSuchMethodException", "<init>", "(Ljava/lang/String;)V", false);
		mv.visitInsn(Opcodes.ATHROW);
		
		mv.visitLabel(variable);
		mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, start, next, 0);
		mv.visitLocalVariable("name", "Ljava/lang/String;", null, start, next, 1);
		
		mv.visitMaxs(5, 2);
		mv.visitEnd();
	}
	
	/**
	 * Generates code:
	 * 
	 * public Class<?>[] _asActionInterceptors(String name) {
	 * 	return _asInterceptors.get(name);
	 * }
	 */
	public void createActionInterceptorsMethod() {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asActionInterceptors", "(Ljava/lang/String;)[Ljava/lang/Class;", null, null);
		
		Label l0 = new Label();
		Label l1 = new Label();
		mv.visitLabel(l0);
		mv.visitFieldInsn(Opcodes.GETSTATIC, reflector.getClazzInternalName(), "_asInterceptors", "Ljava/util/Map;");
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
		mv.visitTypeInsn(Opcodes.CHECKCAST, "[Ljava/lang/Class;");
		mv.visitInsn(Opcodes.ARETURN);
		
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, l0, l1, 0);
		mv.visitLocalVariable("name", "Ljava/lang/String;", null, l0, l1, 1);
		mv.visitMaxs(2, 2);
		
		mv.visitEnd();
	}
	
	/**
	 * Generates this code:
	 * 
	 * public boolean _asActionIsDefined(String name) {
	 * 	if(_asActions.containsKey(name))
	 * 		return true;
	 * 	return false;
	 * }
	 */
	public void createActionIsDefinedMethod() {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asActionIsDefined", "(Ljava/lang/String;)Z", null, null);
		Label l0 = new Label();
		Label l1 = new Label();
		Label l2 = new Label();
		Label l3 = new Label();
		
		mv.visitLabel(l0);
		mv.visitFieldInsn(Opcodes.GETSTATIC, reflector.getClazzInternalName(), "_asActions", "Ljava/util/Map;");
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "containsKey", "(Ljava/lang/Object;)Z", true);
		mv.visitJumpInsn(Opcodes.IFEQ, l1);
		
		mv.visitLabel(l2);
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitInsn(Opcodes.IRETURN);
		
		mv.visitLabel(l1);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitInsn(Opcodes.IRETURN);
		
		mv.visitLabel(l3);
		mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, l0, l3, 0);
		mv.visitLocalVariable("name", "Ljava/lang/String;", null, l0, l3, 1);
		mv.visitMaxs(2, 2);
		
		mv.visitEnd();
	}
	
	/**
	 * Generates context method setter: 
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
	
	/**
	 * Generates the code:
	 * public Map<String, Class<?>> _asParameters() {
	 * 	return _asParameters;
	 * }
	 */
	private void createParametersMethod() {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asParameters", "()Ljava/util/Map;", 
				"()Ljava/util/Map<Ljava/lang/String;Ljava/lang/Class<*>;>;", null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitFieldInsn(Opcodes.GETSTATIC, reflector.getClazzInternalName(), "_asParameters", "Ljava/util/Map;");
		mv.visitInsn(Opcodes.ARETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
	
	/**
	 * Generates the _asParameter() getter as
	 * 
	 * public Object _asParameter(String name) {
	 * 	if(name.equals("name"))
	 * 		return getName();
	 * 	if(name.equals("date"))
	 * 		return getDate();
	 * 	...
	 * 	return null;
	 * }
	 */
	private void createParametersGetterMethod() {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asParameter", "(Ljava/lang/String;)Ljava/lang/Object;", null, null);
		
		Label start = new Label();
		Label next = new Label();
		Label variable = new Label();
		boolean first = true;
		
		for(String name: reflector.getParameters().keySet()) {
			Label l0 = null;
			Label l1 = new Label();
			
			if(first) {
				l0 = new Label();
				first = false;
			}
			else {
				l0 = next;
				next = new Label();
			}
			
			mv.visitLabel(l0);
			if(!first)
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitLdcInsn(name);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
			mv.visitJumpInsn(Opcodes.IFEQ, next);
			
			String methodGetterName = reflector.getGetters().get(name).getName();
			String methodGetterDesc = "()"+Type.getDescriptor(reflector.getParameters().get(name));
			
			mv.visitLabel(l1);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, reflector.getClazzInternalName(), methodGetterName, methodGetterDesc, false);
			mv.visitInsn(Opcodes.ARETURN);
		}
		
		mv.visitLabel(next);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitInsn(Opcodes.ACONST_NULL);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitLabel(variable);
		mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, start, variable, 0);
		mv.visitLocalVariable("name", "Ljava/lang/String;", null, start, variable, 1);
		mv.visitMaxs(2, 2);
		mv.visitEnd();
	}
	
	/**
	 * Creates the code as:
	 * 
	 * public void _asParameter(String name, Object value) {
	 * 	if(name.equals("name") {
	 * 		setName((String)value);
	 * 		return;
	 * 	}
	 *	if(name.equals("date") {
	 *		setDate((Date)value);
	 *		return;
	 *	}
	 *	...
	 * 	return;
	 * } 
	 */
	private void createParametersSetterMethod() {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asParameter", "(Ljava/lang/String;Ljava/lang/Object;)V", null, null);
		
		Label start = new Label();
		Label next = new Label();
		Label variable = new Label();
		boolean first = true;
		
		for(String name: reflector.getParameters().keySet()) {
			Label l0 = null;
			Label l1 = new Label();
			Label l2 = new Label();
			
			if(first) {
				l0 = new Label();
				first = false;
			}
			else {
				l0 = next;
				next = new Label();
			}
			
			mv.visitLabel(l0);
			if(!first)
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitLdcInsn(name);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
			mv.visitJumpInsn(Opcodes.IFEQ, next);
			
			Class<?> parameterType = reflector.getParameters().get(name); 
			
			String setterMethodName = reflector.getSetters().get(name).getName();
			String setterMethodDesc = "("+Type.getDescriptor(parameterType)+")V";
			
			mv.visitLabel(l1);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(parameterType));
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, reflector.getClazzInternalName(), setterMethodName, setterMethodDesc, false);
			
			mv.visitLabel(l2);
			mv.visitInsn(Opcodes.RETURN);
		}
		
		mv.visitLabel(next);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitInsn(Opcodes.RETURN);
		
		mv.visitLabel(variable);
		mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, start, next, 0);
		mv.visitLocalVariable("name", "Ljava/lang/String;", null, start, next, 1);
		mv.visitLocalVariable("value", "Ljava/lang/Object;", null, start, next, 2);
		mv.visitMaxs(2, 3);
		mv.visitEnd();
	}
	
	/**
	 * public Class<?> _asParameterConverter(String name) {
	 * 	return _asConverters.get(name);
	 * }
	 */
	public void createParameterConverterMethod() {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asParameterConverter", "(Ljava/lang/String;)Ljava/lang/Class;", null, null);
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitFieldInsn(Opcodes.GETSTATIC, reflector.getClazzInternalName(), "_asConverters", "Ljava/util/Map;");
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
		mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Class");
		mv.visitInsn(Opcodes.ARETURN);
		
		Label l1 = new Label();
		mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, l0, l1, 0);
		mv.visitLocalVariable("name", "Ljava/lang/String;", null, l0, l1, 1);
		mv.visitMaxs(2, 2);
		mv.visitEnd();
	}
	
	/**
	 * Generates the _asUrl() method with using a constant string
	 * public String _asUrl() { return "/*"; }
	 */
	public void createUrlMethod() {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asUrl", "()Ljava/lang/String;", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLdcInsn(reflector.getUrl());
		mv.visitInsn(Opcodes.ARETURN);
		Label l1 = new Label();
		mv.visitLocalVariable("this", Type.getDescriptor(reflector.getClazz()), null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
}
