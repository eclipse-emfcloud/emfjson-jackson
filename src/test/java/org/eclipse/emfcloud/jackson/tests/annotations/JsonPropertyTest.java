/*
 * Copyright (c) 2019 Guillaume Hillairet and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 */

package org.eclipse.emfcloud.jackson.tests.annotations;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emfcloud.jackson.junit.annotations.AnnotationsFactory;
import org.eclipse.emfcloud.jackson.junit.annotations.AnnotationsPackage;
import org.eclipse.emfcloud.jackson.junit.annotations.TestB;
import org.eclipse.emfcloud.jackson.module.EMFModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonPropertyTest {

   private ObjectMapper mapper;

   @Before
   public void setUp() {
      EPackage.Registry.INSTANCE.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
      EPackage.Registry.INSTANCE.put(AnnotationsPackage.eNS_URI, AnnotationsPackage.eINSTANCE);

      mapper = new ObjectMapper();
      mapper.registerModule(new EMFModule());
   }

   @After
   public void tearDown() {
      EPackage.Registry.INSTANCE.clear();
      Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().clear();
   }

   @Test
   public void testSave_AttributeAnnotation_WithValue() {
      JsonNode expected = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/annotations#//TestB")
         .put("my_value", "Hello")
         .put("hello", "Hello");

      TestB b1 = AnnotationsFactory.eINSTANCE.createTestB();
      b1.setValue("Hello");

      JsonNode actual = mapper.valueToTree(b1);
      assertThat(actual)
         .isEqualTo(expected);
   }

   @Test
   public void testLoad_AttributeAnnotation_WithValue() throws IOException {
      JsonNode data = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/annotations#//TestB")
         .put("my_value", "Hello")
         .put("hello", "Hello");

      TestB b = mapper.readValue(data.toString(), TestB.class);

      assertThat(b).isNotNull();
      assertThat(b.eResource()).isNull();
      assertThat(b.getValue()).isEqualTo("Hello");
   }

   @Test
   public void testMethodAnnotation() {
      TestB b1 = AnnotationsFactory.eINSTANCE.createTestB();

      JsonNode expected = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/annotations#//TestB")
         .put("hello", "Hello");

      JsonNode actual = mapper.valueToTree(b1);
      assertThat(actual)
         .isEqualTo(expected);
   }

}
