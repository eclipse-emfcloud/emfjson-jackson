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
package org.eclipse.emfcloud.jackson.databind;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.jackson.databind.deser.ReferenceEntries;
import org.eclipse.emfcloud.jackson.databind.type.EcoreTypeFactory;
import org.eclipse.emfcloud.jackson.handlers.URIHandler;

import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;

public final class EMFContext {

   private EMFContext() {

   }

   public enum Attributes {
      // public attributes
      RESOURCE_SET,
      RESOURCE_URI,
      RESOURCE,
      ROOT_ELEMENT
   }

   enum Internals {
      // internal attributes
      INIT,
      TYPE_FACTORY,
      REFERENCE_ENTRIES,
      CURRENT_DATATYPE,
      CURRENT_FEATURE,
      CURRENT_PARENT,
      MAP_OF_OBJECTS,
      MAP_OF_URIS,
      MAP_OF_RESOURCES,
      MAP_OF_SUB_TYPES,
      ALL_TYPES
   }

   public static void init(final Resource resource, final DatabindContext context) {
      if (context.getAttribute(Internals.INIT) != null) {
         return;
      }

      ReferenceEntries entries = new ReferenceEntries();
      EcoreTypeFactory ecoreType = new EcoreTypeFactory();
      ResourceSet resourceSet = resource.getResourceSet();

      context.setAttribute(Attributes.RESOURCE, resource);
      context.setAttribute(Attributes.RESOURCE_SET, resourceSet);
      context.setAttribute(Internals.REFERENCE_ENTRIES, entries);
      context.setAttribute(Internals.TYPE_FACTORY, ecoreType);

      context.setAttribute(Internals.INIT, true);
   }

   public static void prepare(final DatabindContext ctxt) {
      if (ctxt.getAttribute(Internals.INIT) != null) {
         return;
      }

      if (getResourceSet(ctxt) == null) {
         ctxt.setAttribute(Attributes.RESOURCE_SET, new ResourceSetImpl());
      }
      if (getEntries(ctxt) == null) {
         ctxt.setAttribute(Internals.REFERENCE_ENTRIES, new ReferenceEntries());
      }
   }

   public static ContextAttributes from(final Map<?, ?> options) {
      return ContextAttributes
         .getEmpty()
         .withSharedAttributes(options == null ? new HashMap<>() : new HashMap<>(options));
   }

   public static void resolve(final DeserializationContext ctxt, final URIHandler handler) {
      ReferenceEntries entries = getEntries(ctxt);
      if (entries != null) {
         entries.resolve(ctxt, handler);
      }
   }

   public static URI getURI(final DatabindContext ctxt, final EObject object) {
      if (object == null) {
         return null;
      }

      @SuppressWarnings("unchecked")
      Map<EObject, URI> objects = (Map<EObject, URI>) ctxt.getAttribute(Internals.MAP_OF_OBJECTS);
      if (objects == null) {
         objects = new HashMap<>();
         ctxt.setAttribute(Internals.MAP_OF_OBJECTS, objects);
      }

      URI uri = objects.get(object);

      if (uri == null) {
         uri = EcoreUtil.getURI(object);
         objects.put(object, uri);
      }

      return uri;
   }

   public static EClass findEClass(final DatabindContext ctxt, final String uri) {
      if (uri == null || uri.isEmpty()) {
         return null;
      }

      @SuppressWarnings("unchecked")
      Map<String, EObject> uris = (Map<String, EObject>) ctxt.getAttribute(Internals.MAP_OF_URIS);
      if (uris == null) {
         uris = new HashMap<>();
         ctxt.setAttribute(Internals.MAP_OF_URIS, uris);
      }

      EObject object = uris.get(uri);

      if (object instanceof EClass) {
         return (EClass) object;
      }

      ResourceSet resourceSet = getResourceSet(ctxt);
      if (resourceSet == null) {
         return null;
      }

      object = resourceSet.getEObject(URI.createURI(uri), true);
      if (object instanceof EClass) {
         uris.put(uri, object);
         return (EClass) object;
      }
      return null;
   }

   public static EClass findEClassByName(final DatabindContext ctxt, final String name) {
      @SuppressWarnings("unchecked")
      Set<EClass> types = (Set<EClass>) ctxt.getAttribute(Internals.ALL_TYPES);
      if (types == null) {
         types = initAllTypes(ctxt);
      }

      return types.stream().filter(findByName(name)).findFirst().orElse(null);
   }

   public static EClass findEClassByQualifiedName(final DatabindContext ctxt, final String name) {
      @SuppressWarnings("unchecked")
      Set<EClass> types = (Set<EClass>) ctxt.getAttribute(Internals.ALL_TYPES);
      if (types == null) {
         types = initAllTypes(ctxt);
      }

      return types.stream().filter(findByQualifiedName(name)).findFirst().orElse(null);
   }

   private static Set<EClass> initAllTypes(final DatabindContext ctxt) {
      EPackage.Registry global = EPackage.Registry.INSTANCE;

      ResourceSet resourceSet = getResourceSet(ctxt);
      EPackage.Registry local = resourceSet.getPackageRegistry();

      Map<String, Object> registry = new HashMap<>();
      registry.putAll(global);
      registry.putAll(local);

      Set<EClass> types = registry.values().stream()
         .map(e -> {
            if (e instanceof EPackage.Descriptor) {
               return ((EPackage.Descriptor) e).getEPackage();
            } else if (e instanceof EPackage) {
               return (EPackage) e;
            } else {
               return null;
            }
         })
         .filter(Objects::nonNull)
         .flatMap(e -> stream(spliteratorUnknownSize(e.eAllContents(), ORDERED), false))
         .filter(e -> e instanceof EClass)
         .map(e -> (EClass) e)
         .collect(Collectors.toSet());

      ctxt.setAttribute(Internals.ALL_TYPES, types);

      return types;
   }

   public static EClass findEClassByName(final String value, final EPackage ePackage) {
      return iterateAndFind(Collections.singleton(ePackage), findByName(value));
   }

   public static EClass findEClassByQualifiedName(final String value, final EPackage ePackage) {
      return iterateAndFind(Collections.singleton(ePackage), findByQualifiedName(value));
   }

   private static Predicate<EObject> findByName(final String value) {
      return e -> value != null && e instanceof EClass && value.equals(((EClass) e).getName());
   }

   private static Predicate<EObject> findByQualifiedName(final String value) {
      return e -> value != null && e instanceof EClass && value.equals(((EClass) e).getInstanceClassName());
   }

   private static EClass iterateAndFind(final Collection<Object> packages, final Predicate<EObject> predicate) {
      return (EClass) packages.stream()
         .flatMap(e -> stream(spliteratorUnknownSize(((EPackage) e).eAllContents(), ORDERED), false))
         .filter(e -> e instanceof EClass)
         .filter(predicate)
         .findFirst()
         .orElse(null);
   }

   public static Resource getResource(final DatabindContext ctxt, final EObject object) {
      @SuppressWarnings("unchecked")
      Map<EObject, Resource> resources = (Map<EObject, Resource>) ctxt.getAttribute(Internals.MAP_OF_RESOURCES);

      if (resources == null) {
         resources = new HashMap<>();
         ctxt.setAttribute(Internals.MAP_OF_RESOURCES, resources);
      }

      Resource resource = resources.get(object);
      if (resource == null) {
         if (object instanceof InternalEObject) {
            resource = ((InternalEObject) object).eDirectResource();
         }

         if (resource == null) {
            resource = object.eResource();
         }

         resources.put(object, resource);
      }
      return resource;
   }

   public static ResourceSet getResourceSet(final DatabindContext context) {
      ResourceSet resourceSet;
      try {
         resourceSet = (ResourceSet) context.getAttribute(Attributes.RESOURCE_SET);
      } catch (ClassCastException e) {
         resourceSet = null;
      }

      if (resourceSet == null) {
         resourceSet = new ResourceSetImpl();
         context.setAttribute(Attributes.RESOURCE_SET, resourceSet);
      }

      return resourceSet;
   }

   public static Resource getResource(final DatabindContext context) {
      try {
         return (Resource) context.getAttribute(Attributes.RESOURCE);
      } catch (ClassCastException e) {
         return null;
      }
   }

   public static URI getURI(final DatabindContext context) {
      try {
         return (URI) context.getAttribute(Attributes.RESOURCE_URI);
      } catch (ClassCastException e) {
         return null;
      }
   }

   public static EClass getRoot(final DatabindContext context) {
      try {
         return (EClass) context.getAttribute(Attributes.ROOT_ELEMENT);
      } catch (ClassCastException e) {
         return null;
      }
   }

   public static EObject getParent(final DatabindContext ctxt) {
      try {
         return (EObject) ctxt.getAttribute(Internals.CURRENT_PARENT);
      } catch (ClassCastException e) {
         return null;
      }
   }

   public static EReference getReference(final DatabindContext ctxt) {
      try {
         return (EReference) ctxt.getAttribute(Internals.CURRENT_FEATURE);
      } catch (ClassCastException e) {
         return null;
      }
   }

   public static EStructuralFeature getFeature(final DatabindContext ctxt) {
      try {
         return (EStructuralFeature) ctxt.getAttribute(Internals.CURRENT_FEATURE);
      } catch (ClassCastException e) {
         return null;
      }
   }

   public static EDataType getDataType(final DatabindContext ctxt) {
      try {
         return (EDataType) ctxt.getAttribute(Internals.CURRENT_DATATYPE);
      } catch (ClassCastException e) {
         return null;
      }
   }

   public static ReferenceEntries getEntries(final DatabindContext ctxt) {
      try {
         return (ReferenceEntries) ctxt.getAttribute(Internals.REFERENCE_ENTRIES);
      } catch (ClassCastException e) {
         ReferenceEntries entries = new ReferenceEntries();
         ctxt.setAttribute(Internals.REFERENCE_ENTRIES, entries);

         return entries;
      }
   }

   public static void setParent(final DatabindContext ctxt, final EObject parent) {
      ctxt.setAttribute(Internals.CURRENT_PARENT, parent);
   }

   public static void setFeature(final DatabindContext ctxt, final EStructuralFeature feature) {
      ctxt.setAttribute(Internals.CURRENT_FEATURE, feature);
   }

   public static void setDataType(final DatabindContext ctxt, final EClassifier type) {
      ctxt.setAttribute(Internals.CURRENT_DATATYPE, type);
   }

   public static EcoreTypeFactory getTypeFactory(final DatabindContext ctxt) {
      EcoreTypeFactory factory = (EcoreTypeFactory) ctxt.getAttribute(Internals.TYPE_FACTORY);
      if (factory == null) {
         factory = new EcoreTypeFactory();
         ctxt.setAttribute(Internals.TYPE_FACTORY, factory);
      }
      return factory;
   }

   @SuppressWarnings("checkstyle:cyclomaticComplexity")
   public static List<EClass> allSubTypes(final DatabindContext ctxt, final EClass eClass) {
      if (eClass == null) {
         return Collections.emptyList();
      }

      @SuppressWarnings("unchecked")
      Map<EClass, List<EClass>> subTypeMap = (Map<EClass, List<EClass>>) ctxt.getAttribute(Internals.MAP_OF_SUB_TYPES);
      if (subTypeMap == null) {
         subTypeMap = new HashMap<>();
         ctxt.setAttribute(Internals.MAP_OF_SUB_TYPES, subTypeMap);
      }

      List<EClass> subTypes = subTypeMap.get(eClass);
      if (subTypes == null) {
         subTypes = new ArrayList<>();

         @SuppressWarnings("unchecked")
         Set<EClass> allTypes = (Set<EClass>) ctxt.getAttribute(Internals.ALL_TYPES);
         if (allTypes == null) {
            allTypes = initAllTypes(ctxt);
         }

         for (EClass type : allTypes) {
            if (!type.isAbstract() && eClass.isSuperTypeOf(type)) {
               subTypes.add(type);
            }
         }

         subTypeMap.put(eClass, subTypes);
      }

      return subTypes;
   }

}
