/*******************************************************************************
 * Copyright (c) 2023 Bonitasoft and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 *******************************************************************************/
package org.eclipse.emfcloud.jackson.databind.ser;

import java.io.IOException;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emfcloud.jackson.databind.FeatureMapEntryConfig;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * A serializer for {@link org.eclipse.emf.ecore.util.FeatureMap.Entry}.
 * We need FeatureMap to be displayed as a sequence of entries in order to keep the relative order among different
 * features.
 * This serializer takes care of a single entry.
 *
 * @author vhemery
 */
public class FeatureMapEntrySerializer extends JsonSerializer<FeatureMap.Entry> {
   private JsonSerializer<EObject> referenceSerializer;
   private final FeatureMapEntryConfig config;

   public FeatureMapEntrySerializer(final JsonSerializer<EObject> referenceSerializer,
      final FeatureMapEntryConfig config) {
      this.referenceSerializer = referenceSerializer;
      this.config = config;
   }

   public void setReferenceSerializer(final JsonSerializer<EObject> referenceSerializer) {
      this.referenceSerializer = referenceSerializer;
   }

   @Override
   public Class<FeatureMap.Entry> handledType() {
      return FeatureMap.Entry.class;
   }

   @Override
   public void serialize(final FeatureMap.Entry entry, final JsonGenerator jg, final SerializerProvider serializers)
      throws IOException {
      jg.writeStartObject();
      EStructuralFeature feat = entry.getEStructuralFeature();
      String featureName = config.getPropertyName(feat);
      if (shouldUseKeyAndValueProperties()) {
         // write the feature name with a dedicated property
         jg.writeStringField(FeatureMapEntryConfig.KEY_PROPERTY, featureName);
         // write the value
         if (feat instanceof EReference && !((EReference) feat).isContainment()) {
            jg.writeFieldName(FeatureMapEntryConfig.VALUE_PROPERTY);

            referenceSerializer.serialize((EObject) entry.getValue(), jg, serializers);
         } else {
            jg.writeObjectField(FeatureMapEntryConfig.VALUE_PROPERTY, entry.getValue());
         }
      } else {
         // write value with the feature name as key
         if (feat instanceof EReference && !((EReference) feat).isContainment()) {
            jg.writeFieldName(featureName);

            referenceSerializer.serialize((EObject) entry.getValue(), jg, serializers);
         } else {
            jg.writeObjectField(featureName, entry.getValue());
         }

      }
      jg.writeEndObject();
   }

   /**
    * Test whether we should use dedicated {@link FeatureMapEntryConfig#KEY_PROPERTY} and
    * {@link FeatureMapEntryConfig#VALUE_PROPERTY} properties for feature map
    * entries.
    *
    * @return true when using properties, false otherwise (default)
    */
   public boolean shouldUseKeyAndValueProperties() {
      return config.shouldUseKeyAndValueProperties();
   }

}
