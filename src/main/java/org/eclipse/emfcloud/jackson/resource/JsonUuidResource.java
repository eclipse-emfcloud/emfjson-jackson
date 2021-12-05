/*
 * Copyright (c) 2019 Guillaume Hillairet and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 */
package org.eclipse.emfcloud.jackson.resource;

import org.eclipse.emf.common.util.URI;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUuidResource extends JsonResource {

   public JsonUuidResource(final URI uri, final ObjectMapper mapper) {
      super(uri, mapper);
   }

   public JsonUuidResource(final URI uri) {
      super(uri);
   }

   @Override
   protected boolean useUUIDs() {
      return true;
   }

}
