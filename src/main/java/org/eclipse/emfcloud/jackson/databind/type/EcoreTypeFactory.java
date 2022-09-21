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

package org.eclipse.emfcloud.jackson.databind.type;

import static org.eclipse.emf.ecore.EcorePackage.Literals.EBYTE_ARRAY;
import static org.eclipse.emf.ecore.EcorePackage.Literals.EJAVA_CLASS;
import static org.eclipse.emf.ecore.EcorePackage.Literals.EJAVA_OBJECT;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class EcoreTypeFactory {

   private final Map<Pair<EClass, EStructuralFeature>, JavaType> cache = Collections.synchronizedMap(new WeakHashMap<>());

   private static class Pair<A, B> {
      private final A a;
      private final B b;

      Pair(final A a, final B b) {
         this.a = a;
         this.b = b;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         }
         if (o == null || getClass() != o.getClass()) {
            return false;
         }
         Pair<?, ?> pair = (Pair<?, ?>) o;
         return Objects.equals(a, pair.a) &&
            Objects.equals(b, pair.b);
      }

      @Override
      public int hashCode() {
         return Objects.hash(a, b);
      }

      public static <A, B> Pair<A, B> of(final A a, final B b) {
         return new Pair<>(a, b);
      }
   }

   public JavaType typeOf(final DatabindContext ctxt, final EClass type, final EStructuralFeature feature) {
      Pair<EClass, EStructuralFeature> pair = Pair.of(type, feature);

      if (cache.containsKey(pair)) {
         return cache.get(pair);
      }

      EGenericType genericType = type.getFeatureType(feature);
      EClassifier realType = genericType.getERawType();

      JavaType javaType;
      if (realType != null) {
         javaType = typeOf(ctxt.getTypeFactory(), FeatureKind.get(feature), realType);
      } else {
         javaType = null;
      }

      if (javaType != null) {
         cache.put(pair, javaType);
      }

      return javaType;
   }

   private JavaType typeOf(final TypeFactory factory, final FeatureKind kind, final EClassifier type) {
      switch (kind) {
         case SINGLE_REFERENCE:
            return constructReferenceType(factory, type);
         case MANY_ATTRIBUTE:
         case MANY_CONTAINMENT:
            return constructCollectionType(factory, constructSimpleType(factory, type));
         case MANY_REFERENCE:
            return constructCollectionType(factory, constructReferenceType(factory, type));
         case MAP:
            return constructMapType(factory, (EClass) type);
         default:
            return constructSimpleType(factory, type);
      }
   }

   JavaType constructSimpleType(final TypeFactory factory, final EClassifier type) {
      return factory.constructType(rawType(type));
   }

   JavaType constructReferenceType(final TypeFactory factory, final EClassifier type) {
      Class<?> rawType = rawType(type);

      return factory.constructReferenceType(EcoreType.ReferenceType.class, factory.constructType(rawType));
   }

   JavaType constructCollectionType(final TypeFactory factory, final JavaType type) {
      return factory.constructCollectionType(Collection.class, type);
   }

   JavaType constructMapType(final TypeFactory factory, final EClass type) {
      EStructuralFeature key = type.getEStructuralFeature("key");
      EStructuralFeature value = type.getEStructuralFeature("value");

      if (key == null || value == null) {
         return null;
      }

      EClassifier keyType = key.getEType();
      EClassifier valueType = value.getEType();

      Class<?> keyClass = rawType(keyType);
      if (String.class.isAssignableFrom(keyClass)) {
         return factory.constructMapLikeType(EMap.class, keyClass, rawType(valueType));
      }
      return factory.constructCollectionType(Collection.class, factory.constructType(EObject.class));
   }

   private Class<?> rawType(final EClassifier classifier) {
      Class<?> rawType = classifier.getInstanceClass();

      if (classifier instanceof EDataType) {
         if (rawType == null || classifier == EJAVA_CLASS || classifier == EJAVA_OBJECT || classifier == EBYTE_ARRAY) {
            rawType = EcoreType.DataType.class;
         }
      } else if (rawType == null) {
         rawType = EObject.class;
      }

      return rawType;
   }

}
