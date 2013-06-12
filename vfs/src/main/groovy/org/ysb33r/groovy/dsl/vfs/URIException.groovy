// ============================================================================
// Copyright (C) Schalk W. Cronje 2012
//
// This software is licensed under the Apche License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================
package org.ysb33r.groovy.dsl.vfs

import groovy.transform.CompileStatic

/**
 * 
 * @author Schalk W. Cronjé
 */
@CompileStatic
class URIException extends java.net.URISyntaxException {
	URIException(String input,String reason) {
		super(input,reason)
	}
}
