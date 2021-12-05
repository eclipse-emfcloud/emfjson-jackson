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
import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.resource.Resource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class EObjectOperationProperty extends EObjectProperty {

   private final EOperation operation;

   protected EObjectOperationProperty(final String fieldName, final EOperation operation) {
      super(fieldName);

      this.operation = operation;
   }

   @Override
   public void serialize(final EObject bean, final JsonGenerator jg, final SerializerProvider provider)
      throws IOException {
      Object value;
      try {
         value = bean.eInvoke(operation, null);
      } catch (InvocationTargetException e) {
         // handle error
         value = null;
      }

      if (value != null) {
         jg.writeFieldName(getFieldName());
         JsonSerializer<Object> serializer = provider.findValueSerializer(value.getClass());
         serializer.serialize(value, jg, provider);
      }
   }

   @Override
   public EObject deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
      return null;
   }

   @Override
   public void deserializeAndSet(final JsonParser jp, final EObject current, final DeserializationContext ctxt,
      final Resource resource)
      throws IOException {
      // do nothing
   }

}
