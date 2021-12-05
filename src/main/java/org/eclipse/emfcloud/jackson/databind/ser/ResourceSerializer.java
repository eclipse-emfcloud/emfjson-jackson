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
package org.eclipse.emfcloud.jackson.databind.ser;

import java.io.IOException;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ResourceSerializer extends JsonSerializer<Resource> {

   // private final EcoreTypeFactory typeFactory = new EcoreTypeFactory();

   @Override
   public void serialize(final Resource value, final JsonGenerator jg, final SerializerProvider provider)
      throws IOException {
      if (value.getContents().size() == 1) {
         serializeOne(value.getContents().get(0), jg, provider);
      } else {
         jg.writeStartArray();
         for (EObject o : value.getContents()) {
            serializeOne(o, jg, provider);
         }
         jg.writeEndArray();
      }
   }

   private void serializeOne(final EObject object, final JsonGenerator jg, final SerializerProvider provider)
      throws IOException {
      final JavaType type = provider.constructType(object.getClass());
      final JsonSerializer<Object> serializer = provider.findValueSerializer(type);

      if (serializer != null) {
         serializer.serialize(object, jg, provider);
      }
   }

   @Override
   public Class<Resource> handledType() {
      return Resource.class;
   }

}
