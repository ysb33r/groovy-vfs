// ============================================================================
// Copyright (C) Schalk W. Cronje 2012
//
// This software is licensed under the Apche License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================

package org.ysb33r.groovy.dsl.vfs.impl


import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.FileSystemManager
import org.ysb33r.groovy.dsl.vfs.OptionException
import org.ysb33r.groovy.dsl.vfs.URI;

class Util {

	/** Resolves a file from a URI.
	 * @param properties Map of 'vfs.SCHEME.OPTION' properties. ANy options passed in here will override options in defaultFSOptions.
	 * @param fsMgr Apache VFS FileSystemManager instance
	 * @param defaultFSOptions Default filesystem options that is used as a baseline
	 * @param uri URI instance or something that can be converted to a URI
	 */
	static def resolveURI (properties=[:],FileSystemManager fsMgr,FileSystemOptions defaultFSOptions,uri) {
		def u = uri instanceof URI ? uri : new URI(uri)
		def fo = this.buildOptions(u.properties(),fsMgr,defaultFSOptions)
		fsMgr.resolveFile(u.toString(), properties ? this.buildOptions(properties,fsMgr,fo) : fo )
	}
	
	/** Traverses a map extracting keys in the form of 'vfs.SCHEME.FSOPTION'.
	 * Keys not of this form or not supported by the current file system 
	 * manager will be ignored.
	 * @param options A map of options with keys in the form of 
	 * 'vfs.SCHEME.FSOPTION'.
	 * @param options 
	 * @param fsMgr File system manager
	 * @param baseFSOpt If supplied this is used as the initial file system options
	 */
	static def buildOptions = { Map options,FileSystemManager fsMgr, FileSystemOptions baseFSOpt=null ->
		def fsOpt = baseFSOpt ? baseFSOpt.clone() : new FileSystemOptions() 
		options.each { k,v ->
			def m = k =~ /^(?i:vfs\.)(\p{Alpha}\p{Alnum}+)\.(\p{Alpha}\w+)$/
			if (m.matches()) {
				def scheme = m[0][1]
				def opt = m[0][2]
				def builder = fsMgr.getFileSystemConfigBuilder(scheme)				
				if(builder) {
					def setterName = "set${opt.capitalize()}"
					if (builder.metaClass.respondsTo(builder,setterName,FileSystemOptions,String) ) {
						this.setValue( builder,setterName,fsOpt,v )
					} else if (	builder.metaClass.respondsTo(builder,setterName,FileSystemOptions,Integer) ) {
						this.setValue( builder,setterName,fsOpt,v as Integer )						
					} else if (	builder.metaClass.respondsTo(builder,setterName,FileSystemOptions,Boolean) ) {
						this.setBooleanValue( builder,setterName,fsOpt,v )
					} else if (builder.metaClass.respondsTo(builder,setterName,FileSystemOptions,String[]) ) {
						// vfs.ftp.shortMonthNames
						throw new java.lang.Exception("Cannot support arrays as yet")
					} else {
						this.log(fsMgr,"'${opt}' is not a valid property for '${scheme}'")
					}
				} 
			}
		} 
		// vfs.http.cookies org.apache.commons.httpclient.Cookie[]
		// vfs.http.proxyAuthenticator org.apache.commons.vfs2.UserAuthenticator
		// vfs.res.classLoader java.lang.ClassLoader
		return fsOpt
	}

	private static def setValue = { builder,operation,fsOpts,value ->
		builder."${operation}"(fsOpts,value)
	}

	private static def setBooleanValue = { builder,operation,fsOpts,value ->
		switch (value) {
			case String:
				this.setBooleanValueFromValidString(builder,operation,fsOpts,value)
				break
			case GString:
				this.setBooleanValueFromValidString(builder,operation,fsOpts,value.toString())
				break
			case Boolean:
				builder."${operation}"(fsOpts,value)
				break
			default:
println "default: ${value}"
				if (value.metaClass.respondsTo(value,'asBoolean')) {
					builder."${operation}"(fsOpts,value.asBoolean())					
				} else if (value.metaClass.respondsTo(value,'toString')) {
					this.setBooleanValueFromValidString(builder,operation,fsOpts,value.toString())
				} else {
					throw new OptionException("Only Boolean or types with toBoolean() are allowed for setting Boolean options")
				}			
		}
	}

	private static def setBooleanValueFromValidString = { builder,operation,fsOpts,String value ->
println "XXX: ${value}"		
		builder."${operation}"(fsOpts,value.toBoolean())
		
	}
	private static def log = { fsMgr,data ->
		// TODO: fsMgr.getLogger
		println "TODO: IMPLEMENT LOGGING OF :::${data}:::"
	}

}
