/********************************************************************************
 * Copyright (c) 2022 CS GROUP and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.jackson.databind.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serializes a null key in an EMap.
 *
 * @author vhemery
 */
public class NullKeySerializer extends StdSerializer<Object> {

   /** Default serial UID. */
   private static final long serialVersionUID = 1L;

   /**
    * Constructs a new Null key serializer.
    */
   public NullKeySerializer() {
      super(null, false);
   }

   @Override
   public void serialize(final Object nullKey, final JsonGenerator gen, final SerializerProvider serializers)
      throws java.io.IOException {
      gen.writeFieldName("");
   }
}
