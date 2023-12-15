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
package org.eclipse.emfcloud.jackson.databind.deser;

import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.eclipse.emfcloud.jackson.databind.property.EObjectPropertyMap;
import org.eclipse.emfcloud.jackson.databind.type.EcoreType;
import org.eclipse.emfcloud.jackson.module.EMFModule;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.ReferenceType;

public class EMFDeserializers extends Deserializers.Base {

   private final ResourceDeserializer resourceDeserializer;
   private final JsonDeserializer<EList<Map.Entry<?, ?>>> mapDeserializer;
   private final JsonDeserializer<Object> dataTypeDeserializer;
   private final JsonDeserializer<ReferenceEntry> referenceDeserializer;
   private final JsonDeserializer<Entry> featureMapEntryDeserializer;
   private final EObjectPropertyMap.Builder builder;

   public EMFDeserializers(final EMFModule module) {
      this.builder = new EObjectPropertyMap.Builder(
         module.getIdentityInfo(),
         module.getTypeInfo(),
         module.getReferenceInfo(),
         module.getFeatures());
      this.resourceDeserializer = new ResourceDeserializer(module.getUriHandler());
      this.referenceDeserializer = module.getReferenceDeserializer();
      this.featureMapEntryDeserializer = module.getFeatureMapEntryDeserializer();
      this.mapDeserializer = new EMapDeserializer();
      this.dataTypeDeserializer = new EDataTypeDeserializer();
   }

   @Override
   public JsonDeserializer<?> findMapLikeDeserializer(final MapLikeType type,
      final DeserializationConfig config,
      final BeanDescription beanDesc,
      final KeyDeserializer keyDeserializer,
      final TypeDeserializer elementTypeDeserializer,
      final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
      if (type.isTypeOrSubTypeOf(EMap.class)) {
         return mapDeserializer;
      }

      return super.findMapLikeDeserializer(type, config, beanDesc, keyDeserializer, elementTypeDeserializer,
         elementDeserializer);
   }

   @Override
   public JsonDeserializer<?> findEnumDeserializer(final Class<?> type, final DeserializationConfig config,
      final BeanDescription beanDesc) throws JsonMappingException {
      if (Enumerator.class.isAssignableFrom(type)) {
         return dataTypeDeserializer;
      }

      return super.findEnumDeserializer(type, config, beanDesc);
   }

   @Override
   public JsonDeserializer<?> findCollectionDeserializer(final CollectionType type,
      final DeserializationConfig config,
      final BeanDescription beanDesc,
      final TypeDeserializer elementTypeDeserializer,
      final JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
      if (type.getContentType().isTypeOrSubTypeOf(EObject.class)) {
         return new CollectionDeserializer(type, new EObjectDeserializer(builder, type.getContentType().getRawClass()),
            referenceDeserializer);
      }
      return super.findCollectionDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
   }

   @Override
   public JsonDeserializer<?> findReferenceDeserializer(final ReferenceType refType,
      final DeserializationConfig config,
      final BeanDescription beanDesc,
      final TypeDeserializer contentTypeDeserializer,
      final JsonDeserializer<?> contentDeserializer) throws JsonMappingException {
      if (referenceDeserializer != null) {
         return referenceDeserializer;
      }
      return super.findReferenceDeserializer(refType, config, beanDesc, contentTypeDeserializer, contentDeserializer);
   }

   @Override
   public JsonDeserializer<?> findBeanDeserializer(final JavaType type,
      final DeserializationConfig config,
      final BeanDescription beanDesc) throws JsonMappingException {
      if (type.isTypeOrSubTypeOf(Resource.class)) {
         return resourceDeserializer;
      }

      if (type.isReferenceType()) {
         return referenceDeserializer;
      }

      if (type.isTypeOrSubTypeOf(EcoreType.DataType.class)) {
         return dataTypeDeserializer;
      }

      if (type.isTypeOrSubTypeOf(EObject.class)) {
         return new EObjectDeserializer(builder, type.getRawClass());
      }

      if (type.isTypeOrSubTypeOf(FeatureMap.Entry.class)) {
         return featureMapEntryDeserializer;
      }

      return super.findBeanDeserializer(type, config, beanDesc);
   }
}
