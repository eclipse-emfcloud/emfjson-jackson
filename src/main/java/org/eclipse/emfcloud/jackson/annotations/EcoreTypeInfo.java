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

import static org.eclipse.emfcloud.jackson.databind.EMFContext.findEClass;
import static org.eclipse.emfcloud.jackson.databind.EMFContext.getURI;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emfcloud.jackson.databind.EMFContext;
import org.eclipse.emfcloud.jackson.utils.ValueReader;
import org.eclipse.emfcloud.jackson.utils.ValueWriter;

public class EcoreTypeInfo {

   public enum USE {
      URI,
      NAME,
      CLASS
   }

   public static final String PROPERTY = "eClass";

   public static final ValueReader<String, EClass> DEFAULT_VALUE_READER = (value, context) -> findEClass(context, value);
   public static final ValueWriter<EClass, String> DEFAULT_VALUE_WRITER = (value, context) -> getURI(context, value)
      .toString();

   public static final ValueReader<String, EClass> READ_BY_NAME = (value, context) -> EMFContext.findEClassByName(context,
      value);
   public static final ValueWriter<EClass, String> WRITE_BY_NAME = (value, context) -> value != null ? value.getName()
      : null;

   public static final ValueReader<String, EClass> READ_BY_CLASS = (value, context) -> EMFContext
      .findEClassByQualifiedName(context, value);
   public static final ValueWriter<EClass, String> WRITE_BY_CLASS_NAME = (value,
      context) -> value != null ? value.getInstanceClassName() : null;

   private final String property;
   private final ValueReader<String, EClass> valueReader;
   private final ValueWriter<EClass, String> valueWriter;

   public EcoreTypeInfo() {
      this(null, null, null);
   }

   public EcoreTypeInfo(final String property) {
      this(property, null, null);
   }

   public EcoreTypeInfo(final String property, final ValueReader<String, EClass> valueReader) {
      this(property, valueReader, null);
   }

   public EcoreTypeInfo(final String property, final ValueWriter<EClass, String> valueWriter) {
      this(property, null, valueWriter);
   }

   public EcoreTypeInfo(final String property, final ValueReader<String, EClass> valueReader,
      final ValueWriter<EClass, String> valueWriter) {
      this.property = property == null ? PROPERTY : property;
      this.valueReader = valueReader == null ? DEFAULT_VALUE_READER : valueReader;
      this.valueWriter = valueWriter == null ? DEFAULT_VALUE_WRITER : valueWriter;
   }

   public String getProperty() { return property; }

   public ValueReader<String, EClass> getValueReader() { return valueReader; }

   public ValueWriter<EClass, String> getValueWriter() { return valueWriter; }

   public static EcoreTypeInfo create(final String property, final USE use) {
      switch (use) {
         case NAME:
            return new EcoreTypeInfo(property, READ_BY_NAME, WRITE_BY_NAME);
         case CLASS:
            return new EcoreTypeInfo(property, READ_BY_CLASS, WRITE_BY_CLASS_NAME);
         default:
            return new EcoreTypeInfo(property, DEFAULT_VALUE_READER, DEFAULT_VALUE_WRITER);
      }
   }
}
