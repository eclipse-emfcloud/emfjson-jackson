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
package org.eclipse.emfcloud.jackson.annotations;

import static org.eclipse.emfcloud.jackson.annotations.EcoreTypeInfo.USE.CLASS;
import static org.eclipse.emfcloud.jackson.annotations.EcoreTypeInfo.USE.NAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emfcloud.jackson.databind.EMFContext;
import org.eclipse.emfcloud.jackson.utils.ValueReader;
import org.eclipse.emfcloud.jackson.utils.ValueWriter;

public final class JsonAnnotations {

   private static final String EXTENDED_METADATA = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";

   private JsonAnnotations() {

   }

   /**
    * Returns the name that should be use to serialize the property.
    *
    * @param element any element
    * @return name of property
    */
   public static String getElementName(final ENamedElement element) {
      String value = getValue(element, "JsonProperty", "value");
      if (value == null) {
         value = getValue(element, EXTENDED_METADATA, "name");
      }

      return value == null ? element.getName() : value;
   }

   /**
    * Returns the configured alias name or null, if there is none.
    *
    * @param element any element
    * @return the property's alias
    */
   public static List<String> getAliases(final ENamedElement element) {
      return getValues(element, "JsonAlias", "value");
   }

   /**
    * Returns true if the feature should not be serialize.
    *
    * @param feature any feature
    * @return true if should not be serialize
    */
   public static boolean shouldIgnore(final EStructuralFeature feature) {
      return feature.getEAnnotation("JsonIgnore") != null;
   }

   /**
    * Returns the property that should be use to store the type information of the classifier.
    *
    * @param classifier any classifier
    * @return the type information property
    */
   public static EcoreTypeInfo getTypeProperty(final EClassifier classifier) {
      return getTypeProperty(classifier, null, null);
   }


   /**
    * Returns the property that should be use to store the type information of the classifier.
    *
    * @param classifier  any classifier
    * @param valueReader the reader to use for deserializing type info
    * @param valueWriter the reader to use for serializing type info
    * @return the type information property
    */
   @SuppressWarnings("checkstyle:cyclomaticComplexity")
   public static EcoreTypeInfo getTypeProperty(final EClassifier classifier,
                                               ValueReader<String, EClass> valueReader,
                                               ValueWriter<EClass, String> valueWriter) {
      String property = getValue(classifier, "JsonType", "property");
      String use = getValue(classifier, "JsonType", "use");

      if (use != null) {
         EcoreTypeInfo.USE useType = EcoreTypeInfo.USE.valueOf(use.toUpperCase());

         if (useType == NAME) {
            valueReader = (value, context) -> {
               EClass type = value != null && value.equalsIgnoreCase(classifier.getName()) ? (EClass) classifier : null;
               if (type == null) {
                  type = EMFContext.findEClassByName(value, classifier.getEPackage());
               }
               return type;
            };
            valueWriter = (value, context) -> value.getName();
         } else if (useType == CLASS) {
            valueReader = (value, context) -> {
               EClass type = value != null && value.equalsIgnoreCase(classifier.getInstanceClassName())
                  ? (EClass) classifier
                  : null;
               if (type == null) {
                  type = EMFContext.findEClassByQualifiedName(value, classifier.getEPackage());
               }
               return type;
            };
            valueWriter = (value, context) -> value.getInstanceClassName();
         }
      }

      return property != null ? new EcoreTypeInfo(property, valueReader, valueWriter) : null;
   }

   /**
    * Returns true if the classifier type information should not be serialize.
    * This is true when the classifier possesses an annotation @JsonType with include = "false".
    *
    * @param classifier any classifier
    * @return true if type info should not be serialize
    */
   public static boolean shouldIgnoreType(final EClassifier classifier) {
      EAnnotation annotation = classifier.getEAnnotation("JsonType");

      return annotation != null && "false".equalsIgnoreCase(annotation.getDetails().get("include"));
   }

   /**
    * Returns the property that should be use to serialize the identity of the object.
    *
    * @param classifier any classifier
    * @return the identity property
    */
   public static String getIdentityProperty(final EClassifier classifier) {
      return getValue(classifier, "JsonIdentity", "property");
   }

   /**
    * Returns {@code true}, if the feature is annotated to be treated as raw JSON.
    *
    * @param feature any feature
    * @return {@code true}, if raw (de)serialization should be done for this feature
    */
   public static boolean isRawValue(final EStructuralFeature feature) {
      return Boolean.parseBoolean(getValue(feature, "JsonRawValue", "value"))
            && feature instanceof EAttribute
            && String.class.getName().equals(feature.getEType().getInstanceClassName());
   }

   protected static String getValue(final ENamedElement element, final String annotation, final String property) {
      EAnnotation ann = element.getEAnnotation(annotation);

      if (ann != null && ann.getDetails().containsKey(property)) {
         return ann.getDetails().get(property);
      }
      return null;
   }

   protected static List<String> getValues(final ENamedElement element, final String annotation,
      final String property) {
      String value = getValue(element, annotation, property);

      if (value == null) {
         return Collections.emptyList();
      }

      if (value.contains(",")) {
         String[] split = value.split(",");
         List<String> values = new ArrayList<>();
         for (String s : split) {
            String v = s.trim();
            if (!v.isEmpty()) {
               values.add(v);
            }
         }
         return values;
      }
      return Collections.singletonList(value);
   }
}
