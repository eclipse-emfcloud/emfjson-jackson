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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emfcloud.jackson.junit.featureMapMixed.AType;
import org.eclipse.emfcloud.jackson.junit.featureMapMixed.BType;
import org.eclipse.emfcloud.jackson.junit.featureMapMixed.DocumentRoot;
import org.eclipse.emfcloud.jackson.junit.featureMapMixed.FeatureMapMixedFactory;
import org.eclipse.emfcloud.jackson.junit.featureMapMixed.FeatureMapMixedPackage;
import org.eclipse.emfcloud.jackson.junit.model.ModelFactory;
import org.eclipse.emfcloud.jackson.junit.model.ModelPackage;
import org.eclipse.emfcloud.jackson.junit.model.PrimaryObject;
import org.eclipse.emfcloud.jackson.junit.model.TargetObject;
import org.eclipse.emfcloud.jackson.module.EMFModule;
import org.eclipse.emfcloud.jackson.support.StandardFixture;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RunWith(Parameterized.class)
public class FeatureMapTest {

   @Parameterized.Parameters
   public static Collection<Boolean> booleans() {
      return List.of(true, false);
   }

   @ClassRule
   public static StandardFixture fixture = new StandardFixture();
   private final ResourceSet resourceSet = fixture.getResourceSet();
   private final ObjectMapper mapper;
   private final Boolean optionUseFeatureMapKeyAndValueProperties;

   public FeatureMapTest(final Boolean optionUseFeatureMapKeyAndValueProperties) {
      this.optionUseFeatureMapKeyAndValueProperties = optionUseFeatureMapKeyAndValueProperties;
      mapper = fixture.mapper(EMFModule.Feature.OPTION_USE_FEATURE_MAP_KEY_AND_VALUE_PROPERTIES,
         optionUseFeatureMapKeyAndValueProperties);
   }

   @Test
   public void testSaveFeatureMap() throws IOException {
      ObjectNode expected = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/model#//PrimaryObject")
         .put("name", "junit");

      if (optionUseFeatureMapKeyAndValueProperties) {
         expected.set("featureMapAttributeCollection", mapper.createArrayNode()
            .add(mapper.createObjectNode()
               .put("featureName", "featureMapAttributeType1")
               .put("value", "Hello"))
            .add(mapper.createObjectNode()
               .put("featureName", "featureMapAttributeType2")
               .put("value", "World"))
            .add(mapper.createObjectNode()
               .put("featureName", "featureMapAttributeType1")
               .put("value", "!")));
      } else {
         expected.set("featureMapAttributeCollection", mapper.createArrayNode()
            .add(mapper.createObjectNode()
               .put("featureMapAttributeType1", "Hello"))
            .add(mapper.createObjectNode()
               .put("valfeatureMapAttributeType2ue", "World"))
            .add(mapper.createObjectNode()
               .put("featureMapAttributeType1", "!")));
      }

      PrimaryObject primaryObject = ModelFactory.eINSTANCE.createPrimaryObject();
      primaryObject.setName("junit");

      primaryObject.getFeatureMapAttributeCollection()
         .add(ModelPackage.Literals.PRIMARY_OBJECT__FEATURE_MAP_ATTRIBUTE_TYPE1, "Hello");
      primaryObject.getFeatureMapAttributeCollection()
         .add(ModelPackage.Literals.PRIMARY_OBJECT__FEATURE_MAP_ATTRIBUTE_TYPE2, "World");
      primaryObject.getFeatureMapAttributeCollection()
         .add(ModelPackage.Literals.PRIMARY_OBJECT__FEATURE_MAP_ATTRIBUTE_TYPE1, "!");

      assertEquals(3, primaryObject.getFeatureMapAttributeCollection().size());
      assertEquals(2, primaryObject.getFeatureMapAttributeType1().size());
      assertEquals(1, primaryObject.getFeatureMapAttributeType2().size());

      Resource resource = resourceSet.createResource(URI.createURI("test.json"));
      resource.getContents().add(primaryObject);

      System.out.println(mapper.valueToTree(resource));

      assertEquals(expected, mapper.valueToTree(resource));
   }

   @Test
   public void testLoadFeatureMap() throws IOException {
      Resource resource = resourceSet.getResource(URI.createURI(optionUseFeatureMapKeyAndValueProperties
         ? "src/test/resources/tests/test-load-feature-map-use-properties.json"
         : "src/test/resources/tests/test-load-feature-map.json"), true);

      assertEquals(1, resource.getContents().size());
      assertTrue(resource.getContents().get(0) instanceof PrimaryObject);

      PrimaryObject o = (PrimaryObject) resource.getContents().get(0);
      assertEquals("junit", o.getName());
      assertArrayEquals(new String[] { "Hello", "World", "!" },
         o.getFeatureMapAttributeCollection().stream().map(FeatureMap.Entry::getValue).map(Object::toString).toArray());
      assertEquals("Hello", o.getFeatureMapAttributeType1().get(0));
      assertEquals("World", o.getFeatureMapAttributeType2().get(0));
      assertEquals("!", o.getFeatureMapAttributeType1().get(1));
   }

   @Test
   public void testLoadFeatureMapReferences() throws IOException {
      Resource resource = resourceSet.getResource(URI.createURI(optionUseFeatureMapKeyAndValueProperties
         ? "src/test/resources/tests/test-load-feature-map-refs-use-properties.json"
         : "src/test/resources/tests/test-load-feature-map-refs.json"), true);

      assertEquals(1, resource.getContents().size());
      assertEquals(ModelPackage.Literals.PRIMARY_OBJECT, resource.getContents().get(0).eClass());

      PrimaryObject p = (PrimaryObject) resource.getContents().get(0);

      assertEquals(6, p.getFeatureMapReferenceCollection().size());
      assertEquals(2, p.getFeatureMapReferenceType1().size());
      assertEquals(4, p.getFeatureMapReferenceType2().size());

      TargetObject t1 = p.getFeatureMapReferenceType2().get(0);
      assertEquals("1", t1.getSingleAttribute());
      assertEquals(t1, p.getFeatureMapReferenceCollection().get(1).getValue());

      TargetObject t2 = p.getFeatureMapReferenceType2().get(1);
      assertEquals("2", t2.getSingleAttribute());
      assertEquals(t2, p.getFeatureMapReferenceCollection().get(2).getValue());

      TargetObject t3 = p.getFeatureMapReferenceType2().get(2);
      assertEquals("3", t3.getSingleAttribute());
      assertEquals(t1, p.getFeatureMapReferenceCollection().get(3).getValue());

      TargetObject t4 = p.getFeatureMapReferenceType2().get(3);
      assertEquals("4", t4.getSingleAttribute());
      assertEquals(t1, p.getFeatureMapReferenceCollection().get(4).getValue());

      assertEquals(t1, p.getFeatureMapReferenceType1().get(0));
      assertEquals(t1, p.getFeatureMapReferenceCollection().get(0).getValue());
      assertEquals(t2, p.getFeatureMapReferenceType1().get(1));
      assertEquals(t2, p.getFeatureMapReferenceCollection().get(5).getValue());
   }

   @Test
   public void testSaveFeatureMapReferences() throws IOException {
      ObjectNode expected = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/model#//PrimaryObject");

      if (optionUseFeatureMapKeyAndValueProperties) {
         expected.set("featureMapReferenceCollection", mapper.createArrayNode()
            .add(mapper.createObjectNode()
               .put("featureName", "featureMapReferenceType1")
               .set("value", mapper.createObjectNode()
                  .put("$ref", "//@featureMapReferenceCollection.1/value")))
            .add(mapper.createObjectNode()
               .put("featureName", "featureMapReferenceType2")
               .set("value", mapper.createObjectNode()
                  .put("singleAttribute", "1")))
            .add(mapper.createObjectNode()
               .put("featureName", "featureMapReferenceType2")
               .set("value", mapper.createObjectNode()
                  .put("singleAttribute", "2")))
            .add(mapper.createObjectNode()
               .put("featureName", "featureMapReferenceType2")
               .set("value", mapper.createObjectNode()
                  .put("singleAttribute", "3")))
            .add(mapper.createObjectNode()
               .put("featureName", "featureMapReferenceType2")
               .set("value", mapper.createObjectNode()
                  .put("singleAttribute", "4")))
            .add(mapper.createObjectNode()
               .put("featureName", "featureMapReferenceType2")
               .set("value", mapper.createObjectNode()
                  .put("$ref", "//@featureMapReferenceCollection.2/value"))));
      } else {
         expected.set("featureMapReferenceCollection", mapper.createArrayNode()
            .add(mapper.createObjectNode()
               .set("featureMapReferenceType1", mapper.createObjectNode()
                  .put("$ref", "//@featureMapReferenceCollection.1/featureMapReferenceType2")))
            .add(mapper.createObjectNode()
               .set("featureMapReferenceType2", mapper.createObjectNode()
                  .put("singleAttribute", "1")))
            .add(mapper.createObjectNode()
               .set("featureMapReferenceType2", mapper.createObjectNode()
                  .put("singleAttribute", "2")))
            .add(mapper.createObjectNode()
               .set("featureMapReferenceType2", mapper.createObjectNode()
                  .put("singleAttribute", "3")))
            .add(mapper.createObjectNode()
               .set("featureMapReferenceType2", mapper.createObjectNode()
                  .put("singleAttribute", "4")))
            .add(mapper.createObjectNode()
               .set("featureMapReferenceType2", mapper.createObjectNode()
                  .put("$ref", "//@featureMapReferenceCollection.2/featureMapReferenceType2"))));
      }

      Resource resource = resourceSet.createResource(URI.createURI("tests.json"));
      assertNotNull(resource);

      PrimaryObject p = ModelFactory.eINSTANCE.createPrimaryObject();
      TargetObject t1 = ModelFactory.eINSTANCE.createTargetObject();
      t1.setSingleAttribute("1");
      TargetObject t2 = ModelFactory.eINSTANCE.createTargetObject();
      t2.setSingleAttribute("2");
      TargetObject t3 = ModelFactory.eINSTANCE.createTargetObject();
      t3.setSingleAttribute("3");
      TargetObject t4 = ModelFactory.eINSTANCE.createTargetObject();
      t4.setSingleAttribute("4");

      p.getFeatureMapReferenceType1().add(t1);

      p.getFeatureMapReferenceType2().add(t1);
      p.getFeatureMapReferenceType2().add(t2);
      p.getFeatureMapReferenceType2().add(t3);
      p.getFeatureMapReferenceType2().add(t4);

      p.getFeatureMapReferenceType1().add(t2);

      resource.getContents().add(p);

      assertEquals(expected, mapper.valueToTree(resource));
   }

   @Test
   public void testSaveFeatureMapMixedContent() throws IOException {
      ObjectNode expected = mapper.createObjectNode()
         .put("eClass", "http://www.emfjson.org/jackson/featureMapMixed#//DocumentRoot");

      if (optionUseFeatureMapKeyAndValueProperties) {
         expected.set("mixed", mapper.createArrayNode()
            .add(mapper.createObjectNode()
               .put("featureName", "B")
               .set("value", mapper.createObjectNode()
                  .put("attributeB", "valueB")))
            .add(mapper.createObjectNode()
               .put("featureName", "A")
               .set("value", mapper.createObjectNode()
                  .put("attributeA", "valueA"))));
      } else {
         expected.set("mixed", mapper.createArrayNode()
            .add(mapper.createObjectNode()
               .set("B", mapper.createObjectNode()
                  .put("attributeB", "valueB")))
            .add(mapper.createObjectNode()
               .set("A", mapper.createObjectNode()
                  .put("attributeA", "valueA"))));
      }

      DocumentRoot root = FeatureMapMixedFactory.eINSTANCE.createDocumentRoot();

      BType b = FeatureMapMixedFactory.eINSTANCE.createBType();
      b.setAttributeB("valueB");
      root.getMixed().add(FeatureMapMixedPackage.Literals.DOCUMENT_ROOT__BELEMENTS, b);

      AType a = FeatureMapMixedFactory.eINSTANCE.createAType();
      a.setAttributeA("valueA");
      root.getMixed().add(FeatureMapMixedPackage.Literals.DOCUMENT_ROOT__AELEMENTS, a);

      assertEquals(2, root.getMixed().size());
      assertNotNull(root.getAElements());
      assertNotNull(root.getBElements());

      Resource resource = resourceSet.createResource(URI.createURI("test.json"));
      resource.getContents().add(root);

      System.out.println(mapper.valueToTree(resource));

      assertEquals(expected, mapper.valueToTree(resource));
   }

   @Test
   public void testLoadFeatureMapMixedContent() throws IOException {
      Resource resource = resourceSet.getResource(URI.createURI(optionUseFeatureMapKeyAndValueProperties
         ? "src/test/resources/tests/test-load-feature-map-mixed-use-properties.json"
         : "src/test/resources/tests/test-load-feature-map-mixed.json"), true);

      assertEquals(1, resource.getContents().size());
      assertTrue(resource.getContents().get(0) instanceof DocumentRoot);

      DocumentRoot root = (DocumentRoot) resource.getContents().get(0);
      Object b = root.getMixed().get(0).getValue();
      assertTrue(b instanceof BType);
      assertEquals(root.getBElements(), b);
      assertEquals("valueB", root.getBElements().getAttributeB());

      Object a = root.getMixed().get(1).getValue();
      assertTrue(a instanceof AType);
      assertEquals(root.getAElements(), a);
      assertEquals("valueA", root.getAElements().getAttributeA());
   }

}
