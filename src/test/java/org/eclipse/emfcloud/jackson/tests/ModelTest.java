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
import static org.assertj.core.api.Assertions.extractProperty;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.assertj.core.api.Condition;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emfcloud.jackson.databind.ser.NullKeySerializer;
import org.eclipse.emfcloud.jackson.module.EMFModule;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ModelTest {

   private ResourceSetImpl resourceSet;
   protected URI baseTestFilesFileDirectory = URI.createFileURI("src/test/resources/tests/");
   protected ObjectMapper mapper = new ObjectMapper();

   @Before
   public void setUp() {
      URI baseURI = URI.createURI("http://eclipselabs.org/emfjson/tests/");
      EPackage.Registry.INSTANCE.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);

      Resource.Factory.Registry.INSTANCE
         .getExtensionToFactoryMap().put("*", new JsonResourceFactory(mapper));

      resourceSet = new ResourceSetImpl();
      resourceSet.getURIConverter().getURIMap().put(baseURI, baseTestFilesFileDirectory);

      mapper.registerModule(new EMFModule());

      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
      dateFormat.setTimeZone(TimeZone.getDefault());

      mapper.setDateFormat(dateFormat);
      // add default serializer for null EMap key
      mapper.getSerializerProvider().setNullKeySerializer(new NullKeySerializer());

      resourceSet.getURIConverter()
         .getURIMap()
         .put(
            URI.createURI("http://emfjson/dynamic/model"),
            URI.createURI("src/test/resources/model/dynamic/model.json"));

      resourceSet.getURIConverter()
         .getURIMap()
         .put(
            URI.createURI("http://www.emfjson.org/jackson/model"),
            URI.createURI("src/test/resources/model/dynamic/model-2.json"));

      resourceSet.getURIConverter()
         .getURIMap()
         .put(
            URI.createURI("http://foo.org/p"),
            URI.createURI("src/test/resources/model/dynamic/model-1.json"));

      resourceSet.getURIConverter()
         .getURIMap()
         .put(
            URI.createURI("http://foo.org/p2"),
            URI.createURI("src/test/resources/model/dynamic/model-3.json"));
   }

   @Test
   public void testLoadModel() {
      Resource model = resourceSet.getResource(URI.createURI("http://foo.org/p"), true);

      assertThat(model).isNotNull();
      assertThat(model.getContents())
         .hasSize(1)
         .hasOnlyElementsOfType(EPackage.class);

      EPackage p = (EPackage) model.getContents().get(0);

      assertThat(p.getName()).isEqualTo("p");
      assertThat(p.getNsPrefix()).isEqualTo("p");
      assertThat(p.getNsURI()).isEqualTo("http://foo.org/p");

      assertThat(p.getEClassifiers())
         .hasSize(2)
         .hasOnlyElementsOfType(EClass.class);

      EClass a = (EClass) p.getEClassifiers().get(0);
      EClass b = (EClass) p.getEClassifiers().get(1);

      assertThat(a.getName()).isEqualTo("A");
      assertThat(a.getEStructuralFeatures()).hasSize(2);

      EStructuralFeature f1 = a.getEStructuralFeatures().get(0);
      EStructuralFeature f2 = a.getEStructuralFeatures().get(1);

      assertThat(f1.getName()).isEqualTo("label");
      assertThat(f1.getEType()).isSameAs(EcorePackage.Literals.ESTRING);

      assertThat(f2.getName()).isEqualTo("bs");
      assertThat(f2.getEType()).isSameAs(b);

      assertThat(b.getName()).isEqualTo("B");
      assertThat(b.getEStructuralFeatures()).isEmpty();
   }

   @Test
   public void testLoadModel2() {
      Resource model = resourceSet.getResource(URI.createURI("http://www.emfjson.org/jackson/model"), true);

      assertThat(model).isNotNull();
      assertThat(model.getContents())
         .hasSize(1)
         .hasOnlyElementsOfType(EPackage.class);

      EPackage p = (EPackage) model.getContents().get(0);

      assertThat(p.getName()).isEqualTo("model");
      assertThat(p.getNsPrefix()).isEqualTo("model");
      assertThat(p.getNsURI()).isEqualTo("http://www.emfjson.org/jackson/model");
      assertThat(p.getEClassifiers())
         .hasSize(5)
         .hasOnlyElementsOfType(EClass.class);

      EClass c1 = (EClass) p.getEClassifiers().get(0);
      EClass c2 = (EClass) p.getEClassifiers().get(1);
      EClass c3 = (EClass) p.getEClassifiers().get(2);
      EClass c4 = (EClass) p.getEClassifiers().get(3);
      EClass c5 = (EClass) p.getEClassifiers().get(4);

      assertThat(c1.getName()).isEqualTo("ETypes");
      assertThat(c1.getEStructuralFeatures()).hasSize(2);

      // features
      EStructuralFeature c1f1 = c1.getEStructuralFeatures().get(0);
      assertThat(c1f1.getName()).isEqualTo("values");
      assertThat(c1f1.getEType()).isSameAs(c2);

      EStructuralFeature c1f2 = c1.getEStructuralFeatures().get(1);
      assertThat(c1f2.getName()).isEqualTo("stringMapValues");
      assertThat(c1f2.getEType()).isSameAs(c3);

      // TMap
      assertThat(c2.getName()).isEqualTo("TMap");
      assertThat(c2.getInstanceClassName()).isEqualTo("java.util.Map.Entry");
      assertThat(c2.getEStructuralFeatures()).hasSize(2);

      // features
      EStructuralFeature c2f1 = c2.getEStructuralFeatures().get(0);
      assertThat(c2f1.getName()).isEqualTo("key");
      assertThat(c2f1.getEType()).isSameAs(c4);

      EStructuralFeature c2f2 = c2.getEStructuralFeatures().get(1);
      assertThat(c2f2.getName()).isEqualTo("value");
      assertThat(c2f2.getEType()).isSameAs(c5);

      // StringMap
      assertThat(c3.getName()).isEqualTo("StringMap");
      assertThat(c3.getInstanceClassName()).isEqualTo("java.util.Map.Entry");
      assertThat(c3.getEStructuralFeatures()).hasSize(2);

      // features
      EStructuralFeature c3f1 = c3.getEStructuralFeatures().get(0);
      assertThat(c3f1.getName()).isEqualTo("key");
      assertThat(c3f1.getEType()).isSameAs(EcorePackage.Literals.ESTRING);

      EStructuralFeature c3f2 = c3.getEStructuralFeatures().get(1);
      assertThat(c3f2.getName()).isEqualTo("value");
      assertThat(c3f2.getEType()).isSameAs(c5);

      // Type
      assertThat(c4.getName()).isEqualTo("Type");
      assertThat(c4.getEStructuralFeatures()).hasSize(1);

      // features
      EStructuralFeature c4f1 = c4.getEStructuralFeatures().get(0);
      assertThat(c4f1.getName()).isEqualTo("name");
      assertThat(c4f1.getEType()).isSameAs(EcorePackage.Literals.ESTRING);

      // Value
      assertThat(c5.getName()).isEqualTo("Value");
      assertThat(c5.getEStructuralFeatures()).hasSize(1);

      // features
      EStructuralFeature c5f1 = c5.getEStructuralFeatures().get(0);
      assertThat(c5f1.getName()).isEqualTo("value");
      assertThat(c5f1.getEType()).isSameAs(EcorePackage.Literals.EINT);
   }

   @Test
   public void testSaveEnums() {

   }

   @Test
   public void testLoadDynamicEnums() {
      Resource model = resourceSet.getResource(URI.createURI("http://emfjson/dynamic/model"), true);

      EPackage p = (EPackage) model.getContents().get(0);
      EClassifier kind = p.getEClassifier("Kind");

      assertThat(kind).isInstanceOf(EEnum.class);

      EEnum kindEnum = (EEnum) kind;
      EList<EEnumLiteral> literals = kindEnum.getELiterals();

      assertThat(literals)
         .doesNotContainNull()
         .hasSize(2)
         .have(new Condition<EEnumLiteral>() {
            @Override
            public boolean matches(final EEnumLiteral value) {
               return value.getName() != null;
            }
         });

      assertThat(extractProperty("name").from(literals))
         .containsExactly("e1", "e2");

      assertThat(extractProperty("literal").from(literals))
         .containsExactly("e1", "E2");
   }
}
