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
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.jackson.databind.EMFContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Serializes values of an {@link EMap}.
 *
 * @author vhemery
 */
public class EMapValueSerializer extends JsonSerializer<Object> {

   @Override
   public void serialize(final Object value, final JsonGenerator gen, final SerializerProvider serializers)
      throws java.io.IOException {
      EStructuralFeature feature = EMFContext.getFeature(serializers);
      Optional<EReference> mapRef = Optional.ofNullable(feature).filter(EReference.class::isInstance)
         .map(EReference.class::cast);
      Optional<EStructuralFeature> valueFeature = mapRef.map(EReference::getEReferenceType)
         .map(mapType -> mapType.getEStructuralFeature("value")).filter(Objects::nonNull);
      Optional<EDataType> valueDataType = valueFeature.filter(EAttribute.class::isInstance).map(EAttribute.class::cast)
         .map(EAttribute::getEAttributeType);
      Optional<EClass> valueEClass = valueFeature.filter(EReference.class::isInstance).map(EReference.class::cast)
         .map(EReference::getEReferenceType);
      if (valueDataType.isPresent()) {
         gen.writeString(EcoreUtil.convertToString(valueDataType.get(), value));
      } else if (valueEClass.isPresent() && value instanceof EObject) {
         gen.writeObject(value);
      } else {
         // the metamodel is probably incorrect...
         gen.writeString(value.toString());
      }
   }
}
