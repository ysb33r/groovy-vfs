// ============================================================================
// Copyright (C) Schalk W. Cronje 2012
//
// This software is licensed under the Apche License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================

/**
 * 
 */
package org.ysb33r.groovy.dsl.vfs

import java.util.regex.Pattern
import org.apache.commons.logging.impl.NoOpLog
import org.apache.commons.vfs2.AllFileSelector
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager
import org.ysb33r.groovy.dsl.vfs.impl.CopyMoveOperations
import org.ysb33r.groovy.dsl.vfs.impl.Util
import org.apache.commons.logging.Log

/**
 * @author Schalk W. Cronjé
 *
 * {@code
 * import org.ysb33r.groovy.dsl.vfs.VFS
 * 
 * def vfs = VFS.manager( ..... )
 * 
 * 
 * vfs.cp ftp://foo.example/myfile sftp://bar.example/yourfile
 * 
 * vfs << {
 *   cp http://first.example/myfile, sftp://second.example/yourfile
 *   mv sftp://second.example/yourfile, ftp://third.example/theirfile
 *   
 *   ls http://first.example {
 *   }
 *    
 *   cat http://first.example/myfile {
 *   }
 * }
 * 
 * }
 */
class VFS {

	private def fsMgr
	private FileSystemOptions defaultFSOptions

	/**
	 * @param properties - Default properties for initialising the system 
	 * @li cacheStrategy - Sets the cache strategy to use when dealing with file object data
	 * @li defaultProvider - Default provider for unknown schemas
	 * @li filesCache - Sets the file cache implementation
	 * @li logger Sets the logger to use. Unlike the Apache VFS2 default behaviour, not providing this property, will turn off VFS logging completely
	 * @li replicator - Sets the replicator
	 * @li temporaryFileStore - Sets the temporary file store
	 * @li Any file system properties in the form vfs.FILESYSTEM.OPTION i.e. vfs.ftp.passive
	 */
	VFS( Map properties=[:] ) {
		fsMgr = new StandardFileSystemManager()
		
		[ 'cacheStrategy','defaultProvider','filesCache','replicator','temporaryFileStore' ].each {
			if(properties.hasProperty(it)) {
				fsMgr."set${it.capitalize()}"(properties[it])
			}
		}
		
		fsMgr.setLogger( properties.containsKey('logger') ? properties['logger'] : new NoOpLog() )
		fsMgr.init()
		fsMgr.metaClass.loggerInstance = {->getLogger()}
		
		defaultFSOptions = Util.buildOptions(properties,fsMgr)
	}

	/**
	 * Executes a sequence of operations on the same VFS
	 */
	def script = { Closure c ->
		def newc=c.clone()
		newc.delegate=this
		newc.call()
	}
	
	/** Override the << operator and an alias to the script method
	 * 
	 */
	def leftShift = script
	
	/**
	 * If used without a closure it will return an of VFS FileObject[]. 
	 * If a closure is passed to it, then the closure will be called with a FileObject for
	 * each instance found
	 * 
	 * @param properties
	 * @li filter A regex, closure of VFS FileSelector by which to select objects
	 * @li recursive If set to true will traverse down any subfolders. Ignored if filter is a FileSelector 
	 *
	 * @param uri URI pointing to area for which file listign is to be retrieved
	 * @param c Closure that will be a single parameter (VFS FileObject)
	 * 
	 * @return If a closure is specified returns the results of passing each file to the
	 * provided closure, otherwise just return VFS FileObject[] 
	 * 
	 * @code{
	 *   def vfs = new VFS()
	 *   vfs.ls 'file:///usr/bin' 
	 *   vfs.ls ('file:///usr/bin') {
	 *     println it.name
	 *   }
	 *   
	 *   vfs.ls (filter: /^a.+/, 'file:///usr/bin')  {
	 *     println it.name
	 *   }  
	 *   
	 *   vfs.ls (recursive: true, filter: {}, 'file:///usr/bin')  {
	 *   }  
	 * }	
	 * 
	 */
	def ls ( properties=[:],uri,Closure c ) {
		assert properties != null
		def children
		def ruri=resolveURI(properties,uri)
		boolean recurse = properties.containsKey('recursive') ? properties.recursive : false
		
		if( properties.containsKey('filter') ) {
			
			def selector
			def traverse = { fsi -> fsi.depth==0 || recurse }
			switch (properties.filter) {
				case Pattern :
					selector = [
						'includeFile' : { fsi -> fsi.file.name.baseName ==~ properties.filter },
						'traverseDescendents' : traverse
					]
					break
				case FileSelector:
					selector=properties.filter
					break
				case Closure:
					selector = [
						'includeFile' : { fsi -> properties.filter.call(fsi) },
						'traverseDescendents' : traverse
					]
					break
				default:
					selector = [
						'includeFile' : { fsi -> fsi.file.name.baseName ==~ /"${properties.filter.toString()}"/ },
						'traverseDescendents' : traverse
					]
			}
			
			children=ruri.findFiles(selector as FileSelector)
			 	
		} else if (recurse) {
			children= ruri.findFiles( Selectors.SELECT_ALL )
		}
		else {
			children= ruri.children			
		}
		
		if(c) {
			def newc=c.clone()
			newc.delegate=this
			return children.collect { newc.call(it) }
		} else {
			return children
		}
	}

	/**
	 * 
	 * @param uri
	 * @return
	 */
	def ls ( properties=[:],uri ) { ls(properties,uri,null) }
	
	/** Allows for the content of a local or remote file object to be read
	 * 
	 */
	def cat ( properties=[:],uri,Closure c ) {
		assert properties != null
		def istream=resolveURI(properties,uri).content.inputStream
		c ? istream.withStream(c) : istream
	}

	/**
	 * 
	 * @param uri
	 * @return InputStream
	 * 
	 * @code {
	 * vfs.cat ('file:///etc/resolv.conf').text 
	 * }
	 */
	def cat ( properties=[:],uri ) { cat(properties,uri,null) }

	/** Creates a folder on any VFS that allows this functionality
	 * @param properties Any additional vfs properties
	 * @param uri Folder that needs to be created
	 */
	def mkdir ( properties=[:],uri ) {
		assert properties != null
		resolveURI(properties,uri).createFolder()
	}

	/**
	 * @param from URI to copy from. If source is a folder all descendants will be copied recursively.
	 * See filter property for selectively copying descendants
	 * @param to URI to copy to. If destination is a folder, it will be placed inside folder. 
	 * If destination is a file it weill be replaced if overwrite property is true. 
	 * @param properties 
	 * @li overwrite. Set to true to force overwrite of an existing target. Set to false, not to allow overwrites. 
	 * Can also be a closure, in which case it needs to be of interface boolean(from,to).
	 * @li filter.  A filter to select which file objects to copy. The filter is ignored if teh source is a file.
	 * @li smash. Set to true, to replace an existing target file with a source directory
	 * @li recursive. Used when source is a directory to indicate that directories should be copied recursively. 
	 *     Like -r/--recursive in POSIX cp.  
	 * 
	 * The following rules apply, if no filter is provided:
	 * 
	 * <table>
	 * <tr><th>From FileType</th><th>To FileType><th>Overwrite?</th><th>Smash?</th><th>Recursive</th><th>Action</th></tr>
	 * <tr>
	 *   <td>FILE</td>  <td>IMAGINARY</td><td>No</td> <td>No</td><td>-</td>   <td>Copy (create file)</td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FILE</td>     <td>No</td> <td>-</td> <td>-</td>   <td>Don't copy</td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FILE</td>     <td>Yes</td><td>-</td> <td>-</td>   <td>Overwrite file</td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FOLDER</td>   <td>No</td> <td>No</td> <td>-</td>  <td>If a same-named file does not exist in the folder, copy it, otherwise don't copy</td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FOLDER</td>   <td>Yes</td><td>No</td> <td>-</td>  <td>Create same-named file in the folder, even it exists. If same-named directory exists, fail</td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FOLDER</td>   <td>-</td>  <td>Yes</td><td>-</td>  <td>Replace same-named folder with a filename</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>IMAGINARY</td><td>No</td> <td>No</td> <td>No</td> <td>Don't copy</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>IMAGINARY</td><td>No</td> <td>No</td> <td>Yes</td><td>Copy directory and descendants</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FILE</td>     <td>No</td> <td>No</td> <td>???</td><td>Don't copy </td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FILE</td>     <td>Yes</td><td>No</td> <td>???</td><td>Don't copy </td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FILE</td>     <td>-</td>  <td>Yes</td><td>???</td><td> Replace file with folder </td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>No</td> <td>No</td> <td>No</td><td>Don't copy</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>No</td> <td>No</td> <td>Yes</td><td>Copy as a subfolder. Existing files will be skipped</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>Yes</td><td>No</td> <td>Yes</td><td>Copy as subfolder, replacing any same-named files along the way. </td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>-</td>  <td>Yes</td><td>???</td><td>Replace existing folder and its contents with the content of the source folder </td>
	 * </tr>
	 * </table>
	 *    
	 */
	def cp ( properties=[:],from,to ) {
		assert properties != null
		
		CopyMoveOperations.copy(
			resolveURI(properties,from),
			resolveURI(properties,to),
			properties.smash ?: false,
			properties.overwrite ?: false,
			properties.recursive ?: false,
			properties.filter
		)
	}
 
/*		

	
	def mv = { properties=[:],from,to,Closure c=null ->
	   println "mv ${from} -> ${to}"
	}
	
	
	
*/	

	def friendlyURI( FileObject uri ) {
		return uri.name.friendlyURI
	}
	
	def friendlyURI( URI uri ) {
		return friendlyURI(resolveURI(uri))
	}
	
	Log logger() {
		fsMgr.loggerInstance()
	}
	
	private def resolveURI (properties=[:],uri) {
		if (uri instanceof FileObject) {
			properties.size() ?	Util.resolveURI(properties,fsMgr,uri.fileSystem.fileSystemOptions,uri.getURI()) : uri	
		} else {
			Util.resolveURI(properties,fsMgr,defaultFSOptions,uri)
		} 
	}
}
