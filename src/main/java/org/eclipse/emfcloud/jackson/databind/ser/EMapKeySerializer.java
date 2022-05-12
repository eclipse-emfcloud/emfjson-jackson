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

import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.jackson.databind.EMFContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Serializes keys of an {@link EMap}.
 *
 * @author vhemery
 */
public class EMapKeySerializer extends JsonSerializer<Object> {

   @Override
   public void serialize(final Object value, final JsonGenerator gen, final SerializerProvider serializers)
      throws java.io.IOException {
      EStructuralFeature feature = EMFContext.getFeature(serializers);
      Optional<EReference> mapRef = Optional.ofNullable(feature).filter(EReference.class::isInstance)
         .map(EReference.class::cast);
      Optional<EStructuralFeature> keyFeature = mapRef.map(EReference::getEReferenceType)
         .map(mapType -> mapType.getEStructuralFeature("key")).filter(Objects::nonNull);
      Optional<EDataType> keyType = keyFeature.filter(EAttribute.class::isInstance).map(EAttribute.class::cast)
         .map(EAttribute::getEAttributeType);
      if (keyType.isPresent()) {
         gen.writeFieldName(EcoreUtil.convertToString(keyType.get(), value));
      } else {
         // the metamodel is probably incorrect...
         gen.writeFieldName(value.toString());
      }
   }
}
