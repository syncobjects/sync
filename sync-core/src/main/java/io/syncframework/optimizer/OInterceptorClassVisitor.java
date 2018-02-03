/*
 * Copyright 2012-2017 SyncObjects Ltda.
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
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import io.syncframework.api.ApplicationContext;
import io.syncframework.api.CookieContext;
import io.syncframework.api.ErrorContext;
import io.syncframework.api.MessageContext;
import io.syncframework.api.RequestContext;
import io.syncframework.api.Result;
import io.syncframework.api.SessionContext;

/**
 * @author dfroz
 */
public class OInterceptorClassVisitor extends ClassVisitor {
	private OInterceptorReflector reflector;
	private boolean createdStaticMethod = false;

	public OInterceptorClassVisitor(ClassVisitor cv, OInterceptorReflector reflector) {
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
		ninterfaces[ninterfaces.length - 1] = Type.getInternalName(OInterceptor.class);
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
			mv = new OInterceptorStaticMethodVisitor(mv, reflector);
		}
		return mv;
	}
	
	/**
	 * Add code to the end of the class. We are adding the IController methods
	 * @see org.objectweb.asm.ClassVisitor#visitEnd()
	 */
	@Override
	public void visitEnd() {
		// private static String _asAfterType;
		{
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "_asAfterType", "Ljava/lang/String;", 
					null, null);
			fv.visitEnd();
		}
		// private static String _asBeforeType;
		{
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "_asBeforeType", "Ljava/lang/String;", 
					null,	null);
			fv.visitEnd();
		}
		// private static Map<String,Class<?>> _asConverters;
		{
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "_asConverters", "Ljava/util/Map;",
					"Ljava/util/Map<Ljava/lang/String;Ljava/lang/Class<*>;>;", null);
			fv.visitEnd();
		}
		// private static Map<String,Class<?>> _asParameters;
		{
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "_asParameters", "Ljava/util/Map;",
					"Ljava/util/Map<Ljava/lang/String;Ljava/lang/Class<*>;>;", null);
			fv.visitEnd();
		}
		
		createStaticMethod();
		
		if(!createdStaticMethod) {
			MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			mv = new OInterceptorStaticMethodVisitor(mv, reflector);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		
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
		
		createAfterMethod();
		createAfterTypeMethod();
		createBeforeMethod();
		createBeforeTypeMethod();
	}
	
	/**
	 * Generates the code:
	 * 
	 * public Result _asAfter() {
	 * 	return after();
	 * }
	 */
	public void createAfterMethod() {
		String description = "()"+Type.getDescriptor(Result.class);
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asAfter", description, null, null);
		Label l0 = new Label();
		
		if(reflector.getAfter() != null) {
			mv.visitLabel(l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, reflector.getClazzInternalName(), "after", description, false);
			mv.visitInsn(Opcodes.ARETURN);
		}
		else {
			mv.visitLabel(l0);
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitInsn(Opcodes.ARETURN);
		}
		
		Label l1 = new Label();
		mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
	
	/**
	 * Generates code:
	 * public Result _asAfterType() {
	 * 	return _asAfterType;
	 * }
	 */
	public void createAfterTypeMethod() {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asAfterType", "()Ljava/lang/String;", null, null);
		
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitFieldInsn(Opcodes.GETSTATIC, reflector.getClazzInternalName(), "_asAfterType", "Ljava/lang/String;");
		mv.visitInsn(Opcodes.ARETURN);
		
		Label l1 = new Label();
		mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
	
	/**
	 * Generates the code:
	 * 
	 * public Result _asBefore() {
	 * 	return before();
	 * }
	 */
	public void createBeforeMethod() {
		String description = "()"+Type.getDescriptor(Result.class);
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asBefore", description, null, null);
		Label l0 = new Label();
		
		if(reflector.getAfter() != null) {
			mv.visitLabel(l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, reflector.getClazzInternalName(), "before", description, false);
			mv.visitInsn(Opcodes.ARETURN);
		}
		else {
			mv.visitLabel(l0);
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitInsn(Opcodes.ARETURN);
		}
		
		Label l1 = new Label();
		mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
	
	/**
	 * Generates code:
	 * public Result _asBeforeType() {
	 * 	return _asBeforeType;
	 * }
	 */
	public void createBeforeTypeMethod() {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "_asBeforeType", "()Ljava/lang/String;", null, null);
		
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitFieldInsn(Opcodes.GETSTATIC, reflector.getClazzInternalName(), "_asBeforeType", "Ljava/lang/String;");
		mv.visitInsn(Opcodes.ARETURN);
		
		Label l1 = new Label();
		mv.visitLocalVariable("this", reflector.getClazzDescriptor(), null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
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
	
	/**
	 * Generates static {} code:
	 * 
	 * static {
	 * 	try {
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
	 */
	private void createStaticMethod() {
		
	}
}