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
package org.eclipse.emfcloud.jackson.handlers;

import org.eclipse.emf.common.util.URI;

/**
 * URIHandler that does not modify uris during resolve and
 * deresolve operations.
 *
 */
public class IdentityURIHandler implements URIHandler {

   @Override
   public URI resolve(final URI baseURI, final URI uri) {
      return uri;
   }

   @Override
   public URI deresolve(final URI baseURI, final URI uri) {
      return uri;
   }

}
