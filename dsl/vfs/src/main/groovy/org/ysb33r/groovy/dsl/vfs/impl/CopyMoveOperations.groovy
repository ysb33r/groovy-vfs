// ============================================================================
// Copyright (C) Schalk W. Cronje 2012
//
// This software is licensed under the Apche License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================
package org.ysb33r.groovy.dsl.vfs.impl

import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.FileSelector
import org.apache.commons.vfs2.AllFileSelector
import org.ysb33r.groovy.dsl.vfs.FileActionException
import org.apache.commons.vfs2.Selectors

class CopyMoveOperations {

	static def friendlyURI( FileObject uri ) {
		return uri.name.friendlyURI
	}

	static def copy( FileObject from,FileObject to,boolean smash,boolean overwrite,filter=null ) {

		def fromType= from.type
		def toType= to.type
		assert fromType != FileType.FILE_OR_FOLDER
		
		if(!from.exists()) {
			throw new FileActionException("Source '${from.friendlyURI}' does not exist")
		}

		def selector

		switch(filter) {			
			case null:
				selector=Selectors.SELECT_ALL
				break
			default:
				// TODO: Handle specific filter types
				assert "NEEDS IMPLEMENTATION"
		}		

		switch(fromType) {
			case FileType.FILE:
				_copyFromSourceFile(from,to,smash,overwrite,selector)
				break					
			case FileType.FOLDER:
				_copyFromSourceDir(from,to,smash,overwrite,selector)
				break
		}		
		
		// void copyFileOverExistingDirectoryWithOverwriteWithoutSmashFails() {
		// void copyFileOverExistingDirectoryWithOverwriteWithSmashReplacesDirectoryWithFile() {
		// void copyDirectoryToDirectoryAddsToDirectory()
		// void copyDirectoryOverExistingDirectoryWithoutOverwriteFails()
		// void copyDirectoryOverExistingDirectoryWithOverwriteReplacesDirectory()
		// void copyDirectoryWIthFilterSelectivelyCopiesFilesToNewDestination()
	}

	/** Implements copying from a source file
	 * 
	 */
	private static def _copyFromSourceFile(FileObject from,FileObject to,boolean smash,boolean overwrite,FileSelector selector) {
		def toType= to.type
		assert toType != FileType.FILE_OR_FOLDER

		FileObject target
		switch(toType) { 
			case FileType.FOLDER:					
				if(!smash) {
					target=to.resolveFile(from.name.baseName)
					if(target.type == FileType.FOLDER) {
						throw new FileActionException("Destination directory '${this.friendlyURI(to)}' contains directory with the same name as the source file '${from.name.baseName}'") 
					} else if (target.type == FileType.FILE && !overwrite && target.exists()) {
						throw new FileActionException("'${this.friendlyURI(target)}' exists and overwrite mode is not set")
					}					
				} else {
					target=to
				}

				break
			case FileType.FILE:
				if(!overwrite && to.exists()) {
					throw new FileActionException("'${this.friendlyURI(to)}' exists and overwrite mode is not set")							
				}
			case FileType.IMAGINARY:
				target=to
				break
			default:
				assert "Should never get here"
		}

		target.copyFrom(from,selector)
		return
	}	

	/** Implements copying from a source directory
	 * 
	 */
	private static def _copyFromSourceDir(FileObject from,FileObject to,boolean smash,boolean overwrite,FileSelector selector) {
		def toType= to.type
		def target
		if(toType == FileType.FILE && !smash) {
			throw new FileActionException("${this.friendlyURI(from)} is a directory, ${this.friendlyURI(to)} is afile, and smash is not set")
		}
		
		assert "NEEDS IMPLEMENTATION"
		target.copyFrom(from,selector)
	}
}
