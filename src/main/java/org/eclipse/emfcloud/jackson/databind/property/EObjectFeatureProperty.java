/*******************************************************************************
 * Copyright (c) 2019-2022 Guillaume Hillairet and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 *******************************************************************************/

package org.eclipse.emfcloud.jackson.databind.property;

import static org.eclipse.emfcloud.jackson.annotations.JsonAnnotations.getElementName;
import static org.eclipse.emfcloud.jackson.annotations.JsonAnnotations.isRawValue;
import static org.eclipse.emfcloud.jackson.module.EMFModule.Feature.OPTION_SERIALIZE_DEFAULT_VALUE;

import java.io.IOException;

import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emfcloud.jackson.databind.EMFContext;
import org.eclipse.emfcloud.jackson.databind.deser.RawDeserializer;
import org.eclipse.emfcloud.jackson.databind.deser.ReferenceEntries;
import org.eclipse.emfcloud.jackson.databind.deser.ReferenceEntry;
import org.eclipse.emfcloud.jackson.databind.type.FeatureKind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.UnknownSerializer;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;

public class EObjectFeatureProperty extends EObjectProperty {

   private final EStructuralFeature feature;
   private final JavaType javaType;
   private final boolean defaultValues;

   private JsonSerializer<Object> serializer;
   private JsonDeserializer<Object> deserializer;

   public EObjectFeatureProperty(final EStructuralFeature feature, final JavaType type, final int features) {
      super(getElementName(feature, features));

      this.feature = feature;
      this.javaType = type;
      this.defaultValues = OPTION_SERIALIZE_DEFAULT_VALUE.enabledIn(features);

      if (isRawValue(feature)) {
         this.serializer = new RawSerializer<>(String.class);
         this.deserializer = new RawDeserializer();
      }
   }

   @Override
   @SuppressWarnings({ "checkstyle:cyclomaticComplexity", "checkstyle:fallThrough" })
   public void deserializeAndSet(final JsonParser jp, final EObject current, final DeserializationContext ctxt,
      final Resource resource)
      throws IOException {
      if (deserializer == null) {
         deserializer = ctxt.findContextualValueDeserializer(javaType, null);
      }
      JsonToken token = null;

      if (jp.getCurrentToken() == JsonToken.FIELD_NAME) {
         token = jp.nextToken();
      }

      if (jp.getCurrentToken() == JsonToken.VALUE_NULL) {
         return;
      }

      boolean isMap = false;
      switch (FeatureKind.get(feature)) {
         case MAP:
            isMap = true;
            //$FALL-THROUGH$
         case MANY_CONTAINMENT:
         case SINGLE_CONTAINMENT: {
            EMFContext.setFeature(ctxt, feature);
            EMFContext.setParent(ctxt, current);
         }
         //$FALL-THROUGH$
         case SINGLE_ATTRIBUTE:
         case MANY_ATTRIBUTE: {
            if (feature.getEType() instanceof EDataType) {
               EMFContext.setDataType(ctxt, feature.getEType());
               Class<?> clazz = feature.getEType().getInstanceClass();
               if (clazz != null && FeatureMap.Entry.class.isAssignableFrom(clazz)) {
                  // we need the parent to construct the feature map entry with correct feature
                  EMFContext.setParent(ctxt, current);
               }
            }

            if (feature.isMany()) {
               if (token != JsonToken.START_ARRAY && !isMap) {
                  throw new JsonParseException(jp, "Expected START_ARRAY token, got " + token);
               }

               deserializer.deserialize(jp, ctxt, current.eGet(feature));
            } else {
               Object value = deserializer.deserialize(jp, ctxt);

               if (value != null) {
                  current.eSet(feature, value);
               }
            }
         }
            break;
         case MANY_REFERENCE:
         case SINGLE_REFERENCE: {
            EMFContext.setFeature(ctxt, feature);
            EMFContext.setParent(ctxt, current);

            ReferenceEntries entries = EMFContext.getEntries(ctxt);
            if (feature.isMany()) {
               deserializer.deserialize(jp, ctxt, entries.entries());
            } else {
               Object value = deserializer.deserialize(jp, ctxt);
               if (entries != null && value instanceof ReferenceEntry) {
                  entries.entries().add((ReferenceEntry) value);
               }
            }
         }
            break;
         default:
            break;
      }
   }

   @Override
   public void serialize(final EObject bean, final JsonGenerator jg, final SerializerProvider provider)
      throws IOException {
      if (serializer == null) {
         serializer = provider.findValueSerializer(javaType);
      }

      EMFContext.setParent(provider, bean);
      EMFContext.setFeature(provider, feature);

      if (bean.eIsSet(feature)) {
         Object value = bean.eGet(feature, false);

         jg.writeFieldName(getFieldName());

         if (serializer instanceof UnknownSerializer) {
            JsonSerializer<Object> other = provider.findValueSerializer(value.getClass());
            if (other != null) {
               other.serialize(value, jg, provider);
            }
         } else {
            serializer.serialize(value, jg, provider);
         }
      } else if (defaultValues) {
         Object value = feature.getDefaultValue();

         if (value != null) {
            jg.writeFieldName(getFieldName());
            serializer.serialize(value, jg, provider);
         }
      }
   }

   @Override
   public EObject deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
      return null;
   }
}
