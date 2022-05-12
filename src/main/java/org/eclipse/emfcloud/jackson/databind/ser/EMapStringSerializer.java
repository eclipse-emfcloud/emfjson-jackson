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
package org.eclipse.emfcloud.jackson.databind.ser;

import java.io.IOException;
import java.util.Map;

import org.eclipse.emf.common.util.EList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @deprecated use {@link EMapSerializer} instead for configurability and resilience.
 * @see org.eclipse.emfcloud.jackson.databind.ser.EMFSerializers EMapSerializer example in findMapLikeSerializer.
 */
@Deprecated
public class EMapStringSerializer extends JsonSerializer<EList<Map.Entry<String, ?>>> {

   @Override
   public void serialize(final EList<Map.Entry<String, ?>> value, final JsonGenerator jg,
      final SerializerProvider serializers)
      throws IOException {
      if (value == null || value.isEmpty()) {
         jg.writeNull();
         return;
      }

      jg.writeStartObject();
      for (Map.Entry<String, ?> entry : value) {
         jg.writeObjectField(entry.getKey(), entry.getValue());
      }
      jg.writeEndObject();
   }

}
