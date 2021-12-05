/*
 * Copyright (c) 2019 Guillaume Hillairet and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 */
package org.eclipse.emfcloud.jackson.databind.ser;

import java.io.IOException;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.jackson.databind.EMFContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class EDataTypeSerializer extends JsonSerializer<Object> {

   @Override
   public void serialize(final Object value, final JsonGenerator gen, final SerializerProvider serializers)
      throws IOException {
      EAttribute feature = (EAttribute) EMFContext.getFeature(serializers);

      if (feature != null) {
         gen.writeString(EcoreUtil.convertToString(feature.getEAttributeType(), value));
      } else {
         gen.writeNull();
      }
   }

}
