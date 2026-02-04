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

package org.eclipse.emfcloud.jackson.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.eclipse.emfcloud.jackson.databind.EMFContext.Attributes.RESOURCE_SET;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emfcloud.jackson.junit.model.ETypes;
import org.eclipse.emfcloud.jackson.junit.model.ModelFactory;
import org.eclipse.emfcloud.jackson.junit.model.PrimaryObject;
import org.eclipse.emfcloud.jackson.junit.model.TargetObject;
import org.eclipse.emfcloud.jackson.junit.model.Type;
import org.eclipse.emfcloud.jackson.junit.model.Value;
import org.eclipse.emfcloud.jackson.support.StandardFixture;
import org.junit.ClassRule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;

public class MapTest {

   @ClassRule
   public static StandardFixture fixture = new StandardFixture();

   private final ObjectMapper mapper = fixture.mapper();
   private final ResourceSet resourceSet = fixture.getResourceSet();

   @Test
   public void testSaveMap() throws IOException {
      JsonNode expected = mapper.readTree(
         Paths.get("src/test/resources/tests/test-map-1.json").toFile());

      Resource resource = resourceSet.createResource(URI.createURI("test"));

      ETypes types = ModelFactory.eINSTANCE.createETypes();
      {
         Type t = ModelFactory.eINSTANCE.createType();
         t.setName("t1");
         Value v = ModelFactory.eINSTANCE.createValue();
         v.setValue(1);
         types.getValues().put(t, v);
      }
      {
         Type t = ModelFactory.eINSTANCE.createType();
         t.setName("t2");
         Value v = ModelFactory.eINSTANCE.createValue();
         v.setValue(2);
         types.getValues().put(t, v);
      }
      resource.getContents().add(types);

      JsonNode actual = mapper.valueToTree(resource);
      assertThat(actual).isEqualTo(expected);
   }

   @Test
   public void testLoadMap() {
      Resource resource = resourceSet.getResource(
         URI.createURI("src/test/resources/tests/test-map-1.json"), true);

      assertThat(resource.getContents()).hasSize(1);
      assertThat(resource.getContents().get(0)).isInstanceOf(ETypes.class);

      ETypes types = (ETypes) resource.getContents().get(0);

      EMap<Type, Value> values = types.getValues();

      assertThat(values).hasSize(2);

      Map.Entry<Type, Value> e = values.get(0);

      assertThat(e.getKey().getName())
         .isEqualTo("t1");
      assertThat(e.getValue().getValue())
         .isEqualTo(1);

      e = values.get(1);

      assertThat(e.getKey().getName())
         .isEqualTo("t2");
      assertThat(e.getValue().getValue())
         .isEqualTo(2);
   }

   @Test
   public void testSaveMapWithStringKey() throws IOException {
      JsonNode expected = mapper.readTree(
         Paths.get("src/test/resources/tests/test-map-2.json").toFile());

      Resource resource = resourceSet.createResource(URI.createURI("test"));

      ETypes types = ModelFactory.eINSTANCE.createETypes();

      Value v1 = ModelFactory.eINSTANCE.createValue();
      v1.setValue(1);

      Value v2 = ModelFactory.eINSTANCE.createValue();
      v2.setValue(2);

      types.getStringMapValues()
         .put("Hello", v1);

      types.getStringMapValues()
         .put("World", v2);

      resource.getContents().add(types);

      JsonNode actual = mapper.valueToTree(resource);
      assertThat(actual).isEqualTo(expected);
   }

   @Test
   public void testLoadMapWithStringKey() {
      Resource resource = resourceSet.getResource(URI.createURI("src/test/resources/tests/test-map-2.json"), true);

      assertThat(resource.getContents()).hasSize(1);
      assertThat(resource.getContents().get(0)).isInstanceOf(ETypes.class);

      ETypes types = (ETypes) resource.getContents().get(0);

      assertThat(types.getStringMapValues()).hasSize(2);

      EMap<String, Value> mapValues = types.getStringMapValues();

      assertThat(mapValues.get("Hello").getValue()).isEqualTo(1);
      assertThat(mapValues.get("World").getValue()).isEqualTo(2);
   }

   @Test
   public void testSaveMapWithRefs() throws IOException {
      JsonNode expected = mapper.readTree(
         Paths.get("src/test/resources/tests/test-map-with-refs.json").toFile());

      Resource resource = resourceSet.createResource(URI.createURI("test"));

      ETypes types = ModelFactory.eINSTANCE.createETypes();

      PrimaryObject p1 = ModelFactory.eINSTANCE.createPrimaryObject();
      p1.setName("p1");

      TargetObject t1 = ModelFactory.eINSTANCE.createTargetObject();
      t1.setSingleAttribute("t1");

      types.getValuesWithRef().put(p1, t1);

      resource.getContents().add(types);
      resource.getContents().add(p1);
      resource.getContents().add(t1);

      JsonNode actual = mapper.valueToTree(resource);
      assertThat(actual).isEqualTo(expected);
   }

   @Test
   public void testLoadMapWithRefs() {
      Resource resource = resourceSet.getResource(URI.createURI("src/test/resources/tests/test-map-with-refs.json"),
         true);

      assertThat(resource.getContents()).hasSize(3);
      assertThat(resource.getContents().get(0)).isInstanceOf(ETypes.class);
      assertThat(resource.getContents().get(1)).isInstanceOf(PrimaryObject.class);
      assertThat(resource.getContents().get(2)).isInstanceOf(TargetObject.class);

      ETypes types = (ETypes) resource.getContents().get(0);
      PrimaryObject p1 = (PrimaryObject) resource.getContents().get(1);
      TargetObject t1 = (TargetObject) resource.getContents().get(2);

      assertThat(types.getValuesWithRef())
         .hasSize(1);

      assertThat(types.getValuesWithRef().map())
         .containsExactly(entry(p1, t1));
   }

   @Test
   public void testSaveMapWithContainmentInValue() throws IOException {
      JsonNode expected = mapper.readTree(
         Paths.get("src/test/resources/tests/test-map-with-containment-in-value.json").toFile());

      Resource resource = resourceSet.createResource(URI.createURI("test"));

      ETypes types = ModelFactory.eINSTANCE.createETypes();

      PrimaryObject p1 = ModelFactory.eINSTANCE.createPrimaryObject();
      p1.setName("p1");
      TargetObject c1 = ModelFactory.eINSTANCE.createTargetObject();
      c1.setSingleAttribute("c1");
      p1.getMultipleContainmentReferenceNoProxies().add(c1);

      PrimaryObject p2 = ModelFactory.eINSTANCE.createPrimaryObject();
      p2.setName("p2");
      TargetObject c2 = ModelFactory.eINSTANCE.createTargetObject();
      c2.setSingleAttribute("c2");
      p2.getMultipleContainmentReferenceNoProxies().add(c2);

      types.getStringMapValuesWithContainmentInValue().put("Hello", p1);
      types.getStringMapValuesWithContainmentInValue().put("World", p2);

      resource.getContents().add(types);

      JsonNode actual = mapper.valueToTree(resource);
      assertThat(actual).isEqualTo(expected);
   }

   @Test
   public void testLoadMapWithContainmentInValue() {
      Resource resource = resourceSet.getResource(
         URI.createURI("src/test/resources/tests/test-map-with-containment-in-value.json"),
         true);

      assertThat(resource.getContents()).hasSize(1);
      assertThat(resource.getContents().get(0)).isInstanceOf(ETypes.class);

      ETypes types = (ETypes) resource.getContents().get(0);

      EMap<String, PrimaryObject> mapValues = types.getStringMapValuesWithContainmentInValue();
      assertThat(mapValues).hasSize(2);

      PrimaryObject p1 = mapValues.get("Hello");
      PrimaryObject p2 = mapValues.get("World");

      assertThat(p1.getName()).isEqualTo("p1");
      assertThat(p1.getMultipleContainmentReferenceNoProxies().size()).isEqualTo(1);
      assertThat(p1.getMultipleContainmentReferenceNoProxies().get(0).getSingleAttribute()).isEqualTo("c1");
      assertThat(p2.getName()).isEqualTo("p2");
      assertThat(p2.getMultipleContainmentReferenceNoProxies().size()).isEqualTo(1);
      assertThat(p2.getMultipleContainmentReferenceNoProxies().get(0).getSingleAttribute()).isEqualTo("c2");
   }

   @Test
   public void testSaveMapWithDataTypeKey() {
      JsonNode expected = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/model#//ETypes")
         .set("dataTypeMapValues", mapper.createObjectNode()
            .put("test.json", "hello"));

      ETypes types = ModelFactory.eINSTANCE.createETypes();
      types.getDataTypeMapValues().put("test.json", "hello");

      JsonNode actual = mapper.valueToTree(types);
      assertThat(actual)
         .isEqualTo(expected);
   }

   @Test
   public void testSaveLoadWithDataTypeKey() throws IOException {
      JsonNode data = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/model#//ETypes")
         .set("dataTypeMapValues", mapper.createObjectNode()
            .put("test.json", "hello"));

      ETypes types = mapper.reader()
         .with(ContextAttributes
            .getEmpty()
            .withSharedAttribute(RESOURCE_SET, resourceSet))
         .forType(ETypes.class)
         .readValue(data);

      assertThat(types)
         .isNotNull();
      assertThat(types.getDataTypeMapValues().map())
         .contains(entry("test.json", "hello"));
   }

   /**
    * Test the edge case when an EMap has an entry with null key
    * (not compatible with json)
    */
   @Test
   public void testSaveMapWithNullKey() {
      JsonNode expected = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/model#//ETypes")
         .set("dataTypeMapValues", mapper.createObjectNode()
            .put("", "hello"));

      ETypes types = ModelFactory.eINSTANCE.createETypes();
      types.getDataTypeMapValues().put(null, "hello");

      JsonNode actual = mapper.valueToTree(types);
      assertThat(actual)
         .isEqualTo(expected);
   }
}
