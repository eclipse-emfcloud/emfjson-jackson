/*
 * Copyright (c) 2019 Guillaume Hillairet and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 */
package org.eclipse.emfcloud.jackson.tests.custom;

import static org.eclipse.emfcloud.jackson.databind.EMFContext.Attributes.RESOURCE_SET;
import static org.eclipse.emfcloud.jackson.databind.EMFContext.Attributes.ROOT_ELEMENT;
import static org.eclipse.emfcloud.jackson.module.EMFModule.Feature.OPTION_USE_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.IOException;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emfcloud.jackson.annotations.EcoreIdentityInfo;
import org.eclipse.emfcloud.jackson.annotations.EcoreReferenceInfo;
import org.eclipse.emfcloud.jackson.annotations.EcoreTypeInfo;
import org.eclipse.emfcloud.jackson.databind.EMFContext;
import org.eclipse.emfcloud.jackson.databind.deser.ReferenceEntry;
import org.eclipse.emfcloud.jackson.junit.model.ModelPackage;
import org.eclipse.emfcloud.jackson.junit.model.Sex;
import org.eclipse.emfcloud.jackson.junit.model.User;
import org.eclipse.emfcloud.jackson.module.EMFModule;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;
import org.eclipse.emfcloud.jackson.utils.ValueReader;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomDeserializersTest {

   private ObjectMapper mapper;
   private ResourceSetImpl resourceSet;

   @Before
   public void setUp() {
      mapper = new ObjectMapper();
      resourceSet = new ResourceSetImpl();
      resourceSet.getPackageRegistry().put(ModelPackage.eNS_URI, ModelPackage.eINSTANCE);
      resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new JsonResourceFactory(mapper));
   }

   @Test
   public void testDeserializeTypeValueWithOtherFieldName() throws JsonProcessingException {
      EMFModule module = new EMFModule();
      module.setTypeInfo(new EcoreTypeInfo("type"));
      mapper.registerModule(module);

      JsonNode data = mapper.createObjectNode()
         .put("type", "http://www.emfjson.org/jackson/model#//User")
         .put("userId", "u1");

      Resource resource = mapper
         .reader()
         .withAttribute(RESOURCE_SET, resourceSet)
         .treeToValue(data, Resource.class);

      assertEquals(1, resource.getContents().size());
      assertEquals(ModelPackage.Literals.USER, resource.getContents().get(0).eClass());

      User u = (User) resource.getContents().get(0);

      assertEquals("u1", u.getUserId());
      assertEquals(0, u.getFriends().size());
      assertNull(u.getUniqueFriend());
      assertNull(u.getAddress());
   }

   @Test
   public void testDeserializeTypeValue() throws JsonProcessingException {
      EMFModule module = new EMFModule();
      module.setTypeInfo(new EcoreTypeInfo("type", (ValueReader<String, EClass>) (value, context) -> (EClass) ModelPackage.eINSTANCE.getEClassifier(value)));
      mapper.registerModule(module);

      JsonNode data = mapper.createObjectNode()
         .put("type", "User")
         .put("userId", "u1");

      Resource resource = mapper
         .reader()
         .withAttribute(RESOURCE_SET, resourceSet)
         .treeToValue(data, Resource.class);

      assertEquals(1, resource.getContents().size());
      assertEquals(ModelPackage.Literals.USER, resource.getContents().get(0).eClass());

      User u = (User) resource.getContents().get(0);

      assertEquals("u1", u.getUserId());
      assertEquals(Sex.MALE, u.getSex());
      assertEquals(0, u.getFriends().size());
      assertNull(u.getUniqueFriend());
      assertNull(u.getAddress());
   }

   @Test
   public void testDeserializeIdValueWithOtherFieldName() throws JsonProcessingException {
      EMFModule module = new EMFModule();
      module.configure(OPTION_USE_ID, true);
      module.setIdentityInfo(new EcoreIdentityInfo("_id"));
      mapper.registerModule(module);

      JsonNode data = mapper.createObjectNode()
         .put("_id", "1")
         .put("userId", "u1");

      Resource resource = mapper
         .reader()
         .withAttribute(RESOURCE_SET, resourceSet)
         .withAttribute(ROOT_ELEMENT, ModelPackage.Literals.USER)
         .treeToValue(data, Resource.class);

      assertEquals(1, resource.getContents().size());
      assertEquals(ModelPackage.Literals.USER, resource.getContents().get(0).eClass());

      User u = (User) resource.getContents().get(0);

      assertEquals("u1", u.getUserId());
      assertSame(u, resource.getEObject("1"));
   }

   @Test
   public void testDeserializeIdValue() throws JsonProcessingException {
      EMFModule module = new EMFModule();
      module.configure(OPTION_USE_ID, true);
      module.setIdentityInfo(new EcoreIdentityInfo("_id", (ValueReader<Object, String>) (value, context) -> value.toString()));
      mapper.registerModule(module);

      JsonNode data = mapper.createObjectNode()
         .put("_id", 1)
         .put("userId", "u1");

      Resource resource = mapper
         .reader()
         .withAttribute(RESOURCE_SET, resourceSet)
         .withAttribute(ROOT_ELEMENT, ModelPackage.Literals.USER)
         .treeToValue(data, Resource.class);

      assertEquals(1, resource.getContents().size());
      assertEquals(ModelPackage.Literals.USER, resource.getContents().get(0).eClass());

      User u = (User) resource.getContents().get(0);

      assertEquals("u1", u.getUserId());
      assertSame(u, resource.getEObject("1"));
   }

   @Test
   public void testDeserializeReferenceAsStrings() throws JsonProcessingException {
      EMFModule module = new EMFModule();
      module.configure(EMFModule.Feature.OPTION_USE_ID, true);
      module.configure(EMFModule.Feature.OPTION_SERIALIZE_TYPE, false);

      module.setReferenceDeserializer(new JsonDeserializer<ReferenceEntry>() {
         @Override
         public ReferenceEntry deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            final EObject parent = EMFContext.getParent(ctxt);
            final EReference reference = EMFContext.getReference(ctxt);

            if (p.getCurrentToken() == JsonToken.FIELD_NAME) {
               p.nextToken();
            }

            return new ReferenceEntry.Base(parent, reference, p.getText());
         }
      });

      mapper.registerModule(module);

      JsonNode data = mapper.createArrayNode()
         .add(mapper.createObjectNode()
            .put("@id", "1")
            .put("name", "Paul")
            .put("uniqueFriend", "2"))
         .add(mapper.createObjectNode()
            .put("@id", "2")
            .put("name", "Franck"));

      Resource resource = mapper
         .reader()
         .withAttribute(RESOURCE_SET, resourceSet)
         .withAttribute(ROOT_ELEMENT, ModelPackage.Literals.USER)
         .treeToValue(data, Resource.class);

      assertEquals(2, resource.getContents().size());

      User u1 = (User) resource.getContents().get(0);
      User u2 = (User) resource.getContents().get(1);

      assertSame(u2, u1.getUniqueFriend());
   }

   @Test
   public void testDeserializeReferenceWithOtherFieldNames() throws JsonProcessingException {
      EMFModule module = new EMFModule();
      module.setTypeInfo(new EcoreTypeInfo("my_type"));
      module.setReferenceInfo(new EcoreReferenceInfo("my_ref"));
      mapper.registerModule(module);

      JsonNode data = mapper.createArrayNode()
         .add(mapper.createObjectNode()
            .put("name", "Paul")
            .set("uniqueFriend", mapper.createObjectNode()
               .put("my_type", "http://www.emfjson.org/jackson/model#//User")
               .put("my_ref", "/1")))
         .add(mapper.createObjectNode()
            .put("name", "Franck"));

      Resource resource = mapper
         .reader()
         .withAttribute(RESOURCE_SET, resourceSet)
         .withAttribute(ROOT_ELEMENT, ModelPackage.Literals.USER)
         .treeToValue(data, Resource.class);

      assertEquals(2, resource.getContents().size());

      User u1 = (User) resource.getContents().get(0);
      User u2 = (User) resource.getContents().get(1);

      assertSame(u2, u1.getUniqueFriend());
   }
}
