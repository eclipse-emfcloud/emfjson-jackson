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

import static org.eclipse.emf.ecore.util.EcoreUtil.getURI;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emfcloud.jackson.resource.JsonResource;
import org.eclipse.emfcloud.jackson.resource.JsonUuidResource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class UuidSupport {

   protected ResourceSet resourceSet = new ResourceSetImpl();

   protected String uuid(final EObject object) {
      return getURI(object).fragment();
   }

   protected String uuid(final JsonNode node) {
      return node.get("@id").asText();
   }

   protected Resource createUuidResource(final String name, final ObjectMapper mapper) {
      JsonResource resource = new JsonUuidResource(URI.createURI(name));
      resource.setObjectMapper(mapper);
      resourceSet.getResources().add(resource);
      return resource;
   }

}
