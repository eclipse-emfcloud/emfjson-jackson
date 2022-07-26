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
package org.eclipse.emfcloud.jackson.tests.annotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emfcloud.jackson.junit.annotations.AnnotationsPackage;
import org.eclipse.emfcloud.jackson.junit.annotations.RawJson;
import org.eclipse.emfcloud.jackson.module.EMFModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonRawValueTest {

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
   public void test_deserializeRaw() throws IOException {
      final JsonNode input = mapper.createObjectNode()
            .put("eClass", "http://www.emfjson.org/jackson/annotations#//RawJson")
            .set("raw", mapper.createObjectNode()
                  .put("foo", "bar"));

      final RawJson raw = mapper.readValue(input.toString(), RawJson.class);
      assertThat(raw.getRaw()).containsSubsequence("\"foo\":\"bar\"");
   }

   @Test
   public void test_deserializeRawNull() throws IOException {
      final JsonNode input = mapper.createObjectNode()
            .put("eClass", "http://www.emfjson.org/jackson/annotations#//RawJson")
            .putNull("raw");

      final RawJson raw = mapper.readValue(input.toString(), RawJson.class);
      assertThat(raw.getRaw()).isNull();
   }
}
