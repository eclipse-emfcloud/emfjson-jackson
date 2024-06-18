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

import java.util.function.Consumer;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap.Entry;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emfcloud.jackson.databind.EMFContext;
import org.eclipse.emfcloud.jackson.handlers.URIHandler;
import org.eclipse.emfcloud.jackson.utils.EObjects;

import com.fasterxml.jackson.databind.DatabindContext;

public interface ReferenceEntry {

   void resolve(DatabindContext context, URIHandler handler);

   class Base implements ReferenceEntry {

      protected final EObject owner;
      protected final EReference reference;
      protected final String id;
      private final String type;

      public Base(final EObject owner, final EReference reference, final String id) {
         this(owner, reference, id, null);
      }

      public Base(final EObject owner, final EReference reference, final String id, final String type) {
         this.owner = owner;
         this.reference = reference;
         this.id = id;
         this.type = type;
      }

      @Override
      @SuppressWarnings("checkstyle:cyclomaticComplexity")
      public void resolve(final DatabindContext context, final URIHandler handler) {
         if (id == null) {
            return;
         }

         ReferenceEntries entries = EMFContext.getEntries(context);
         ResourceSet resourceSet = EMFContext.getResourceSet(context);
         EObject target = entries.get(id);

         if (target == null) {
            Resource resource = EMFContext.getResource(context, owner);
            target = resource.getEObject(id);

            if (target == null) {

               URI baseURI = resource.getURI().trimFragment();
               URI uri = handler.resolve(baseURI, URI.createURI(id));

               if (reference.isResolveProxies() && type != null) {
                  target = createProxy(resourceSet, uri);
               } else {
                  target = resourceSet.getEObject(uri, true);
               }
            }

            if (target != null) {
               entries.store(id, target);
            }
         }

         if (target != null) {
            EObjects.setOrAdd(owner, reference, target);
         }
      }

      @SuppressWarnings("checkstyle:illegalCatch")
      protected EObject createProxy(final ResourceSet resourceSet, final URI uri) {
         EClass eClass;
         try {
            eClass = (EClass) resourceSet.getEObject(URI.createURI(type), true);
         } catch (Exception e) {
            return null;
         }

         if (eClass == null) {
            return null;
         }

         EObject object = EcoreUtil.create(eClass);
         if (object instanceof InternalEObject) {
            ((InternalEObject) object).eSetProxyURI(uri);
         }

         return object;
      }

      @Override
      @SuppressWarnings("checkstyle:cyclomaticComplexity")
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         }
         if (o == null || getClass() != o.getClass()) {
            return false;
         }

         Base that = (Base) o;

         if (!owner.equals(that.owner)) {
            return false;
         }
         if (!reference.equals(that.reference)) {
            return false;
         }
         if (!id.equals(that.id)) {
            return false;
         }
         return type != null ? type.equals(that.type) : that.type == null;

      }

      @Override
      public int hashCode() {
         int result = owner.hashCode();
         result = 31 * result + reference.hashCode();
         result = 31 * result + id.hashCode();
         result = 31 * result + (type != null ? type.hashCode() : 0);
         return result;
      }
   }

   /**
    * ReferenceEntry to create for usage in FeatureMapEntries.
    * This allows us to create the required entry and resolve it later.
    */
   class ForMapEntry extends Base {

      private EObject proxyValue;

      public ForMapEntry(final EObject owner, final EReference reference, final String id, final String type) {
         super(owner, reference, id, type);
      }

      @Override
      public void resolve(final DatabindContext context, final URIHandler handler) {
         if (proxyValue == null) {
            // proxy and entry were never created nor used, just rely on basic implementation
            super.resolve(context, handler);
            return;
         }
         /*
          * Proxy reference has been built with an incorrect URI which is actually just the fragment id.
          * Reference must be resolved correctly now.
          */
         Consumer<EObject> replaceProxy = target -> {
            if (target != proxyValue) {
               // replace proxy with target
               EcoreUtil.replace(owner, reference, proxyValue, target);
            }
         };

         ReferenceEntries entries = EMFContext.getEntries(context);
         ResourceSet resourceSet = EMFContext.getResourceSet(context);
         EObject target = entries.get(id);
         if (target == null) {
            Resource resource = EMFContext.getResource(context, owner);
            target = resource.getEObject(id);
            if (target == null) {
               URI baseURI = resource.getURI().trimFragment();
               URI uri = handler.resolve(baseURI, URI.createURI(id));
               // update proxy to target uri
               if (proxyValue instanceof InternalEObject) {
                  ((InternalEObject) proxyValue).eSetProxyURI(uri);
               }
               if (!reference.isResolveProxies()) {
                  // resolve the proxy
                  target = EcoreUtil.resolve(proxyValue, resourceSet);
                  replaceProxy.accept(target);
               }
            } else {
               // replace proxy with target
               replaceProxy.accept(target);
            }
            entries.store(id, target);
         } else {
            // replace proxy with target already resolved from previous occurrence
            replaceProxy.accept(target);
         }
      }

      /**
       * Create the feature map entry which will be resolved to the correct value later.
       *
       * @param context databind (usually deserialization) context
       * @return the created feature map entry holding a temporary proxy value
       */
      public Entry createFeatureMapEntry(final DatabindContext context) {
         ResourceSet resourceSet = EMFContext.getResourceSet(context);
         proxyValue = createProxy(resourceSet, URI.createURI(id));
         return FeatureMapUtil.createEntry(reference, proxyValue);
      }

   }
}
