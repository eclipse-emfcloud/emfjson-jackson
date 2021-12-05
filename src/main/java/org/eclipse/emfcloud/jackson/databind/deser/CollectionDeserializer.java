/*
 * Copyright (c) 2019 Guillaume Hillairet and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 */
package org.eclipse.emfcloud.jackson.databind.deser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emfcloud.jackson.databind.EMFContext;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;

public class CollectionDeserializer extends JsonDeserializer<Collection<Object>> {

   private final CollectionType baseType;
   private final JsonDeserializer<? extends EObject> deserializer;
   private final JsonDeserializer<? extends ReferenceEntry> referenceDeserializer;

   public CollectionDeserializer(final CollectionType type, final JsonDeserializer<? extends EObject> deserializer,
      final JsonDeserializer<ReferenceEntry> referenceDeserializer) {
      this.baseType = type;
      this.deserializer = deserializer;
      this.referenceDeserializer = referenceDeserializer;
   }

   @Override
   public Collection<Object> deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
      Collection<Object> values = createCollection(ctxt);
      while (p.nextToken() != JsonToken.END_ARRAY) {
         EObject result = deserializer.deserialize(p, ctxt);
         if (result != null) {
            values.add(result);
         }
      }
      return values;
   }

   @SuppressWarnings("unchecked")
   private Collection<Object> createCollection(final DeserializationContext ctxt) {
      CollectionType type = baseType;
      try {
         if (baseType.isAbstract() && baseType.isCollectionLikeType()) {
            type = (CollectionType) ctxt.getFactory().mapAbstractType(ctxt.getConfig(), type);
         }
         if (!type.isAbstract()) {
            return (Collection<Object>) type.getRawClass().getConstructor().newInstance();
         }
      } catch (Exception e) {}
      // use HashSet as fallback implementation for Sets which could not evaluate it's type
      if (type.isTypeOrSubTypeOf(Set.class)) {
         return new HashSet<>();
      }
      // fallback to default behavior
      return new ArrayList<>();
   }

   @Override
   public Collection<Object> deserialize(final JsonParser p, final DeserializationContext ctxt,
      final Collection<Object> intoValue)
      throws IOException {
      final EObject parent = EMFContext.getParent(ctxt);
      final EReference feature = EMFContext.getReference(ctxt);

      while (p.nextToken() != JsonToken.END_ARRAY) {
         EMFContext.setParent(ctxt, parent);
         EMFContext.setFeature(ctxt, feature);

         if (feature != null && feature.isContainment()) {
            EObject result = deserializer.deserialize(p, ctxt);
            if (result != null) {
               intoValue.add(result);
            }
         } else {
            ReferenceEntry entry = referenceDeserializer.deserialize(p, ctxt);
            if (entry != null) {
               intoValue.add(entry);
            }
         }
      }
      return intoValue;
   }
}
