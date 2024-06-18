/********************************************************************************
 * Copyright (c) 2024 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.jackson.databind;

import static org.eclipse.emfcloud.jackson.annotations.JsonAnnotations.getElementName;
import static org.eclipse.emfcloud.jackson.module.EMFModule.Feature.OPTION_USE_FEATURE_MAP_KEY_AND_VALUE_PROPERTIES;

import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Helps with feature map entries (de)serialization by taking care of configurable options.
 */
public class FeatureMapEntryConfig {

   /** The json property used to store the entry's value. */
   public static final String VALUE_PROPERTY = "value";
   /** The json property used to store the entry's key (feature name). */
   public static final String KEY_PROPERTY = "featureName";
   /** The activated features from module. */
   private final int features;

   public FeatureMapEntryConfig(final int features) {
      this.features = features;
   }

   /**
    * Test whether we should use dedicated {@link FeatureMapEntryConfig#KEY_PROPERTY} and
    * {@link FeatureMapEntryConfig#VALUE_PROPERTY} properties for feature map entries.
    *
    * @return true when using properties, false otherwise (default)
    */
   public boolean shouldUseKeyAndValueProperties() {
      return OPTION_USE_FEATURE_MAP_KEY_AND_VALUE_PROPERTIES.enabledIn(features);
   }

   /**
    * Get the name to use as property key or value (for {@link FeatureMapEntryConfig#VALUE_PROPERTY}) for the
    * feature.
    *
    * @param feature a feature in map keys
    * @return the name to use
    */
   public String getPropertyName(final EStructuralFeature feature) {
      return getElementName(feature, features);
   }

}
