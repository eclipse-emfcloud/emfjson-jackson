/*******************************************************************************
 * Copyright (c) 2022 Data In Motion Consulting GmbH and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 *******************************************************************************/
package org.eclipse.emfcloud.jackson.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emfcloud.jackson.junit.array.ArrayFactory;
import org.eclipse.emfcloud.jackson.junit.array.ArrayHost;
import org.eclipse.emfcloud.jackson.support.StandardFixture;
import org.junit.ClassRule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ArrayTest {

   @ClassRule
   public static StandardFixture fixture = new StandardFixture();

   private final ObjectMapper mapper = fixture.mapper();
   private final ResourceSet resourceSet = fixture.getResourceSet();

   @Test
   public void testByteArray() {
      ArrayHost u = ArrayFactory.eINSTANCE.createArrayHost();
      u.setB(new byte[] { 1, 2 });

      ObjectNode expected = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/model#//ArrayHost");
      expected.put("b", "0102");

      assertEquals(expected,
         fixture.mapper()
            .valueToTree(u));
   }

   @Test
   public void test1DArray() {
      ArrayHost u = ArrayFactory.eINSTANCE.createArrayHost();
      u.setD1(new Double[] { 1.1, 1.2 });

      ObjectNode expected = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/model#//ArrayHost");
      ArrayNode a = expected.putArray("d1");
      a.add(1.1).add(1.2);

      assertEquals(expected,
         fixture.mapper().valueToTree(u));
   }

   @Test
   public void test2DArray() {
      ArrayHost u = ArrayFactory.eINSTANCE.createArrayHost();
      u.setD2(new Double[][] { { 1.1, 1.2 }, { 2.1, 2.2 } });

      ObjectNode expected = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/model#//ArrayHost");
      ArrayNode a = expected.putArray("d2");
      a.addArray().add(1.1).add(1.2);
      a.addArray().add(2.1).add(2.2);

      assertEquals(expected,
         fixture.mapper()
            .valueToTree(u));
   }

   @Test
   public void test3DArray() {
      ArrayHost u = ArrayFactory.eINSTANCE.createArrayHost();
      u.setD3(new Double[][][] { { { 1.11, 1.12 }, { 1.21, 1.22 } }, { { 2.11, 2.12 }, { 2.21, 2.22 } } });

      ObjectNode expected = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/model#//ArrayHost");
      ArrayNode a = expected.putArray("d3");
      ArrayNode a1 = a.addArray();
      a1.addArray().add(1.11).add(1.12);
      a1.addArray().add(1.21).add(1.22);
      ArrayNode a2 = a.addArray();
      a2.addArray().add(2.11).add(2.12);
      a2.addArray().add(2.21).add(2.22);

      assertEquals(expected,
         fixture.mapper()
            .valueToTree(u));
   }

   @Test
   public void test2DString() {
      ArrayHost u = ArrayFactory.eINSTANCE.createArrayHost();
      u.setS2(new String[][] { { "1.1", "1.2" }, { "2.1", "2.2" } });

      ObjectNode expected = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/model#//ArrayHost");
      ArrayNode a = expected.putArray("s2");
      a.addArray().add("1.1").add("1.2");
      a.addArray().add("2.1").add("2.2");

      assertEquals(expected,
         fixture.mapper()
            .valueToTree(u));
   }

   @Test
   public void testLoad2DDoubleArrayValues() throws IOException {
      String data = "{\n" +
         "  \"eClass\": \"http://www.emfjson.org/jackson/model#//ArrayHost\",\n" +
         "  \"d2\": [ \n" +
         "    [1.1, 1.2], \n" +
         "    [2.1, 2.2] ]\n" +
         "}";

      Resource resource = resourceSet.createResource(URI.createURI("tests/test.json"));
      resource.load(new ByteArrayInputStream(data.getBytes()), null);

      ArrayHost host = (ArrayHost) resource.getContents().get(0);
      Double[][] d2 = host.getD2();
      assertThat(d2).hasSize(2);
      assertThat(d2[0]).containsExactly(1.1, 1.2);
      assertThat(d2[1]).containsExactly(2.1, 2.2);
   }

   @Test
   public void testLoad2DStringArrayValues() throws IOException {
      String data = "{\n" +
         "  \"eClass\": \"http://www.emfjson.org/jackson/model#//ArrayHost\",\n" +
         "  \"s2\": [ \n" +
         "    [\"1.1\", \"1.2\"], \n" +
         "    [\"2.1\", \"2.2\"] ]\n" +
         "}";

      Resource resource = resourceSet.createResource(URI.createURI("tests/test.json"));
      resource.load(new ByteArrayInputStream(data.getBytes()), null);

      ArrayHost host = (ArrayHost) resource.getContents().get(0);
      String[][] s2 = host.getS2();
      assertThat(s2).hasSize(2);
      assertThat(s2[0]).containsExactly("1.1", "1.2");
      assertThat(s2[1]).containsExactly("2.1", "2.2");
   }

}
