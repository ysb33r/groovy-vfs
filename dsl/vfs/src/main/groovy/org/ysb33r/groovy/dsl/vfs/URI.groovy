package org.ysb33r.groovy.dsl.vfs

import groovyx.net.http.URIBuilder
import groovy.transform.EqualsAndHashCode
import java.net.URISyntaxException

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;


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
	
	String toString() {
		return uri
	}
	
	def properties = { props } 
	
	private URIBuilder removeAndUpdateVFSProperties (URIBuilder tmpuri) {
		def q= tmpuri.query
		def p=[:]
		if (q) {
			q.each { k,v ->
				if(k ==~ /^(?i:vfs\..+\..+)/) {
					p[k]=v
				}
			}
			
			tmpuri.query = q.minus(p)
			
			if(p) {
				p.each { k,v ->
					def schemeAndName= k[4..-1]
					def splitPos=schemeAndName.indexOf('.')
					def scheme=schemeAndName[0..(splitPos-1)].toLowerCase()
					def name=schemeAndName[(splitPos+1)..-1]
					if (!props."${scheme}") {
						props[scheme] = [:]
					}
					 
					props."${scheme}"."${name}" = v
					
				}				
			}
		}
	
		return tmpuri
	}

	private def checkScheme (URIBuilder tmpuri) { 
		if(!tmpuri.scheme || !tmpuri.scheme.size()) {
			throw new URIException("${tmpuri}","URI must be created with a scheme")
		}
	}
	
	private def props = [:]
	private String uri
	  
	//public getFriendlyURI()
	
}
