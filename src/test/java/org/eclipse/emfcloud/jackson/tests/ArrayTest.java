package org.eclipse.emfcloud.jackson.tests;

import static org.eclipse.emfcloud.jackson.module.EMFModule.Feature.OPTION_SERIALIZE_DEFAULT_VALUE;
import static org.junit.Assert.assertEquals;

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

   @Test
   public void testByteArray() {
      ArrayHost u = ArrayFactory.eINSTANCE.createArrayHost();
      u.setB(new byte[] { 1, 2 });

      ObjectNode expected = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/model#//ArrayHost");
      expected.put("b", "0102");

      assertEquals(expected,
         fixture.mapper(OPTION_SERIALIZE_DEFAULT_VALUE, true)
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
         fixture.mapper(OPTION_SERIALIZE_DEFAULT_VALUE, true)
            .valueToTree(u));
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
         fixture.mapper(OPTION_SERIALIZE_DEFAULT_VALUE, true)
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
         fixture.mapper(OPTION_SERIALIZE_DEFAULT_VALUE, true)
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
         fixture.mapper(OPTION_SERIALIZE_DEFAULT_VALUE, true)
            .valueToTree(u));
   }
}
