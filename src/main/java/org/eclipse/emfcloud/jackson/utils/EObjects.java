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
package org.eclipse.emfcloud.jackson.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl.BasicEMapEntry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emfcloud.jackson.databind.EMFContext;

import com.fasterxml.jackson.databind.DatabindContext;

/**
 * Utility class to facilitate access or modification of eObjects.
 */
public final class EObjects {

   private EObjects() {}

   /**
    * Set or add a value to an object reference. The value must be
    * an EObject.
    *
    * @param owner     EObject owning the reference
    * @param reference the reference of the value to set
    * @param value     value to set for the reference
    */
   public static void setOrAdd(final EObject owner, final EReference reference, final Object value) {
      if (value != null) {
         if (reference.isMany()) {
            @SuppressWarnings("unchecked")
            Collection<EObject> values = (Collection<EObject>) owner.eGet(reference, false);
            if (values != null && value instanceof EObject) {
               values.add((EObject) value);
            }
         } else {
            owner.eSet(reference, value);
         }
      }
   }

   /**
    * Checks that the contained object is in a different resource than it's owner, making
    * it a contained proxy.
    *
    * @param ctxt      the databind context
    * @param owner     EObject owning the contained object
    * @param contained contained object to be evaluated
    * @return true if proxy
    */
   public static boolean isContainmentProxy(final DatabindContext ctxt, final EObject owner, final EObject contained) {
      if (contained.eIsProxy()) {
         return true;
      }

      Resource ownerResource = EMFContext.getResource(ctxt, owner);
      Resource containedResource = EMFContext.getResource(ctxt, contained);

      return ownerResource != null && ownerResource != containedResource;
   }

   /**
    * Creates a map entry of type string, string.
    *
    * @param key   of entry
    * @param value of entry
    * @param type  of entry
    * @return entry
    */
   public static EObject createEntry(final String key, final Object value, final EClass type) {
      if (type == EcorePackage.Literals.ESTRING_TO_STRING_MAP_ENTRY) {

         final EObject entry = EcoreUtil.create(EcorePackage.Literals.ESTRING_TO_STRING_MAP_ENTRY);
         entry.eSet(EcorePackage.Literals.ESTRING_TO_STRING_MAP_ENTRY__KEY, key);
         entry.eSet(EcorePackage.Literals.ESTRING_TO_STRING_MAP_ENTRY__VALUE, value);

         return entry;

      }
      final BasicEMapEntry<String, Object> entry = new BasicEMapEntry<>();
      entry.eSetClass(type);
      entry.setKey(key);
      entry.setValue(value);

      return entry;
   }

   private static final Map<EClass, Optional<EStructuralFeature>> ELEMENT_WILDCARD_CACHE = Collections
      .synchronizedMap(new WeakHashMap<>());
   private static final Map<EClass, Optional<EStructuralFeature>> ATTRIBUTE_WILDCARD_CACHE = Collections
      .synchronizedMap(new WeakHashMap<>());

   /**
    * Test whether the feature is a map entry feature.
    *
    * @param feature to test
    * @return true when this is a map entry part of another feature
    */
   public static boolean isFeatureMapEntry(final EStructuralFeature feature) {
      if (feature != null) {
         EAnnotation annotation = feature.getEAnnotation(ExtendedMetaData.ANNOTATION_URI);
         if (annotation != null) {
            // a classic grouped feature map entry
            return annotation.getDetails().containsKey(ExtendedMetaData.FEATURE_KINDS[ExtendedMetaData.GROUP_FEATURE])
               ||
               // test for an element feature to be included in a ":mixed" wildcard
               ExtendedMetaData.INSTANCE.getFeatureKind(feature) == ExtendedMetaData.ELEMENT_FEATURE
                  && ELEMENT_WILDCARD_CACHE.computeIfAbsent(feature.getEContainingClass(),
                     EObjects::getElementWildcard).isPresent()
               ||
               // test for an attribute feature to be included in a ":mixed" wildcard
               ExtendedMetaData.INSTANCE.getFeatureKind(feature) == ExtendedMetaData.ATTRIBUTE_FEATURE
                  && ATTRIBUTE_WILDCARD_CACHE.computeIfAbsent(feature.getEContainingClass(),
                     EObjects::getAttributeWildcard).isPresent();
         }
      }
      return false;
   }

   /**
    * Get the group name for the general feature containing the elements.
    *
    * @param featureMapEntry the feature corresponding to an entry in a feature map
    * @return the name of the group feature corresponding to the whole map
    */
   public static String getGroupNameForFeatureMapEntry(final EStructuralFeature featureMapEntry) {
      // supplies the IllegalArgumentException for features which are not a feature map entry
      Supplier<IllegalArgumentException> illegal = () -> new IllegalArgumentException(featureMapEntry.getName());
      EAnnotation annotation = featureMapEntry.getEAnnotation(ExtendedMetaData.ANNOTATION_URI);
      if (annotation == null) {
         // this is not a feature map entry
         throw illegal.get();
      }
      String group = annotation.getDetails().get(ExtendedMetaData.FEATURE_KINDS[ExtendedMetaData.GROUP_FEATURE]);
      return Optional.ofNullable(group)
         // remove leading # (not ':' in the ":mixed" name)
         .map(g -> g.startsWith("#") ? g.substring(1) : g)
         .orElseGet(() -> {
            // no group entry, we use the ":mixed" wildcard
            EStructuralFeature wildcard;
            if (ExtendedMetaData.INSTANCE.getFeatureKind(featureMapEntry) == ExtendedMetaData.ELEMENT_FEATURE) {
               wildcard = ELEMENT_WILDCARD_CACHE.computeIfAbsent(featureMapEntry.getEContainingClass(),
                  EObjects::getElementWildcard).orElseThrow(illegal);
            } else if (ExtendedMetaData.INSTANCE
               .getFeatureKind(featureMapEntry) == ExtendedMetaData.ATTRIBUTE_FEATURE) {
               wildcard = ATTRIBUTE_WILDCARD_CACHE.computeIfAbsent(featureMapEntry.getEContainingClass(),
                  EObjects::getAttributeWildcard).orElseThrow(illegal);
            } else {
               // this is not a feature map entry
               throw illegal.get();
            }
            return wildcard.getName();
         });
   }

   /**
    * Test whether the EClass type has an element wildcard (usually ":mixed") and return it.
    *
    * @param type the EClass
    * @return an Optional with the wildcard feature when it exists
    */
   private static Optional<EStructuralFeature> getElementWildcard(final EClass type) {
      // most probably only EAttributes can be wildcards, but let's not take the risk for exotic cases...
      Stream<EStructuralFeature> featureMapAtts = type.getEAllStructuralFeatures().stream()
         .filter(FeatureMapUtil::isFeatureMap);
      return featureMapAtts
         .filter(r -> ExtendedMetaData.INSTANCE.getFeatureKind(r) == ExtendedMetaData.ELEMENT_WILDCARD_FEATURE)
         .findFirst();
   }

   /**
    * Test whether the EClass type has an attribute wildcard (usually ":mixed") and return it.
    *
    * @param type the EClass
    * @return an Optional with the wildcard feature when it exists
    */
   private static Optional<EStructuralFeature> getAttributeWildcard(final EClass type) {
      // most probably only EAttributes can be wildcards, but let's not take the risk for exotic cases...
      Stream<EStructuralFeature> featureMapAtts = type.getEAllStructuralFeatures().stream()
         .filter(FeatureMapUtil::isFeatureMap);
      return featureMapAtts
         .filter(r -> ExtendedMetaData.INSTANCE.getFeatureKind(r) == ExtendedMetaData.ATTRIBUTE_WILDCARD_FEATURE)
         .findFirst();
   }
}
