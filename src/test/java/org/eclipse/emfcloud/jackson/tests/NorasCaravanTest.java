/*******************************************************************************
 * Copyright (c) 2024 Bonitasoft and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 *******************************************************************************/
package org.eclipse.emfcloud.jackson.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.jackson.junit.caravan.CaravanPackage;
import org.eclipse.emfcloud.jackson.junit.caravan.DocumentRoot;
import org.eclipse.emfcloud.jackson.junit.caravan.util.CaravanResourceFactoryImpl;
import org.eclipse.emfcloud.jackson.module.EMFModule;
import org.eclipse.emfcloud.jackson.resource.JsonResource;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;
import org.eclipse.emfcloud.jackson.support.StandardFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This is a functional test which tests a full model sample for illustrating the usage of feature map entries.
 */
@RunWith(Parameterized.class)
public class NorasCaravanTest {

   @Parameterized.Parameters
   public static Collection<Boolean> booleans() {
      return List.of(true, false);
   }

   @ClassRule
   public static StandardFixture fixture = new StandardFixture();
   private final ResourceSet resourceSet = fixture.getResourceSet();
   private final ObjectMapper mapper;
   private final Boolean optionUseFeatureMapKeyAndValueProperties;

   @Before
   public void setUp() {
      // make sure Caravan package is loaded so that EMF does not try to get it from http
      EPackage.Registry.INSTANCE.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
      EPackage.Registry.INSTANCE.put(CaravanPackage.eNS_URI, CaravanPackage.eINSTANCE);
   }

   @After
   public void tearDown() {
      EPackage.Registry.INSTANCE.clear();
      Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().clear();
   }

   public NorasCaravanTest(final Boolean optionUseFeatureMapKeyAndValueProperties) {
      this.optionUseFeatureMapKeyAndValueProperties = optionUseFeatureMapKeyAndValueProperties;
      mapper = fixture.mapper(EMFModule.Feature.OPTION_USE_FEATURE_MAP_KEY_AND_VALUE_PROPERTIES,
         optionUseFeatureMapKeyAndValueProperties);
      // update the resource factory with correct mapper
      fixture.getResourceSet().getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
         new JsonResourceFactory(mapper));
   }

   @Test
   public void testSerializeModel() throws IOException {
      Resource xmi = getXmiResource();
      EObject root = xmi.getContents().get(0);

      Resource resource = resourceSet.createResource(URI.createURI("test.json"));
      ((JsonResource) resource).setObjectMapper(mapper);
      resource.getContents().add(root);

      JsonNode expected = mapper.readTree(Paths.get(getJsonPath()).toFile());

      assertEquals(expected, mapper.valueToTree(resource));
   }

   @Test
   public void testDeserializeModel() throws IOException {
      Resource resource = resourceSet.getResource(URI.createURI(getJsonPath()), true);

      assertEquals(1, resource.getContents().size());

      Resource xmi = getXmiResource();
      EObject expected = xmi.getContents().get(0);
      ((DocumentRoot) expected).eUnset(CaravanPackage.Literals.DOCUMENT_ROOT__XMLNS_PREFIX_MAP);

      assertTrue(EcoreUtil.equals(expected, resource.getContents().get(0)));
   }

   public Resource getXmiResource() {
      ResourceSet xmiResourceSet = new ResourceSetImpl();
      xmiResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new CaravanResourceFactoryImpl());
      xmiResourceSet.getPackageRegistry().put(CaravanPackage.eNS_URI, CaravanPackage.eINSTANCE);
      Resource xmi = xmiResourceSet.getResource(URI.createURI("src/test/resources/xmi/Nora'sCaravan.xmi"), true);
      return xmi;
   }

   /**
    * Get json resource path depending on the use properties option.
    *
    * @return appropriate json file path
    */
   public String getJsonPath() {
      return optionUseFeatureMapKeyAndValueProperties
         ? "src/test/resources/tests/Nora'sCaravan-use-properties.json"
         : "src/test/resources/tests/Nora'sCaravan.json";
   }

}
