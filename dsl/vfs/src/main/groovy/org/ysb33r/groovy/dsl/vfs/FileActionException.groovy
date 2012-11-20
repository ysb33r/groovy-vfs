// ============================================================================
// Copyright (C) Schalk W. Cronje 2012
//
// This software is licensed under the Apche License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================
package org.ysb33r.groovy.dsl.vfs

import org.apache.commons.vfs2.FileSystemException

/** Thrown for when specific file update actions is not allowed
 * 
 * @author scronje
 *
 */
class FileActionException extends FileSystemException {
	FileActionException(String s) {
		super(s)
	}
	
	FileActionException(String s,Object f,Throwable e) {
		super(s,f,e)
	}
}
