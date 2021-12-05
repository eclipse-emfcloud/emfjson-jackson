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
package org.eclipse.emfcloud.jackson.errors;

import org.eclipse.emf.ecore.resource.Resource;

import com.fasterxml.jackson.core.JsonLocation;

public class JSONException extends Exception implements Resource.Diagnostic {

   private static final long serialVersionUID = -7658549711611092935L;
   private final String location;
   private final int line;
   private final int column;

   public JSONException(final String message, final JsonLocation location) {
      super(message);
      this.location = location.toString();
      this.line = location.getLineNr();
      this.column = location.getColumnNr();
   }

   public JSONException(final Exception e, final JsonLocation location) {
      super(e);
      this.location = location.toString();
      this.line = location.getLineNr();
      this.column = location.getColumnNr();
   }

   @Override
   public String getLocation() { return location; }

   @Override
   public int getLine() { return line; }

   @Override
   public int getColumn() { return column; }

}
