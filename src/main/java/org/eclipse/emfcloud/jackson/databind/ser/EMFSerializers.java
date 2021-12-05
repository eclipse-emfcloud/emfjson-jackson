/*
 * Copyright (c) 2019 Guillaume Hillairet and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 */
package org.eclipse.emfcloud.jackson.databind.ser;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EEnumLiteralImpl;
import org.eclipse.emf.ecore.resource.Resource;
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
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;

public class EMFSerializers extends Serializers.Base {

   private final EObjectPropertyMap.Builder propertiesBuilder;
   private final JsonSerializer<EObject> _referenceSerializer;
   private final JsonSerializer<Resource> _resourceSerializer = new ResourceSerializer();
   private final JsonSerializer<?> _dataTypeSerializer = new EDataTypeSerializer();
   private final JsonSerializer<?> _mapSerializer = new EMapStringSerializer();
   private final JsonSerializer<?> _enumeratorSerializer = new EnumeratorSerializer();

   public EMFSerializers(final EMFModule module) {
      this.propertiesBuilder = EObjectPropertyMap.Builder.from(module, module.getFeatures());
      this._referenceSerializer = module.getReferenceSerializer();
   }

   @Override
   public JsonSerializer<?> findMapLikeSerializer(final SerializationConfig config, final MapLikeType type,
      final BeanDescription beanDesc, final JsonSerializer<Object> keySerializer,
      final TypeSerializer elementTypeSerializer,
      final JsonSerializer<Object> elementValueSerializer) {
      if (type.isTypeOrSubTypeOf(EMap.class)) {
         if (type.getKeyType().isTypeOrSubTypeOf(String.class)) {
            return _mapSerializer;
         }
      }

      return super.findMapLikeSerializer(config, type, beanDesc, keySerializer, elementTypeSerializer,
         elementValueSerializer);
   }

   @Override
   public JsonSerializer<?> findCollectionSerializer(final SerializationConfig config, final CollectionType type,
      final BeanDescription beanDesc, final TypeSerializer elementTypeSerializer,
      final JsonSerializer<Object> elementValueSerializer) {
      if (type.getContentType().isReferenceType()) {
         return new CollectionSerializer(type.getContentType(), false, null, (JsonSerializer) _referenceSerializer);
      }
      return super.findCollectionSerializer(config, type, beanDesc, elementTypeSerializer, elementValueSerializer);
   }

   @Override
   public JsonSerializer<?> findSerializer(final SerializationConfig config, final JavaType type,
      final BeanDescription beanDesc) {
      if (type.isTypeOrSubTypeOf(Resource.class)) {
         return _resourceSerializer;
      }

      if (type.isTypeOrSubTypeOf(Enumerator.class) && !type.isReferenceType()) {
         if (type.getRawClass() != EEnumLiteralImpl.class) {
            return _enumeratorSerializer;
         }
      }

      if (type.isReferenceType() || type.isTypeOrSubTypeOf(ReferenceEntry.class)) {
         return _referenceSerializer;
      }

      if (type.isTypeOrSubTypeOf(EcoreType.DataType.class)) {
         return _dataTypeSerializer;
      }

      if (type.isTypeOrSubTypeOf(EObject.class)) {
         return new EObjectSerializer(propertiesBuilder, _referenceSerializer);
      }

      return super.findSerializer(config, type, beanDesc);
   }

}
