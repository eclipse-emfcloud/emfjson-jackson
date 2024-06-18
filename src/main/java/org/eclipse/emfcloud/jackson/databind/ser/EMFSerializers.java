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
package org.eclipse.emfcloud.jackson.databind.ser;

import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EEnumLiteralImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emfcloud.jackson.databind.deser.ReferenceEntry;
import org.eclipse.emfcloud.jackson.databind.property.EObjectPropertyMap;
import org.eclipse.emfcloud.jackson.databind.type.EcoreType;
import org.eclipse.emfcloud.jackson.module.EMFModule;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.ser.std.CollectionSerializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;

public class EMFSerializers extends Serializers.Base {

   private final EObjectPropertyMap.Builder propertiesBuilder;
   private final JsonSerializer<FeatureMap.Entry> featureMapEntrySerializer;
   private final JsonSerializer<EObject> referenceSerializer;
   private final JsonSerializer<Resource> resourceSerializer = new ResourceSerializer();
   private final JsonSerializer<?> dataTypeSerializer = new EDataTypeSerializer();
   private final JsonSerializer<Object> mapKeySerializer = new EMapKeySerializer();
   private final JsonSerializer<Object> mapValueSerializer = new EMapValueSerializer();
   private final JsonSerializer<?> enumeratorSerializer = new EnumeratorSerializer();

   public EMFSerializers(final EMFModule module) {
      this.propertiesBuilder = EObjectPropertyMap.Builder.from(module, module.getFeatures());
      this.featureMapEntrySerializer = module.getFeatureMapEntrySerializer();
      this.referenceSerializer = module.getReferenceSerializer();
   }

   @Override
   public JsonSerializer<?> findMapLikeSerializer(final SerializationConfig config, final MapLikeType type,
      final BeanDescription beanDesc, final JsonSerializer<Object> keySerializer,
      final TypeSerializer elementTypeSerializer,
      final JsonSerializer<Object> elementValueSerializer) {
      if (type.isTypeOrSubTypeOf(EMap.class)) {
         // make a MapSerializer for configurability
         JsonSerializer<Object> keySer = Optional.ofNullable(keySerializer).orElse(mapKeySerializer);
         JsonSerializer<Object> valueSer = Optional.ofNullable(elementValueSerializer).orElse(mapValueSerializer);
         MapSerializer mapSer = MapSerializer.construct(Set.of(), type, false, elementTypeSerializer, keySer, valueSer,
            null);
         // and use a wrapping EMapSerializer for edge cases
         return new EMapSerializer(mapSer);
      }

      return super.findMapLikeSerializer(config, type, beanDesc, keySerializer, elementTypeSerializer,
         elementValueSerializer);
   }

   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Override
   public JsonSerializer<?> findCollectionSerializer(final SerializationConfig config, final CollectionType type,
      final BeanDescription beanDesc, final TypeSerializer elementTypeSerializer,
      final JsonSerializer<Object> elementValueSerializer) {
      if (type.getContentType().isReferenceType()) {
         return new CollectionSerializer(type.getContentType(), false, null, (JsonSerializer) referenceSerializer);
      }
      return super.findCollectionSerializer(config, type, beanDesc, elementTypeSerializer, elementValueSerializer);
   }

   @Override
   @SuppressWarnings("checkstyle:cyclomaticComplexity")
   public JsonSerializer<?> findSerializer(final SerializationConfig config, final JavaType type,
      final BeanDescription beanDesc) {
      if (type.isTypeOrSubTypeOf(Resource.class)) {
         return resourceSerializer;
      }

      if (type.isTypeOrSubTypeOf(Enumerator.class) && !type.isReferenceType()) {
         if (type.getRawClass() != EEnumLiteralImpl.class) {
            return enumeratorSerializer;
         }
      }

      if (type.isReferenceType() || type.isTypeOrSubTypeOf(ReferenceEntry.class)) {
         return referenceSerializer;
      }

      if (type.isTypeOrSubTypeOf(EcoreType.DataType.class)) {
         return dataTypeSerializer;
      }

      if (type.isTypeOrSubTypeOf(EObject.class)) {
         return new EObjectSerializer(propertiesBuilder, referenceSerializer);
      }

      if (type.isTypeOrSubTypeOf(FeatureMap.Entry.class)) {
         return featureMapEntrySerializer;
      }

      return super.findSerializer(config, type, beanDesc);
   }

}
