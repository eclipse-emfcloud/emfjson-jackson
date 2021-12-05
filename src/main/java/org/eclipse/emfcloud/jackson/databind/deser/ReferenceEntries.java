/*
 * Copyright (c) 2019 Guillaume Hillairet and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 */
package org.eclipse.emfcloud.jackson.databind.deser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emfcloud.jackson.handlers.URIHandler;

import com.fasterxml.jackson.databind.DatabindContext;

public class ReferenceEntries {

   private final Collection<ReferenceEntry> entries = new ArrayList<>();
   private final Map<String, EObject> mapOfObjects = new HashMap<>();

   /**
    * Resolves all reference entries that have been collected during deserialization.
    *
    * @param context current deserialization context
    * @param handler use for resolution of URIs
    */
   public void resolve(final DatabindContext context, final URIHandler handler) {
      for (ReferenceEntry entry : entries()) {
         entry.resolve(context, handler);
      }
      mapOfObjects.clear();
   }

   public Collection<ReferenceEntry> entries() {
      return entries;
   }

   public void store(final String id, final EObject value) {
      mapOfObjects.put(id, value);
   }

   public EObject get(final String id) {
      return mapOfObjects.get(id);
   }

}
