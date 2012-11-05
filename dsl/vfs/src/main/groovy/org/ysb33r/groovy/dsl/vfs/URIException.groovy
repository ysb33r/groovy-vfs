package org.ysb33r.groovy.dsl.vfs

/**
 * 
 * @author scronje
 *
 */
class URIException extends java.net.URISyntaxException {
	URIException(String input,String reason) {
		super(input,reason)
	}
}
