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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl.BasicEMapEntry;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
    * @param owner
    * @param reference
    * @param value
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
    * @param owner
    * @param contained
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

}
