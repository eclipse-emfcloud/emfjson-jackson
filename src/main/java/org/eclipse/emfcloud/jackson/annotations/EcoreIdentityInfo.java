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
package org.eclipse.emfcloud.jackson.annotations;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emfcloud.jackson.databind.EMFContext;
import org.eclipse.emfcloud.jackson.resource.JsonResource;
import org.eclipse.emfcloud.jackson.utils.ValueReader;
import org.eclipse.emfcloud.jackson.utils.ValueWriter;

public class EcoreIdentityInfo {

   public static final String PROPERTY = "@id";
   private static final ValueReader<Object, String> DEFAULT_VALUE_READER = (value, context) -> value.toString();
   private static final ValueWriter<EObject, Object> DEFAULT_VALUE_WRITER = (object, context) -> {
      Resource resource = EMFContext.getResource(context, object);
      Object id;
      if (resource instanceof JsonResource) {
         id = ((JsonResource) resource).getID(object);
      } else {
         id = EMFContext.getURI(context, object).fragment();
      }
      return id;
   };

   private final String property;
   private final ValueReader<Object, String> valueReader;
   private final ValueWriter<EObject, Object> valueWriter;

   public EcoreIdentityInfo() {
      this(null, null, null);
   }

   public EcoreIdentityInfo(final String property) {
      this(property, null, null);
   }

   public EcoreIdentityInfo(final String property, final ValueReader<Object, String> valueReader) {
      this(property, valueReader, null);
   }

   public EcoreIdentityInfo(final String property, final ValueWriter<EObject, Object> valueWriter) {
      this(property, null, valueWriter);
   }

   public EcoreIdentityInfo(final String property, final ValueReader<Object, String> valueReader,
      final ValueWriter<EObject, Object> valueWriter) {
      this.property = property == null ? PROPERTY : property;
      this.valueReader = valueReader == null ? DEFAULT_VALUE_READER : valueReader;
      this.valueWriter = valueWriter == null ? DEFAULT_VALUE_WRITER : valueWriter;
   }

   public String getProperty() { return property; }

   public ValueReader<Object, String> getValueReader() { return valueReader; }

   public ValueWriter<EObject, Object> getValueWriter() { return valueWriter; }
}
