/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution.injection;

import static java.util.stream.Collectors.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

import org.junit.gen5.api.extension.ArgumentResolutionException;
import org.junit.gen5.api.extension.MethodArgumentResolver;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * {@code MethodInvoker} encapsulates the invocation of a method, including
 * support for dynamic resolution of method arguments via {@link MethodArgumentResolver
 * MethodArgumentResolvers} registered in the supplied {@link MethodArgumentResolverRegistry}.
 *
 * @author Sam Brannen
 * @author Matthias Merdes
 * @since 5.0
 */
public class MethodInvoker {

	private final Method method;

	private final Object target;

	private final MethodArgumentResolverRegistry resolverRegistry;

	public MethodInvoker(Method method, Object target, MethodArgumentResolverRegistry resolverRegistry) {
		Preconditions.notNull(method, "method must not be null");
		Preconditions.notNull(target, "target object must not be null");
		Preconditions.notNull(resolverRegistry, "MethodArgumentResolverRegistry must not be null");

		this.method = method;
		this.target = target;
		this.resolverRegistry = resolverRegistry;
	}

	public Object invoke(TestExecutionContext testExecutionContext) {
		return ReflectionUtils.invokeMethod(this.method, this.target, resolveArguments(testExecutionContext));
	}

	/**
	 * Resolve the array of arguments for the configured method.
	 *
	 * @param testExecutionContext the current test execution context
	 * @return the array of Objects to be used as arguments in the method
	 * invocation; never {@code null} though potentially empty
	 * @throws ArgumentResolutionException
	 */
	private Object[] resolveArguments(TestExecutionContext testExecutionContext) throws ArgumentResolutionException {
		// @formatter:off
		return Arrays.stream(this.method.getParameters())
				.map(param -> resolveArgument(param, testExecutionContext))
				.toArray(Object[]::new);
		// @formatter:on
	}

	private Object resolveArgument(Parameter parameter, TestExecutionContext testExecutionContext) {
		try {
			// @formatter:off
			List<MethodArgumentResolver> matchingResolvers = this.resolverRegistry.getResolvers().stream()
					.filter(resolver -> resolver.supports(parameter))
					.collect(toList());
			// @formatter:on

			if (matchingResolvers.size() == 0) {
				throw new ArgumentResolutionException(
					String.format("No MethodArgumentResolver registered for parameter [%s] in method [%s].", parameter,
						this.method.toGenericString()));
			}
			if (matchingResolvers.size() > 1) {
				// @formatter:off
				String resolverNames = matchingResolvers.stream()
						.map(resolver -> resolver.getClass().getName())
						.collect(joining(", "));
				// @formatter:on
				throw new ArgumentResolutionException(String.format(
					"Discovered multiple competing MethodArgumentResolvers for parameter [%s] in method [%s]: %s",
					parameter, this.method.toGenericString(), resolverNames));
			}
			return matchingResolvers.get(0).resolveArgument(parameter, testExecutionContext);
		}
		catch (Exception ex) {
			if (ex instanceof ArgumentResolutionException) {
				throw (ArgumentResolutionException) ex;
			}
			throw new ArgumentResolutionException(String.format("Failed to resolve parameter [%s] in method [%s]",
				parameter, this.method.toGenericString()), ex);
		}
	}

}