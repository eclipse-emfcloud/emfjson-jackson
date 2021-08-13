/*
 * Copyright (c) 2019 Guillaume Hillairet and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 *
 */
package org.eclipse.emfcloud.jackson;

import org.eclipse.emfcloud.jackson.databind.type.EcoreTypeFactoryTest;
import org.eclipse.emfcloud.jackson.tests.*;
import org.eclipse.emfcloud.jackson.tests.annotations.JsonPropertyTest;
import org.eclipse.emfcloud.jackson.tests.annotations.JsonTypeInfoTest;
import org.eclipse.emfcloud.jackson.tests.custom.CustomDeserializersTest;
import org.eclipse.emfcloud.jackson.tests.custom.CustomSerializersTest;
import org.eclipse.emfcloud.jackson.tests.dynamic.*;
import org.eclipse.emfcloud.jackson.tests.generics.GenericTest;
import org.eclipse.emfcloud.jackson.tests.uuids.UuidLoadTest;
import org.eclipse.emfcloud.jackson.tests.uuids.UuidSaveTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		// standard
		AnnotationTest.class,
		ContainmentTest.class,
		EnumTest.class,
		ExternalReferencesTest.class,
		FeatureMapTest.class,
		GenericTest.class,
		IdTest.class,
		MapTest.class,
		ModuleTest.class,
		NoTypeTest.class,
		PolymorphicTest.class,
		ReaderTest.class,
		ReferenceTest.class,
		ValueTest.class,

		// type factory
		EcoreTypeFactoryTest.class,

		// meta
		ModelTest.class,

		// annotations
		JsonPropertyTest.class,
		JsonTypeInfoTest.class,

		// uuid
		UuidLoadTest.class,
		UuidSaveTest.class,

		// dynamic tests
		DynamicContainmentTest.class,
		DynamicEnumTest.class,
		DynamicInstanceTest.class,
		DynamicMapTest.class,
		DynamicPackageTest.class,
		DynamicPolymorphicTest.class,
		DynamicValueTest.class,

		// custom
		CustomDeserializersTest.class,
		CustomSerializersTest.class
})
public class TestSuite {
}
