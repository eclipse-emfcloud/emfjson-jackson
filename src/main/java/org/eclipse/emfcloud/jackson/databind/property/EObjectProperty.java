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

package org.eclipse.emfcloud.jackson.databind.property;

import java.io.IOException;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;

public abstract class EObjectProperty {

   private final String fieldName;

   protected EObjectProperty(final String fieldName) {
      this.fieldName = fieldName;
   }

   public String getFieldName() { return fieldName; }

   public abstract void serialize(EObject bean, JsonGenerator jg, SerializerProvider provider) throws IOException;

   public abstract EObject deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException;

   public abstract void deserializeAndSet(JsonParser jp, EObject current, DeserializationContext ctxt,
      Resource resource) throws IOException;

}
