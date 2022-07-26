/*******************************************************************************
 * Copyright (c) 2022 Jan Hicken.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 *******************************************************************************/
package org.eclipse.emfcloud.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.StringWriter;

public class RawDeserializer extends JsonDeserializer<Object> {
   @Override
   public Object deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
      final StringWriter writer = new StringWriter();
      final JsonGenerator generator = p.getCodec().getFactory().createGenerator(writer);
      final JsonNode tree = p.readValueAsTree();
      generator.writeTree(tree);

      return writer.toString();
   }

   @Override
   public boolean isCachable() {
      return true;
   }
}
