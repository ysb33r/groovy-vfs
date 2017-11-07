/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2017
 *
 * This software is licensed under the Apache License 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0 for license details
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * ============================================================================
 */
package org.ysb33r.vfs.dsl.groovy.impl

import org.apache.commons.vfs2.Capability

import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileObject
import static org.apache.commons.vfs2.FileType.*
import org.apache.commons.vfs2.FileSelector
import org.apache.commons.vfs2.AllFileSelector
import org.apache.commons.vfs2.FileSystemException
import org.ysb33r.groovy.dsl.vfs.FileActionException
import org.ysb33r.groovy.dsl.vfs.FilterException;
import org.apache.commons.vfs2.Selectors


import groovy.transform.TypeChecked
import groovy.transform.CompileStatic


class CopyMoveOperations {

	/** An overwrite policy that will only overwrite when the source is newer than the destination.
	 * If either scheme of of the source or the destination does not support {@code Capability.GET_LAST_MODIFIED}, then
	 * overwrite will be sanctioned.
	 */
	static final Closure ONLY_NEWER = { FileObject from, FileObject to ->
		if(from.fileSystem.hasCapability(Capability.GET_LAST_MODIFIED) && to.fileSystem.hasCapability(Capability.GET_LAST_MODIFIED)) {
			return from.content.lastModifiedTime > to.content.lastModifiedTime
		} else {
			return true
		}
	}

	@CompileStatic
	static def friendlyURI( FileObject uri ) {
		return uri.name.friendlyURI
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @param smash
	 * @param overwrite 'true', 'false' or a Closure which will be passed a fileObject and must return groovy truth
	 * @param filter Optional filter to apply. If the from type is a file, then the filter is ignored.
	 * @return
	 */
    @CompileStatic
	static def copy( FileObject from,FileObject to,boolean smash,def overwrite,boolean recursive,def filter=null ) {

		def fromType= from.type
		def toType= to.type
		assert fromType != FILE_OR_FOLDER
		
		if(!from.exists()) {
			throw new FileActionException("Source '${friendlyURI(from)}' does not exist")
		}

		FileSelector selector=_createSelector(filter)
		switch(fromType) {
			case FILE:
				_copyFromSourceFile(from,to,smash,_overwritePolicy(overwrite),selector)
				break					
			case FOLDER:
                _copyFromSourceDir(from,to,smash,_overwritePolicy(overwrite),recursive,selector)
				break
		}		
		
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @param smash - Ability to forcefully replace a file with a folder and vice-versa
	 * @param overwrite
	 * @param intermediates Default behaviour is to create intermediate subdirectories of target path, should they not exist. Set to 'false' if this behaviour is not desired
	 * @param noCreateSubFolder In most cases when moving a folder into a folder then target subfolder needs to be
	 * created. There are some cases where this behaviour is deemed inappropriate. In those cases set to true.
	 * @return
	 */
    @CompileStatic
	static def move( FileObject from,FileObject to,boolean smash,def overwrite,boolean intermediates=true,boolean noCreateSubFolder=false) {
		def fromType= from.type
		def toType= to.type
		assert fromType != FILE_OR_FOLDER
		
		if(!from.exists()) {
			throw new FileActionException("Source '${friendlyURI(from)}' does not exist")
		}
        
        if(smash) {
            _moveItem(from,to,intermediates)
        } else {
            if(toType == FOLDER && !noCreateSubFolder) { 
                return move(from,to.resolveFile(from.name.baseName),false,overwrite,intermediates,true)    
            }
            
    		if(fromType==FOLDER && toType==FILE || toType==FOLDER && fromType==FILE) {
    			throw new FileActionException("Cannot replace folder with file  or file with folder if smash=='false'. Moving '${friendlyURI(to)}' over '${friendlyURI(from)}' is not allowed")
    		}
    		
            if(fromType==FOLDER && toType==FOLDER && from.name.baseName == to.name.baseName && overwrite) {
                _recursiveMoveAndOverwrite(from,to,_overwritePolicy(overwrite))
            } else {
        		def existing= to.exists()
        		if( !existing ) {
                    _moveItem(from,to,intermediates)
        		} else if (existing && overwrite ) {
                    if(_overwritePolicy(overwrite).call(from,to)) {
                        _moveItem(from,to,intermediates)
                    }
        		} else {
        			throw new FileActionException("Replacing '${friendlyURI(to)}' with '${friendlyURI(from)}' is not allowed as both overwrite and smash are 'false'.")
        		}
            }
        }
	}

    /** Moves an item, creating intermediates if necessary.
     * 
     * @param from
     * @param to
     * @param intermediates
     * @return
     */
    @CompileStatic
    private static void _moveItem(FileObject from,FileObject to,boolean intermediates) {
        def fromType= from.type
        def toType= to.type

        if(intermediates && toType==IMAGINARY ) {
            if(fromType==FILE) {
                to.createFile()
            } else if(fromType==FILE) {
                to.createFolder()
            }
        }
        try {
            from.moveTo(to)
        } catch (FileSystemException e) {
            if(intermediates) {
                throw e
            } else {
                throw new FileActionException(
                        "Attempt to move a file of folder fail with intermediates:false. This could be due to target subdirectories not existing.",
                        to,e
                )
            }
        }
    }	
    
    /** A recursive move of files for when source and target folders have exactly the same name and selective overwrite
     * is required. 
     * 
     * This internal function has to iterate through each child and move them individually
     * 
     * @param from
     * @param to
     * @return
     */
    @CompileStatic
    private static def _recursiveMoveAndOverwrite(FileObject from,FileObject to, Closure overwrite) {
        assert from.type == FOLDER
        assert to.type == FOLDER
        assert from.name.baseName == to.name.baseName
        
        from.children.each { FileObject f ->
            move(f,to.resolveFile(f.name.baseName),false,overwrite,false,false)
        }    
    }
    
	/** Creates a VFS selector from a passed in filter
	 * @return
	 */
	private static FileSelector _createSelector(filter) {
		def selector
		switch (filter) {
			case null:
				selector=Selectors.SELECT_ALL
				break		
			case String :
				selector=_createSelector(~/${filter}/)	
				break	
			case Pattern :
				selector = [
					'includeFile' : { fsi -> fsi.file.name.baseName ==~ filter },
					'traverseDescendents' : { fsi -> true }
				] as FileSelector
				break
			case FileSelector:
                selector=filter
				break
			default:
				throw new FilterException("Supplied type (${filter?.class}) is not suitable for a filter")
		}
		
		return selector
	}
	
	/** Implements copying from a source file
	 * 
	 */
	private static def _copyFromSourceFile(FileObject from,FileObject to,boolean smash,Closure overwrite,FileSelector selector) {
		def toType= to.type
		assert toType != FILE_OR_FOLDER

		FileObject target
		switch(toType) { 
			case FOLDER:					
				if(!smash) {
					target=to.resolveFile(from.name.baseName)
					if(target.type == FOLDER) {
						throw new FileActionException("Destination directory '${this.friendlyURI(to)}' contains directory with the same name as the source file '${from.name.baseName}'") 
					} else if (target.type == FILE && target.exists() && !overwrite(from,target) ) {
						throw new FileActionException("'${this.friendlyURI(target)}' exists and overwrite mode is not set")
					}					
				} else {
					target=to
				}

				break
			case FILE:
				if(to.exists() && !overwrite(from,to)) {
					throw new FileActionException("'${this.friendlyURI(to)}' exists and overwrite mode is not set")							
				}
			case IMAGINARY:
				target=to
				break
			default:
				assert "Should never get here",false
		}

		target.copyFrom(from,selector)
		return
	}	

	/** Implements copying from a source directory
	 * 
	 */
	private static def _copyFromSourceDir(
            FileObject from,
            FileObject to,
            boolean smash,
            Closure overwrite,
            boolean recursive,
            FileSelector selector
    ) {
		def toType= to.type
		FileObject target

		switch(toType) {
			case FILE:
				if(!smash) {
					throw new FileActionException("${this.friendlyURI(from)} is a directory, ${this.friendlyURI(to)} is a file, and smash is not set")				
				}
				to.copyFrom(from,selector)
				break
			case FOLDER:
				if(smash) {					
					def original=to.parent.resolveFile('$'*10+"${to.name.baseName}"+'$'*10)
					target=to.parent.resolveFile(from.name.baseName)
					to.moveTo(original)
					try {
						target.copyFrom(from,selector)						
					}
					finally {
						original.delete(Selectors.SELECT_ALL)
					}
					return 
				} else if (recursive) {
					target=to.resolveFile(from.name.baseName)
                    if (selector instanceof AntPatternSelector && selector.excludeSelf) {
                        to.copyFrom(from, _currySelectorWithOverwrite(from, to, selector, overwrite))
                    } else if(target.exists()) {
						_recursiveDirCopyNoSmash(from, target, selector, overwrite)
                    } else if (selector==Selectors.EXCLUDE_SELF) {
                        _recursiveDirCopyNoSmash(from, to, Selectors.SELECT_ALL, overwrite)
					} else {
						target.copyFrom(from,selector)
					}
				} else {
					throw new FileActionException( "Attempt to copy from folder '${friendlyURI(from)}' to folder '${friendlyURI(to)}', but recursive and smash are not set")
				}
				break
			case IMAGINARY:
				if(recursive) {
					to.copyFrom(from,selector)
				} else {
					throw new FileActionException( "Attemping to copy ${friendlyURI(from)} to ${friendlyURI(to)}, but recursive is off" )
				}
				break
			default:
				assert false,"Should never get here"			
		}			
	}
	
	/** Performs a recursive directory to directory copy, applying an overwrite
	 * policy along the way. Only the descendants of the source directory are 
	 * copied. If the overwrite closure returns 'false' at any point it, 
	 * a FileActionException will be raised.
	 * 
	 * @param from Source directory to copy from.
	 * @param to Directory to copy to
	 * @param selector A selector to choose which children from the source to copy
	 * @param overwrite Closure that returns true or false whether a source should overwrite a target
	 * @return
	 */
	private static def _recursiveDirCopyNoSmash(FileObject from,FileObject to,FileSelector selector,Closure overwrite) {

        FileSelector combinedSelector = _currySelectorWithOverwrite(from,to,selector,overwrite)
		from.children.each {
			def nextTarget=to.resolveFile(it.name.baseName)
			try {
				nextTarget.copyFrom(it,combinedSelector)
			} catch(Exception e) {
				throw new FileActionException("Failing to create '${friendlyURI(nextTarget)}'. Maybe overwrite was not allowed.",nextTarget,e)
			}
		}
	}

    private static FileSelector _currySelectorWithOverwrite(FileObject from,FileObject to, FileSelector selector,Closure overwrite) {
        [
            includeFile : {
                if (!selector.includeFile(it)) {
                    return false
                }
                def src= from.name.getRelativeName(it.file.name)
                def target=to.resolveFile(src)

                if(target.exists()) {
                    if(!overwrite(it.file,target)) {
                        throw new FileActionException("Overwriting existing target '${friendlyURI(to)}' is not allowed")
                    } else if(target.type == FOLDER && it.file.type == FOLDER) {
                        return true
                    } else if(target.type == FOLDER) {
                        throw new FileActionException("Replacing existing target folder '${friendlyURI(to)}' with a file is not allowed")
                    }
                }
                return true
            },
            traverseDescendents : {selector.traverseDescendents(it)}
        ] as FileSelector

    }

	/** Returns a closure which can be prompted on a file-by-file basis about overwriting
	 * 
	 * @param overwrite
	 * @return
	 */
	@CompileStatic
	private static Closure _overwritePolicy(overwrite) {
		switch(overwrite) {
			case Closure:
				return overwrite as Closure
			case true:
				return {f,t->true}
			case false:
				return {f,t->false}
			default:
				return {f,t->false}
		}
	}
}
