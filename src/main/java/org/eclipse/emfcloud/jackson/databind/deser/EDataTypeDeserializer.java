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
package org.eclipse.emfcloud.jackson.databind.deser;

import static org.eclipse.emf.ecore.EcorePackage.Literals.EJAVA_CLASS;
import static org.eclipse.emf.ecore.EcorePackage.Literals.EJAVA_OBJECT;

import java.io.IOException;

import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.jackson.databind.EMFContext;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class EDataTypeDeserializer extends JsonDeserializer<Object> {

   public static boolean isJavaLangType(final EDataType dataType) {
      String instanceClassName = dataType.getInstanceClassName();
      return instanceClassName.startsWith("java.lang.") || instanceClassName.indexOf('.') < 0;
   }

   @Override
   public Object deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
      final EDataType dataType = EMFContext.getDataType(ctxt);

      if (dataType == null) {
         return null;
      }
      Class<?> type = dataType.getInstanceClass();

      if (type == null || (!isJavaLangType(dataType)) || EJAVA_CLASS.equals(dataType)
         || EJAVA_OBJECT.equals(dataType)) {
         return EcoreUtil.createFromString(dataType, jp.getText());
      }
      return ctxt.readValue(jp, type);
   }

}
