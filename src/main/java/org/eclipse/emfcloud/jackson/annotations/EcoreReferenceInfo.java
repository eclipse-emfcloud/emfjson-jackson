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
package org.eclipse.emfcloud.jackson.annotations;

import org.eclipse.emfcloud.jackson.handlers.BaseURIHandler;
import org.eclipse.emfcloud.jackson.handlers.URIHandler;

public class EcoreReferenceInfo {

   public static final String PROPERTY = "$ref";

   private final URIHandler handler;
   private final String property;

   public EcoreReferenceInfo(final String property) {
      this(property, new BaseURIHandler());
   }

   public EcoreReferenceInfo(final URIHandler handler) {
      this(PROPERTY, handler);
   }

   public EcoreReferenceInfo(final String property, final URIHandler handler) {
      this.property = property;
      this.handler = handler;
   }

   public String getProperty() { return property; }

   public URIHandler getHandler() { return handler; }
}
