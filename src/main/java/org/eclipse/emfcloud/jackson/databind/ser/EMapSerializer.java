/*******************************************************************************
 * Copyright (c) 2022 CS GROUP and others.
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
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;

/**
 * An serializer for {@link EMap}, which delegates to {@link MapSerializer} for configurability.
 *
 * @author vhemery
 */
public class EMapSerializer extends JsonSerializer<EList<Map.Entry<?, ?>>> {

   /** The Map serializer we delegate the job to. */
   private final MapSerializer delegate;

   public EMapSerializer(final MapSerializer delegateMapSerialize) {
      this.delegate = delegateMapSerialize;
   }

   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Override
   public void serialize(final EList<Map.Entry<?, ?>> value, final JsonGenerator jg,
      final SerializerProvider serializers)
      throws IOException {
      if (value == null || value.isEmpty()) {
         jg.writeNull();
      } else if (value instanceof EMap) {
         delegate.serialize(((EMap) value).map(), jg, serializers);
      } else {
         // iterate on entries manually
         jg.writeStartObject();
         for (Map.Entry<?, ?> entry : value) {
            Object key = Optional.ofNullable((Object) entry.getKey()).orElse("");
            ((JsonSerializer<Object>) delegate.getKeySerializer()).serialize(key, jg, serializers);
            Object objectValue = entry.getValue();
            ((JsonSerializer<Object>) delegate.getContentSerializer()).serialize(objectValue, jg, serializers);
         }
         jg.writeEndObject();
      }
   }

}
