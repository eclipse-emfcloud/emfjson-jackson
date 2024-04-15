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
package org.eclipse.emfcloud.jackson.tests;

import static org.eclipse.emfcloud.jackson.module.EMFModule.Feature.OPTION_MINIMIZE_TYPE_INFO;
import static org.junit.Assert.assertEquals;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emfcloud.jackson.junit.model.Address;
import org.eclipse.emfcloud.jackson.junit.model.ModelFactory;
import org.eclipse.emfcloud.jackson.junit.model.Sex;
import org.eclipse.emfcloud.jackson.junit.model.User;
import org.eclipse.emfcloud.jackson.module.EMFModule;
import org.eclipse.emfcloud.jackson.resource.JsonResource;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NoMinimizedTypeTest {

   private ObjectMapper mapper;
   private ResourceSetImpl resourceSet;

   @Before
   public void setUp() {
      mapper = new ObjectMapper();
      resourceSet = new ResourceSetImpl();

      EMFModule module = new EMFModule();
      module.configure(OPTION_MINIMIZE_TYPE_INFO, false);
      mapper.registerModule(module);

      resourceSet.getResourceFactoryRegistry()
         .getExtensionToFactoryMap()
         .put("*", new JsonResourceFactory(mapper));
   }

   @Test
   public void testSaveSingleObjectWithFullType() {
      JsonNode expected = mapper.createObjectNode()
         .put("userId", "1")
         .put("name", "Paul")
         .put("eClass", "http://www.emfjson.org/jackson/model#//User");

      User u1 = ModelFactory.eINSTANCE.createUser();
      u1.setUserId("1");
      u1.setName("Paul");

      JsonNode result = mapper.valueToTree(u1);

      assertEquals(expected, result);
   }

   @Test
   public void testSaveTwoRootObjectsWithFullType() {
      JsonNode expected = mapper.createArrayNode()
         .add(mapper.createObjectNode()
            .put("userId", "1")
            .put("name", "Paul")
            .put("eClass", "http://www.emfjson.org/jackson/model#//User"))
         .add(mapper.createObjectNode()
            .put("userId", "2")
            .put("name", "Anna")
            .put("sex", "FEMALE")
            .put("eClass", "http://www.emfjson.org/jackson/model#//User"));

      User u1 = ModelFactory.eINSTANCE.createUser();
      u1.setUserId("1");
      u1.setName("Paul");

      User u2 = ModelFactory.eINSTANCE.createUser();
      u2.setUserId("2");
      u2.setName("Anna");
      u2.setSex(Sex.FEMALE);

      Resource resource = new JsonResource(URI.createURI("test"), mapper);
      resource.getContents().add(u1);
      resource.getContents().add(u2);

      JsonNode result = mapper.valueToTree(resource);

      assertEquals(expected, result);
   }

   @Test
   public void testSaveSingleObjectWithOneContainment() {
      JsonNode expected = mapper.createObjectNode()
         .put("userId", "1")
         .put("name", "Paul")
         .put("eClass", "http://www.emfjson.org/jackson/model#//User")
         .set("address", mapper.createObjectNode()
            .put("addId", "a1")
            .put("city", "Prague")
            .put("eClass", "http://www.emfjson.org/jackson/model#//Address"));

      User u1 = ModelFactory.eINSTANCE.createUser();
      u1.setUserId("1");
      u1.setName("Paul");

      Address add = ModelFactory.eINSTANCE.createAddress();
      add.setAddId("a1");
      add.setCity("Prague");

      u1.setAddress(add);

      JsonNode result = mapper.valueToTree(u1);

      assertEquals(expected, result);
   }

   @Test
   public void testSaveSingleObjectWithReferenceAndContainment() {
      JsonNode expected = mapper.createArrayNode()
         .add(mapper.createObjectNode()
            .put("eClass", "http://www.emfjson.org/jackson/model#//User")
            .put("userId", "1")
            .put("name", "Paul")
            .set("uniqueFriend", mapper.createObjectNode()
               .put("eClass", "http://www.emfjson.org/jackson/model#//User")
               .put("$ref", "2")))
         .add(mapper.createObjectNode()
            .put("eClass", "http://www.emfjson.org/jackson/model#//User")
            .put("userId", "2")
            .put("name", "Anna")
            .put("sex", "FEMALE")
            .set("address", mapper.createObjectNode()
               .put("addId", "a1")
               .put("city", "Prague")
               .put("eClass", "http://www.emfjson.org/jackson/model#//Address")));

      User u1 = ModelFactory.eINSTANCE.createUser();
      u1.setUserId("1");
      u1.setName("Paul");

      User u2 = ModelFactory.eINSTANCE.createUser();
      u2.setUserId("2");
      u2.setName("Anna");
      u2.setSex(Sex.FEMALE);

      u1.setUniqueFriend(u2);

      Address add = ModelFactory.eINSTANCE.createAddress();
      add.setAddId("a1");
      add.setCity("Prague");

      u2.setAddress(add);

      Resource resource = new JsonResource(URI.createURI("test"), mapper);
      resource.getContents().add(u1);
      resource.getContents().add(u2);
      JsonNode result = mapper.valueToTree(resource);

      assertEquals(expected, result);
   }

   @Test
   public void testSaveSingleObjectWithReferenceAndTwoLevelContainment() {
      JsonNode expected = mapper.createArrayNode()
         .add(mapper.createObjectNode()
            .put("eClass", "http://www.emfjson.org/jackson/model#//User")
            .put("userId", "1")
            .put("name", "Paul")
            .set("uniqueFriend", mapper.createObjectNode()
               .put("eClass", "http://www.emfjson.org/jackson/model#//User")
               .put("$ref", "2")))
         .add(mapper.createObjectNode()
            .put("eClass", "http://www.emfjson.org/jackson/model#//User")
            .put("userId", "2")
            .put("name", "Anna")
            .put("sex", "FEMALE")
            .set("address", mapper.createObjectNode()
               .put("addId", "a1")
               .put("city", "Prague")
               .put("eClass", "http://www.emfjson.org/jackson/model#//Address")));

      User u1 = ModelFactory.eINSTANCE.createUser();
      u1.setUserId("1");
      u1.setName("Paul");

      User u2 = ModelFactory.eINSTANCE.createUser();
      u2.setUserId("2");
      u2.setName("Anna");
      u2.setSex(Sex.FEMALE);

      u1.setUniqueFriend(u2);

      Address add = ModelFactory.eINSTANCE.createAddress();
      add.setAddId("a1");
      add.setCity("Prague");

      u2.setAddress(add);

      Resource resource = new JsonResource(URI.createURI("test"), mapper);
      resource.getContents().add(u1);
      resource.getContents().add(u2);
      JsonNode result = mapper.valueToTree(resource);

      assertEquals(expected, result);
   }

}
