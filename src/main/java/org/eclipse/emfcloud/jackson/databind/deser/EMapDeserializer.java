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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emfcloud.jackson.databind.EMFContext;
import org.eclipse.emfcloud.jackson.utils.EObjects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class EMapDeserializer extends JsonDeserializer<EList<Map.Entry<?, ?>>> {

   @Override
   public EList<Map.Entry<?, ?>> deserialize(final JsonParser jp, final DeserializationContext ctxt)
      throws IOException {
      return null;
   }

   @Override

   @SuppressWarnings({ "unchecked", "checkstyle:cyclomaticComplexity", "rawtypes" })
   public EList<Map.Entry<?, ?>> deserialize(final JsonParser jp, final DeserializationContext ctxt,
      final EList<Map.Entry<?, ?>> intoValue) throws IOException {
      final EObject parent = EMFContext.getParent(ctxt);
      final EReference mapReference = EMFContext.getReference(ctxt);
      final Optional<EStructuralFeature> valueFeature = extractValueFeature(ctxt);

      if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
         while (jp.nextToken() != JsonToken.END_OBJECT) {
            // At each loop context, restore the context, which may have gotten deeper exploring children
            restoreContextForMapValue(ctxt, parent, valueFeature);

            String key = jp.getCurrentName();
            jp.nextToken();

            final Object value;
            if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
               value = ctxt.readValue(jp, EObject.class);
            } else {
               value = ctxt.readValue(jp, Object.class);
            }

            // Dynamic objects do not use the EMap interface
            // but store entries in a DynamicEList instead.
            if (intoValue instanceof EMap) {
               ((EMap) intoValue).put(key, value);
            } else if (mapReference != null) {
               intoValue.add((Map.Entry<?, ?>) EObjects.createEntry(key, value, mapReference.getEReferenceType()));
            }
         }
      }

      return intoValue;
   }

   /**
    * Restore the deserialization context in a state adequate to read the map value.
    *
    * @param ctxt         deserialization context to update
    * @param parent       the parent holding the map feature
    * @param valueFeature the feature holding the value of a Map Entry
    */
   private void restoreContextForMapValue(final DeserializationContext ctxt, final EObject parent,
      final Optional<EStructuralFeature> valueFeature) {
      EMFContext.setParent(ctxt, parent);
      valueFeature.ifPresent(f -> EMFContext.setFeature(ctxt, f));
   }

   /**
    * Extract the value feature from the deserialization context, at the time of EMap feature reading.
    *
    * @param ctxt deserialization context
    * @return the feature holding the value of a Map Entry
    */
   private Optional<EStructuralFeature> extractValueFeature(final DeserializationContext ctxt) {
      EReference mapReference = EMFContext.getReference(ctxt);
      return Optional.ofNullable(mapReference).flatMap(ref -> {
         EClass referenceType = ref.getEReferenceType();
         EStructuralFeature valueFeature = referenceType.getEStructuralFeature("value");
         return Optional.ofNullable(valueFeature);
      });
   }

}
