/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueIdSpecification;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.UniqueIdTestDescriptorResolver;
import org.junit.gen5.engine.junit5.stubs.TestEngineStub;

/**
 * Unit tests for {@link MethodTestDescriptor}.
 *
 * @author Sam Brannen
 * @author Stefan Bechtold
 * @since 5.0
 */
public class MethodTestDescriptorTests {

	private static final String TEST_METHOD_ID = MethodTestDescriptorTests.class.getName() + "#test()";
	private static final String TEST_METHOD_UID = TestEngineStub.TEST_ENGINE_DUMMY_ID + ":" + TEST_METHOD_ID;
	private static final String TEST_METHOD_STRING_BIGDECIMAL_ID = MethodTestDescriptorTests.class.getName()
			+ "#test(java.lang.String, java.math.BigDecimal)";
	private static final String TEST_METHOD_STRING_BIGDECIMAL_UID = TestEngineStub.TEST_ENGINE_DUMMY_ID + ":"
			+ TEST_METHOD_STRING_BIGDECIMAL_ID;
	private static final EngineDescriptor ENGINE_DESCRIPTOR = new EngineDescriptor(new TestEngineStub());

	@org.junit.Test
	public void constructFromMethod() throws Exception {
		Class<?> testClass = getClass();
		Method testMethod = testClass.getDeclaredMethod("test");
		ClassTestDescriptor parent = new ClassTestDescriptor(testClass, ENGINE_DESCRIPTOR);
		MethodTestDescriptor descriptor = new MethodTestDescriptor(testMethod, parent);

		System.out.println("DEBUG - " + descriptor);
		assertEquals(TEST_METHOD_UID, descriptor.getUniqueId());
		assertEquals(testClass, descriptor.getParent().getTestClass());
		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test", descriptor.getDisplayName(), "display name:");
	}

	@org.junit.Test
	public void constructFromMethodWithCustomDisplayName() throws Exception {
		Class<?> testClass = getClass();
		Method testMethod = testClass.getDeclaredMethod("foo");
		ClassTestDescriptor parent = new ClassTestDescriptor(testClass, ENGINE_DESCRIPTOR);
		MethodTestDescriptor descriptor = new MethodTestDescriptor(testMethod, parent);

		System.out.println("DEBUG - " + descriptor);
		assertEquals(testClass, descriptor.getParent().getTestClass());
		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("custom test name", descriptor.getDisplayName(), "display name:");
	}

	@org.junit.Test
	public void constructFromMethodWithParameters() throws Exception {
		Class<?> testClass = getClass();
		Method testMethod = testClass.getDeclaredMethod("test", String.class, BigDecimal.class);
		ClassTestDescriptor parent = new ClassTestDescriptor(testClass, ENGINE_DESCRIPTOR);
		MethodTestDescriptor descriptor = new MethodTestDescriptor(testMethod, parent);

		System.out.println("DEBUG - " + descriptor);
		assertEquals(TEST_METHOD_STRING_BIGDECIMAL_UID, descriptor.getUniqueId());
		assertEquals(testClass, descriptor.getParent().getTestClass());
		assertEquals(testMethod, descriptor.getTestMethod());
		assertEquals("test", descriptor.getDisplayName(), "display name:");
	}

	@org.junit.Test
	public void fromTestDescriptorIdForMethod() throws Exception {
		UniqueIdTestDescriptorResolver resolver = new UniqueIdTestDescriptorResolver();
		UniqueIdSpecification specification = new UniqueIdSpecification(TEST_METHOD_UID);
		TestDescriptor descriptor = resolver.resolve(ENGINE_DESCRIPTOR, specification);
		List<TestDescriptor> descriptors = resolver.resolveChildren(descriptor, specification);
		descriptor = descriptors.get(0);

		assertNotNull(descriptor, "descriptor:");
		assertEquals("test", descriptor.getDisplayName(), "display name:");
		assertEquals(MethodTestDescriptorTests.class, ((ClassTestDescriptor) descriptor.getParent()).getTestClass());
		assertEquals(MethodTestDescriptorTests.class.getDeclaredMethod("test"),
			((MethodTestDescriptor) descriptor).getTestMethod());
	}

	@org.junit.Test
	public void fromTestDescriptorIdForMethodWithParameters() throws Exception {
		UniqueIdTestDescriptorResolver resolver = new UniqueIdTestDescriptorResolver();
		UniqueIdSpecification specification = new UniqueIdSpecification(TEST_METHOD_STRING_BIGDECIMAL_UID);
		TestDescriptor descriptor = resolver.resolve(ENGINE_DESCRIPTOR, specification);
		List<TestDescriptor> descriptors = resolver.resolveChildren(descriptor, specification);
		descriptor = descriptors.get(0);

		assertNotNull(descriptor, "descriptor:");
		assertEquals("test", descriptor.getDisplayName(), "display name:");
		assertEquals(getClass(), ((ClassTestDescriptor) descriptor.getParent()).getTestClass());
		assertEquals(getClass().getDeclaredMethod("test", String.class, BigDecimal.class),
			((MethodTestDescriptor) descriptor).getTestMethod());
	}

	void test() {
	}

	void test(String txt, BigDecimal sum) {
	}

	@Test(name = "custom test name")
	void foo() {
	}
}