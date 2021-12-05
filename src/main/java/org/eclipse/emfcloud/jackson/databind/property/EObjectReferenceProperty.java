/*
 * Copyright (c) 2019 Guillaume Hillairet and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 */

package org.eclipse.emfcloud.jackson.databind.property;

import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emfcloud.jackson.annotations.EcoreReferenceInfo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;

public class EObjectReferenceProperty extends EObjectProperty {

   public EObjectReferenceProperty(final EcoreReferenceInfo referenceInfo) {
      super(referenceInfo.getProperty());
   }

   @Override
   public void serialize(final EObject bean, final JsonGenerator jg, final SerializerProvider provider)
      throws IOException {
      // do nothing
   }

   @Override
   public EObject deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
      return null;
   }

   @Override
   public void deserializeAndSet(final JsonParser jp, final EObject current, final DeserializationContext ctxt,
      final Resource resource)
      throws IOException {
      String value = jp.nextTextValue();
      if (value != null) {
         ((InternalEObject) current).eSetProxyURI(URI.createURI(value));
      }
   }
}
