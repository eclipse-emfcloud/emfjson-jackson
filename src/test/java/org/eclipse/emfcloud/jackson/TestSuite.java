/*******************************************************************************
 * Copyright (c) 2019-2022 Guillaume Hillairet and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 *******************************************************************************/
package org.eclipse.emfcloud.jackson;

import org.eclipse.emfcloud.jackson.databind.type.EcoreTypeFactoryTest;
import org.eclipse.emfcloud.jackson.tests.AnnotationTest;
import org.eclipse.emfcloud.jackson.tests.ArrayTest;
import org.eclipse.emfcloud.jackson.tests.ContainmentTest;
import org.eclipse.emfcloud.jackson.tests.EnumTest;
import org.eclipse.emfcloud.jackson.tests.ExternalReferencesTest;
import org.eclipse.emfcloud.jackson.tests.FeatureMapTest;
import org.eclipse.emfcloud.jackson.tests.IdTest;
import org.eclipse.emfcloud.jackson.tests.MapTest;
import org.eclipse.emfcloud.jackson.tests.ModelTest;
import org.eclipse.emfcloud.jackson.tests.ModuleTest;
import org.eclipse.emfcloud.jackson.tests.NoTypeTest;
import org.eclipse.emfcloud.jackson.tests.PolymorphicTest;
import org.eclipse.emfcloud.jackson.tests.ReaderTest;
import org.eclipse.emfcloud.jackson.tests.ReferenceTest;
import org.eclipse.emfcloud.jackson.tests.ValueTest;
import org.eclipse.emfcloud.jackson.tests.annotations.JsonPropertyTest;
import org.eclipse.emfcloud.jackson.tests.annotations.JsonTypeInfoTest;
import org.eclipse.emfcloud.jackson.tests.custom.CustomDeserializersTest;
import org.eclipse.emfcloud.jackson.tests.custom.CustomSerializersTest;
import org.eclipse.emfcloud.jackson.tests.dynamic.DynamicContainmentTest;
import org.eclipse.emfcloud.jackson.tests.dynamic.DynamicEnumTest;
import org.eclipse.emfcloud.jackson.tests.dynamic.DynamicInstanceTest;
import org.eclipse.emfcloud.jackson.tests.dynamic.DynamicMapTest;
import org.eclipse.emfcloud.jackson.tests.dynamic.DynamicPackageTest;
import org.eclipse.emfcloud.jackson.tests.dynamic.DynamicPolymorphicTest;
import org.eclipse.emfcloud.jackson.tests.dynamic.DynamicValueTest;
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
   ArrayTest.class,

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

public class TestSuite {}
