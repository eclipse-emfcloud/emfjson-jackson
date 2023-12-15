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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import org.eclipse.emf.common.util.SegmentSequence;
import org.eclipse.emf.common.util.SegmentSequence.Builder;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.eclipse.emfcloud.jackson.databind.EMFContext;
import org.eclipse.emfcloud.jackson.databind.FeatureMapEntryConfig;
import org.eclipse.emfcloud.jackson.databind.ser.FeatureMapEntrySerializer;
import org.eclipse.emfcloud.jackson.utils.EObjects;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
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
   protected String getIDForEObject(final EObject eObject) {
      return Optional.ofNullable(getID(eObject)).orElseGet(() -> super.getIDForEObject(eObject));
   }

   @Override
   public String getURIFragment(final EObject eObject) {
      String id = getIDForEObject(eObject);
      if (id != null) {
         return id;
      }
      // else, check if it is a direct root element
      InternalEObject internalEObject = (InternalEObject) eObject;
      if (isContainedRoot(internalEObject)) {
         return "/" + getURIFragmentRootSegment(eObject);
      }
      // else, we must build the fragment path
      SegmentSequence.Builder builder = SegmentSequence.newBuilder("/");

      boolean supportIDRelativeURIFragmentPaths = supportIDRelativeURIFragmentPaths();

      boolean isContained = buildURIFragmentPath(builder, internalEObject, supportIDRelativeURIFragmentPaths);

      if (!isContained) {
         return "/-1";
      }

      builder.append("");
      builder.reverse();

      // Note that we convert it to a segment sequence because the most common use case is that callers of this method
      // will call URI.appendFragment.
      // By creating the segment sequence here, we ensure that it's found in the cache.
      return builder.toSegmentSequence().toString();
   }

   @Override
   protected EObject getEObject(final List<String> uriFragmentPath) {
      int size = uriFragmentPath.size();
      EObject eObject = getEObjectForURIFragmentRootSegment(size == 0 ? "" : uriFragmentPath.get(0));
      boolean skipFeatureMapEntryFragment = true;
      for (int i = 1; i < size && eObject != null; ++i) {
         // we must check the particular case for Feature Map Entries which are stored differently in json.
         if (EObjects.isFeatureMapEntry(eObject.eContainingFeature())) {
            /*
             * First time, the segment indicates FeatureMapEntrySerializer.VALUE_PROPERTY or the feature.
             * In either case, we already have the targeted value and do not need to look for another one.
             * Next times, we have to navigate into the object the classic way.
             */
            // find index of the entry containing the value
            if (skipFeatureMapEntryFragment) {
               // do nothing
               skipFeatureMapEntryFragment = false;
            } else {
               // navigate into the object
               eObject = ((InternalEObject) eObject).eObjectForURIFragmentSegment(uriFragmentPath.get(i));
               skipFeatureMapEntryFragment = true;
            }
         } else {
            eObject = ((InternalEObject) eObject).eObjectForURIFragmentSegment(uriFragmentPath.get(i));
         }

      }

      return eObject;
   }

   /**
    * Builds the URI fragment by building a path.
    *
    * @param builder                           the path builder
    * @param eObject                           the object to build path for
    * @param supportIDRelativeURIFragmentPaths true when we support path relative to an id
    * @return true when a build path have been successfully built and the object is correctly contained.
    */
   private boolean buildURIFragmentPath(final Builder builder, final InternalEObject eObject,
      final boolean supportIDRelativeURIFragmentPaths) {
      InternalEObject container = eObject.eInternalContainer();
      if (container != null) {
         // add fragment for element from container
         if (EObjects.isFeatureMapEntry(eObject.eContainingFeature())) {
            String generalFeatureName = EObjects.getGroupNameForFeatureMapEntry(eObject.eContainingFeature());
            EStructuralFeature generalFeature = eObject.eContainer().eClass().getEStructuralFeature(generalFeatureName);
            // find index of the entry containing the value
            int index = findEntryIndex(eObject, generalFeature);
            // construct the fragment
            StringBuilder fragment = new StringBuilder();
            fragment.append('@');
            fragment.append(generalFeatureName);
            fragment.append('.');
            fragment.append(index);
            fragment.append('/');
            if (useKeyAndValueForFeatureMapEntry()) {
               fragment.append(FeatureMapEntryConfig.VALUE_PROPERTY);
            } else {
               // use specialized feature name
               fragment.append(eObject.eContainingFeature().getName());
            }
            builder.append(fragment.toString());
         } else {
            builder.append(container.eURIFragmentSegment(eObject.eContainingFeature(), eObject));
         }

         if (supportIDRelativeURIFragmentPaths) {
            String id = getIDForEObject(container);
            if (id != null) {
               // end path with id of container
               builder.append("?" + id);
               // still look for a root container to make sure it is contained
               EObject root = EcoreUtil.getRootContainer(eObject);
               return isContainedRoot((InternalEObject) root);
            }
         }
         // continue building path from container
         return buildURIFragmentPath(builder, container, supportIDRelativeURIFragmentPaths);
      }
      // else, this is the root
      if (isContainedRoot(eObject)) {
         builder.append(getURIFragmentRootSegment(eObject));
         return true;
      }
      // else, not contained in a the resource
      return false;
   }

   /**
    * Test whether we should use key and value dedicated properties for feature map entries.
    *
    * @return true when using dedicated properties, false when using the feature name (default)
    */
   private boolean useKeyAndValueForFeatureMapEntry() {
      // try and ask the FeatureMap.Entry serializer whether we should use key and value properties
      try {
         JsonSerializer<?> ser = this.mapper.getSerializerProviderInstance()
            .findValueSerializer(FeatureMap.Entry.class);
         if (ser instanceof FeatureMapEntrySerializer) {
            return ((FeatureMapEntrySerializer) ser).shouldUseKeyAndValueProperties();
         }
      } catch (JsonMappingException e) {
         EcorePlugin.INSTANCE.log(e);
      }
      return false;

   }

   /**
    * Find the index of the entry containing the value.
    *
    * @param eObject           the value EObject
    * @param generalFeatureMap the general feature with the Map
    * @return the index or -1 when not found
    */
   private int findEntryIndex(final InternalEObject eObject, final EStructuralFeature generalFeatureMap) {
      // value not found
      int index = -1;
      if (generalFeatureMap.isMany()) {
         Object map = eObject.eContainer().eGet(generalFeatureMap);
         if (map instanceof FeatureMap) {
            Iterator<Entry> it = ((FeatureMap) map).iterator();
            boolean found = false;
            int itIndex = 0;
            while (it.hasNext() && !found) {
               Entry entry = it.next();
               found = eObject.eContainingFeature().equals(entry.getEStructuralFeature())
                  && eObject.equals(entry.getValue());
               if (found) {
                  index = itIndex;
               }
               itIndex++;
            }
         }
      } else {
         // single value, index is 0
         index = 0;
      }
      return index;
   }

   /**
    * Check that the EObject is a root contained in this very resource.
    *
    * @param eObject EObject to check
    * @return true when root correctly contained
    */
   private boolean isContainedRoot(final InternalEObject eObject) {
      return eObject.eDirectResource() == this || unloadingContents != null && unloadingContents.contains(eObject);
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
