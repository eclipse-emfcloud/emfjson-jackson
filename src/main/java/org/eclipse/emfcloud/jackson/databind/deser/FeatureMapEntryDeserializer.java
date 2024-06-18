/********************************************************************************
 * Copyright (c) 2023 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.jackson.databind.deser;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.eclipse.emfcloud.jackson.databind.EMFContext.getResource;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emfcloud.jackson.databind.EMFContext;
import org.eclipse.emfcloud.jackson.databind.FeatureMapEntryConfig;
import org.eclipse.emfcloud.jackson.databind.type.EcoreTypeFactory;
import org.eclipse.emfcloud.jackson.errors.JSONException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class FeatureMapEntryDeserializer extends JsonDeserializer<FeatureMap.Entry> {

   private final FeatureMapEntryConfig config;

   public FeatureMapEntryDeserializer(final FeatureMapEntryConfig config) {
      this.config = config;
   }

   /**
    * Content read during tokens deserialization.
    */
   private class DeserializedContent {

      private String featureName;
      private EStructuralFeature feature;
      private Object value;

      public String getFeatureName() { return featureName; }

      public void setValue(final Object value) { this.value = value; }

      public Object getValue() { return value; }

      public EStructuralFeature getFeature() { return feature; }

      public void setFeature(final String featureName, final EObject parent) {
         this.featureName = featureName;
         feature = doGetFeature(parent, featureName);
      }

      public boolean isFeatureSet() { return feature != null; }

      /**
       * Get the feature from the feature name.
       *
       * @param parent      the EObject parent
       * @param featureName the feature name as found in json
       * @return the feature
       */
      private EStructuralFeature doGetFeature(final EObject parent, final String featureName) {
         if (featureName != null) {
            Optional<EStructuralFeature> matchingFeature = parent.eClass().getEAllStructuralFeatures().stream().filter(
               ref -> featureName.equals(config.getPropertyName(ref))).findFirst();
            // eventually, be lenient if feature name was used instead
            EStructuralFeature feature = matchingFeature
               .orElseGet(() -> parent.eClass().getEStructuralFeature(featureName));
            return feature;
         }
         return null;
      }

   }

   @Override
   public Entry deserialize(final JsonParser jp, final DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
      // get parent now, before children values reading erase it.
      EObject parent = EMFContext.getParent(ctxt);
      if (jp.getCurrentToken() != JsonToken.START_OBJECT) {
         return null;
      }
      DeserializedContent content = new DeserializedContent();
      try (TokenBuffer buffer = new TokenBuffer(jp);) {
         while (jp.nextToken() != JsonToken.END_OBJECT) {
            readNextToken(jp, ctxt, parent, content, buffer);
         }
         // read value now when it was before the feature
         if (!buffer.isEmpty() && content.isFeatureSet() && content.getValue() == null) {
            Object value = readValue(buffer.asParser(), ctxt, parent, content.getFeature());
            content.setValue(value);
         }
      }
      // create the resulting entry
      Entry result = createResult(ctxt, parent, content.getFeature(), content.getValue());
      if (result == null) {
         handleUnknownProperty(parent, content.getFeatureName(), jp, getResource(ctxt), ctxt);
      }
      return result;
   }

   public void readNextToken(final JsonParser jp, final DeserializationContext ctxt, EObject parent,
      DeserializedContent content, TokenBuffer buffer) throws IOException {
      String key = jp.getCurrentName();
      jp.nextToken();

      boolean readingValue = false;
      if (config.shouldUseKeyAndValueProperties()) {
         // look for 'featureName' and 'value' entries
         if (FeatureMapEntryConfig.KEY_PROPERTY.equals(key)) {
            content.setFeature(jp.getValueAsString(), parent);
         } else if (FeatureMapEntryConfig.VALUE_PROPERTY.equals(key)) {
            readingValue = true;
         }
      } else {
         // key is the feature name
         content.setFeature(key, parent);
         readingValue = true;
      }

      if (readingValue) {
         if (content.isFeatureSet()) {
            // proceed with value reading
            Object value = readValue(jp, ctxt, parent, content.getFeature());
            content.setValue(value);
         } else {
            // we do not have the feature yet, store in buffer so we can read value with the type information
            buffer.copyCurrentStructure(jp);
         }
      }
   }

   private void handleUnknownProperty(final EObject parent, final String featureName, final JsonParser jp,
      final Resource resource, final DeserializationContext ctxt)
      throws IOException {
      if (resource != null && ctxt.getConfig().hasDeserializationFeatures(FAIL_ON_UNKNOWN_PROPERTIES.getMask())) {
         resource.getErrors()
            .add(new JSONException(
               String.format("Unknown feature '%s' for %s", featureName, EcoreUtil.getURI(parent.eClass())),
               jp.getCurrentLocation()));
      }
   }

   /**
    * Read and deserialize the value in the map entry.
    *
    * @param jp      json parser
    * @param ctxt    the deserialization context
    * @param parent  the parent EObject
    * @param feature the feature holding the value
    * @return the entry's read value
    * @throws IOException read exception
    */
   public Object readValue(final JsonParser jp, final DeserializationContext ctxt, final EObject parent,
      final EStructuralFeature feature)
      throws IOException {
      Object value;
      EcoreTypeFactory factory = EMFContext.getTypeFactory(ctxt);
      JavaType type = factory.typeOf(ctxt, parent.eClass(), feature);
      if (feature.isMany() && type instanceof CollectionType) {
         type = type.getContentType();
      }
      if (jp.getCurrentToken() == JsonToken.START_OBJECT) {
         // this may be an EObject definition or a reference...
         EMFContext.setFeature(ctxt, feature);
         value = ctxt.readValue(jp, type);
      } else {
         value = ctxt.readValue(jp, type);
      }
      return value;
   }

   /**
    * Create result for deserialization.
    *
    * @param ctxt    the deserialization context
    * @param parent  the parent EObject
    * @param feature the feature name
    * @param value   the entry's read value
    * @return the result map entry
    */
   public Entry createResult(final DeserializationContext ctxt, final EObject parent, final EStructuralFeature feature,
      final Object value) {
      if (feature != null) {
         if (value instanceof ReferenceEntry.ForMapEntry) {
            // make sure entry is resolved later
            ReferenceEntries entries = EMFContext.getEntries(ctxt);
            entries.entries().add((ReferenceEntry) value);
            return ((ReferenceEntry.ForMapEntry) value).createFeatureMapEntry(ctxt);
         }
         Entry entry = FeatureMapUtil.createEntry(feature, value);
         return entry;
      }
      return null;
   }

}
