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
package org.eclipse.emfcloud.jackson.resource;

import static java.util.Collections.synchronizedMap;
import static org.eclipse.emfcloud.jackson.databind.EMFContext.Attributes.RESOURCE;
import static org.eclipse.emfcloud.jackson.databind.EMFContext.Attributes.RESOURCE_SET;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.jackson.databind.EMFContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;

/**
 * A Resource implementation that read and write it's content in JSON.
 */
public class JsonResource extends ResourceImpl {

   protected static final Map<EObject, String> DETACHED_EOBJECT_TO_ID_MAP = synchronizedMap(
      new WeakHashMap<EObject, String>());

   private ObjectMapper mapper;
   private Map<String, EObject> idToEObjectMap;
   private Map<EObject, String> eObjectToIDMap;

   public JsonResource(final URI uri, final ObjectMapper mapper) {
      super(uri);
      this.mapper = mapper;
   }

   public JsonResource(final URI uri) {
      super(uri);
   }

   public void setObjectMapper(final ObjectMapper mapper) { this.mapper = mapper; }

   @Override
   protected boolean isAttachedDetachedHelperRequired() { return useIDs() || super.isAttachedDetachedHelperRequired(); }

   protected boolean useIDs() {
      return eObjectToIDMap != null || idToEObjectMap != null || useUUIDs();
   }

   protected boolean useIDAttributes() {
      return true;
   }

   protected boolean useUUIDs() {
      return false;
   }

   public String getID(final EObject eObject) {
      if (eObjectToIDMap == null) {
         return null;
      }
      return eObjectToIDMap.get(eObject);
   }

   @Override
   protected EObject getEObjectByID(final String id) {
      if (idToEObjectMap != null) {
         EObject eObject = idToEObjectMap.get(id);

         if (eObject != null) {
            return eObject;
         }
      }
      return super.getEObjectByID(id);
   }

   @Override
   public String getURIFragment(final EObject eObject) {
      String id = getID(eObject);

      if (id != null) {
         return id;
      }
      return super.getURIFragment(eObject);
   }

   public void setID(final EObject eObject, final String id) {
      String oldID = id != null ? getEObjectToIDMap().put(eObject, id) : getEObjectToIDMap().remove(eObject);

      if (oldID != null) {
         getIDToEObjectMap().remove(oldID);
      }

      if (id != null) {
         getIDToEObjectMap().put(id, eObject);
      }
   }

   @Override
   protected void attachedHelper(final EObject eObject) {
      super.attachedHelper(eObject);

      String id = getID(eObject);

      if (id == null) {
         if (!isLoading()) {
            id = DETACHED_EOBJECT_TO_ID_MAP.remove(eObject);
            if (id == null) {
               id = EcoreUtil.generateUUID();
            }
            setID(eObject, id);
         }
      } else {
         getIDToEObjectMap().put(id, eObject);
      }
   }

   @Override
   protected void detachedHelper(final EObject eObject) {
      if (useIDs() && unloadingContents == null) {
         if (useUUIDs()) {
            DETACHED_EOBJECT_TO_ID_MAP.put(eObject, getID(eObject));
         }

         if (idToEObjectMap != null && eObjectToIDMap != null) {
            setID(eObject, null);
         }
      }

      super.detachedHelper(eObject);
   }

   public Map<String, EObject> getIDToEObjectMap() {
      if (idToEObjectMap == null) {
         idToEObjectMap = new HashMap<>();
      }
      return idToEObjectMap;
   }

   public Map<EObject, String> getEObjectToIDMap() {
      if (eObjectToIDMap == null) {
         eObjectToIDMap = new HashMap<>();
      }
      return eObjectToIDMap;
   }

   @Override
   protected void doLoad(final InputStream inputStream, Map<?, ?> options) throws IOException {
      if (options == null) {
         options = Collections.<String, Object> emptyMap();
      }

      if (inputStream instanceof URIConverter.Loadable) {

         ((URIConverter.Loadable) inputStream).loadResource(this);

      } else {

         ContextAttributes attributes = EMFContext
            .from(options)
            .withPerCallAttribute(RESOURCE_SET, getResourceSet())
            .withPerCallAttribute(RESOURCE, this);

         mapper.reader()
            .with(attributes)
            .forType(Resource.class)
            .withValueToUpdate(this)
            .readValue(inputStream);

      }
   }

   @Override
   protected void doSave(final OutputStream outputStream, Map<?, ?> options) throws IOException {
      if (options == null) {
         options = Collections.<String, Object> emptyMap();
      }

      if (outputStream instanceof URIConverter.Saveable) {

         ((URIConverter.Saveable) outputStream).saveResource(this);

      } else {

         mapper.writer()
            .with(EMFContext.from(options))
            .writeValue(outputStream, this);

      }
   }

}
