/*******************************************************************************
 * Copyright (c) 2019-2021 Guillaume Hillairet and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 *******************************************************************************/
package org.eclipse.emfcloud.jackson.tests.uuids;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.jackson.junit.model.ConcreteTypeOne;
import org.eclipse.emfcloud.jackson.junit.model.Container;
import org.eclipse.emfcloud.jackson.junit.model.ModelFactory;
import org.eclipse.emfcloud.jackson.junit.model.ModelPackage;
import org.eclipse.emfcloud.jackson.module.EMFModule;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class UuidSaveTest extends UuidSupport {

   private final ObjectMapper mapper = new ObjectMapper();

   @Before
   public void setUp() {
      EPackage.Registry.INSTANCE.put(ModelPackage.eNS_URI, ModelPackage.eINSTANCE);

      EMFModule module = new EMFModule();
      module.configure(EMFModule.Feature.OPTION_USE_ID, true);
      mapper.registerModule(module);
   }

   @Test
   public void testSerializeOneObjectWithUuid() {
      Resource resource = createUuidResource("test.xmi", mapper);
      Container root = ModelFactory.eINSTANCE.createContainer();
      resource.getContents().add(root);

      // Make sure the fragment identifier is a UUID
      assertTrue(EcoreUtil.getURI(root).fragment().startsWith("_"));

      JsonNode node = mapper.valueToTree(root);

      assertNotNull(node);
      assertNotNull(node.get("@id"));
      assertEquals(uuid(root), uuid(node));
   }

   @Test
   public void testSerializeOneRootWithTwoChildHavingOneReference() throws IOException {
      Resource resource = createUuidResource("test.xmi", mapper);

      Container root = ModelFactory.eINSTANCE.createContainer();
      ConcreteTypeOne one = ModelFactory.eINSTANCE.createConcreteTypeOne();
      ConcreteTypeOne two = ModelFactory.eINSTANCE.createConcreteTypeOne();

      one.setName("one");
      two.setName("two");

      one.getRefProperty().add(two);

      root.getElements().add(one);
      root.getElements().add(two);

      resource.getContents().add(root);

      JsonNode node = mapper.valueToTree(root);

      assertNotNull(node);
      assertNotNull(node.get("@id"));
      assertEquals(uuid(root), uuid(node));

      assertTrue(node.get("elements").isArray());

      ArrayNode elements = (ArrayNode) node.get("elements");
      assertEquals(2, elements.size());

      JsonNode node1 = elements.get(0);
      JsonNode node2 = elements.get(1);

      assertNotNull(node1.get("@id"));
      assertEquals(uuid(one), uuid(node1));

      assertNotNull(node2.get("@id"));
      assertEquals(uuid(two), uuid(node2));

      assertNotNull(node1.get("refProperty"));
      assertNull(node2.get("refProperty"));
      assertTrue(node1.get("refProperty").isArray());

      ArrayNode refProperty = (ArrayNode) node1.get("refProperty");
      assertEquals(1, refProperty.size());

      JsonNode ref = refProperty.get(0);
      assertNotNull(ref.get("$ref"));

      assertEquals(uuid(two), ref.get("$ref").asText());
   }

}
