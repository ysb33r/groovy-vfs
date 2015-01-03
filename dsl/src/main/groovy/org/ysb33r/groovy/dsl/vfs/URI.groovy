// ============================================================================
// Copyright (C) Schalk W. Cronje 2012
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================
package org.ysb33r.groovy.dsl.vfs

import groovyx.net.http.URIBuilder
import groovy.transform.EqualsAndHashCode
import groovy.transform.TypeChecked
import java.net.URISyntaxException
import groovy.transform.CompileStatic
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject

/** Holds a URI that has not been located on the virtual file system
 *
 * @author Schalk W. Cronjé
 */
@EqualsAndHashCode
class URI {
	
	/**
	 * Creates a URL from a URL string. 
	 * As an extension to normal URI syntax, this will also accept a URI
	 * with a VFS-style encoded password i.e. ftp://ysb33r:{D7B82198B272F5C93790FEB38A73C7B8}@127.0.0.1/path
	 * 
	 * As a further extension to the query string part, any part in the form vfs.SCHEME.PROPERTY will be removed
	 * from the URI and interpreted as a VFI provider property
	 * 
	 * @param String 
	 */
	URI ( String s ) {
		
		URIBuilder tmpuri
		String userInfo
		
		if(!s.trim().size()) {
			throw new URIException("(blank)","Whitespace-only URI is not valid")
		}
		
		try {
			tmpuri =new URIBuilder(s)
		} catch (java.net.URISyntaxException e) {
		 	// If this contains a VFS-encrypted password, remove it,
			// then process the rest
		 	def m= s =~ /^(\p{Alpha}[\p{Alnum}+-.:]*:\/\/)?(.+:\{\p{XDigit}+\}\@)(.+)/

			if (!m || !m.size() || m[0].size()!=4) {
				throw e
			}	 
		    tmpuri=new URIBuilder("${m[0][1]}${m[0][3]}")
			userInfo=m[0][2]
		}
		
		checkScheme(tmpuri)
		tmpuri=removeAndUpdateVFSProperties(tmpuri)
		
		if(userInfo) {
			// Add userinfo back
			def jnURI = tmpuri.toURI()
			def fragment= jnURI.rawFragment
			def query = jnURI.rawQuery
			def port = jnURI.port.toString()
			uri="${jnURI.scheme}://${userInfo}${jnURI.host}${port!='-1'?':'+port:''}${jnURI.rawPath}${query?'?'+query:''}${fragment?'#'+fragment:''}"
		} else {
			uri=tmpuri.toString()
		}
	}
	
	/**
	 * Creates a URL from a local file
	 * A relative file will be normalised to an absolute file path
	 * @param File
	 */
	URI ( File f ) {
		uri=new URIBuilder(f.toURI().normalize().toString().replaceFirst('file:','file://'))
	} 
	
	/**
	 * Creates a URL from a VFS FileName object
	 * @param f A VFS FileName object
	 */
	URI ( FileName f ) {
		uri= f.getURI()
	}

	@CompileStatic
	String toString() {
		return uri
	}

	/** Returns all of the parsed properties
	 *
	 * @return a map in
	 */
	@CompileStatic
	Map<String,Map<String,Object>> getProperties() {props as Map}

	/** Add extra VFS properties. Any non-matching properties will be ignored.
	 *
	 * @param properties
	 * @since 1.0
	 */
	URI addProperties(Map<String,Object> properties) {
		properties.each { String k,Object v ->
			if(k ==~ /^(?i:vfs\..+\..+)/) {
				String schemeAndName = k[4..-1]
				int splitPos = schemeAndName.indexOf('.')
				String scheme = schemeAndName[0..(splitPos - 1)].toLowerCase()
				String name = schemeAndName[(splitPos + 1)..-1]
				if (!props."${scheme}") {
					props[scheme] = [:]
				}

				props."${scheme}"."${name}" = v
			}
		}
		this
	}

	private URIBuilder removeAndUpdateVFSProperties (URIBuilder tmpuri) {
		def q= tmpuri.query
		def p=[:]
		if (q) {
			q.each { k,v ->
				if(k ==~ /^(?i:vfs\..+\..+)/) {
					p[k]=v
				}
			}
			
			tmpuri.setQuery (q - p)
			
			if(p) {
				addProperties(p)
			}
		}
	
		return tmpuri
	}

	@TypeChecked
	private def checkScheme (URIBuilder tmpuri) { 
		if(!tmpuri.scheme || !tmpuri.scheme.size()) {
			throw new URIException("${tmpuri}","URI must be created with a scheme")
		}
	}
	
	private Map<String,Map<String,Object>> props = [:]
	private String uri
	  
	//public getFriendlyURI()
	
}
