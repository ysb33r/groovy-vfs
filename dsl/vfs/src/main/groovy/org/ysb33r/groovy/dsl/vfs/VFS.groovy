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
import org.apache.commons.vfs2.impl.StandardFileSystemManager
import org.ysb33r.groovy.dsl.vfs.impl.Util


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
 * vfs.exec {
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
				default:
					selector = [
						'includeFile' : { fsi -> fsi.file.name.baseName ==~ /"${properties.filter.toString()}"/ },
						'traverseDescendents' : traverse
					]
			}
			
			children=ruri.findFiles(selector as FileSelector)
			 	
		} else if (recurse) {
			children= ruri.findFiles( new AllFileSelector() )
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
		resolveURI(properties,uri).createFolder()
	}

/*		

	def cp = { properties=[:],from,to,Closure c=null ->
	   println "cp ${from} -> ${to}"
	}
	
	def mv = { properties=[:],from,to,Closure c=null ->
	   println "mv ${from} -> ${to}"
	}
	
	
	
	def friendlyURI( uri ) {
		
	}
	
*/	

	private def resolveURI (properties=[:],uri) {
		if (uri instanceof FileObject) {
			properties.size() ?	Util.resolveURI(properties,fsMgr,uri.fileSystem.fileSystemOptions,uri.getURI()) : uri	
		} else {
			Util.resolveURI(properties,fsMgr,defaultFSOptions,uri)
		} 
	}
}
