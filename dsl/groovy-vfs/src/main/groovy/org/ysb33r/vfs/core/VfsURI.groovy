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
//
// ============================================================================
// (C) Copyright Schalk W. Cronje 2013-2015
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
//

package org.ysb33r.vfs.core

import groovy.transform.CompileDynamic
import groovy.transform.EqualsAndHashCode
import groovy.transform.CompileStatic

import java.nio.file.Path
import java.util.regex.Matcher

/** Holds a URI that has not been located on the virtual file system
 *
 * @author Schalk W. Cronjé
 */
@EqualsAndHashCode
@CompileStatic
class VfsURI {

    /**
     * Creates a URI from a Java URI object
     *
     * @param u URI to use.
     */
    VfsURI(final URI u ) {
        configureFromURI(u,null)
    }

    /** Creates a URI from a Java URI object and seed it with default options.
     *
     * <p> Any VFS options foudn in the URI will override the default options
     *
     * @param u
     * @param opts
     */
    VfsURI(final URI u,final FileSystemOptions opts) {
        configureFromURI(u,opts)
    }

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
	VfsURI(final String s) {
		configureFromURI(toURI(s),null)
//		URIBuilder tmpuri
//		String userInfo
//
//		if(!s.trim().size()) {
//			throw new URIException("(blank)","Whitespace-only URI is not valid")
//		}
		
//		try {
//			tmpuri =new URIBuilder(s)
//		} catch (java.net.URISyntaxException e) {
//		 	// If this contains a VFS-encrypted password, remove it,
//			// then process the rest
//		 	def m= s =~ /^(\p{Alpha}[\p{Alnum}+-.:]*:\/\/)?(.+:\{\p{XDigit}+\}\@)(.+)/
//
//			if (!m || !m.size() || m[0].size()!=4) {
//				throw e
//			}
//		    tmpuri=new URIBuilder("${m[0][1]}${m[0][3]}")
//			userInfo=m[0][2]
//		}
//		def parsed = parseString(s)
//		tmpuri = parsed.uriBuilder
//
//		checkScheme(tmpuri)
//		tmpuri=removeAndUpdateVFSProperties(tmpuri)
//
//		if(parsed.userInfo) {
//			// Add userinfo back
//			def jnURI = tmpuri.toURI()
//			def fragment= jnURI.rawFragment
//			def query = jnURI.rawQuery
//			def port = jnURI.port.toString()
//			uri="${jnURI.scheme}://${parsed.userInfo}${jnURI.host}${port!='-1'?':'+port:''}${jnURI.rawPath}${query?'?'+query:''}${fragment?'#'+fragment:''}"
//		} else {
//			uri=tmpuri.toString()
//		}
	}

    VfsURI(final String s,final FileSystemOptions opts) {
        configureFromURI(toURI(s),opts)
    }

    VfsURI(final CharSequence cs) {
        configureFromURI(toURI(cs.toString()),null)
    }

    VfsURI(final CharSequence cs,final FileSystemOptions opts) {
        configureFromURI(toURI(cs.toString()),opts)
    }

    /**
     * Creates a URL from a Path object.
     * A relative file will be normalised to an absolute file path
     * @param p Existing NIO2 path.
     */
    VfsURI(final Path p) {
        this.path = p.toAbsolutePath()
        this.uri = this.path.toUri()
    }

    /**
	 * Creates a URL from a local file
	 *
     * <p> A relative file will be normalised to an absolute file path.
     *
	 * @param File
	 */
	VfsURI(final File f) {
        this.path = f.toPath().toAbsolutePath()
        this.uri = this.path.toUri()
//        configureFromURI(f.absoluteFile.toURI(),null)
	}

    /** Converts to a printable URI.
     *
     * <p> If the URI contains a password it will be masked out.
     *
     * @return Printable URI.
     */
	String toString() {
        UriUtils.friendlyURI(this.uri)
	}

	/** Returns all of the parsed properties
	 *
	 * @return a map in
	 */
	@CompileStatic
	Map<String,Map<String,Object>> getProperties() {
        null
//        props as Map
    }

	/** Add extra VFS properties. Any non-matching properties will be ignored.
	 *
	 * @param properties
	 * @since 1.0
	 */
	VfsURI addProperties(Map<String,Object> properties) {
//		properties.each { String k,Object v ->
//			if(k ==~ /^(?i:vfs\..+\..+)/) {
//				String schemeAndName = k[4..-1]
//				int splitPos = schemeAndName.indexOf('.')
//				String scheme = schemeAndName[0..(splitPos - 1)].toLowerCase()
//				String name = schemeAndName[(splitPos + 1)..-1]
//				if (!props."${scheme}") {
//					props[scheme] = [:]
//				}
//
//				props."${scheme}"."${name}" = v
//			}
//		}
		this
	}

	/** Allows a child path to be appended to an existing URI.
	 * Additional query parameters or fragments are not allowed and will result in an exception.
	 *
	 * @param childPath Relative path to be appended.
	 *
	 * @return A new path with the child path appended. All non-VFS query string parameters will remain intact and
	 * all existing properties will be inherited.
	 * @throw {@link URIException} if additional query parameters or fragments are found.
	 * @since 1.0
	 */
	VfsURI div(CharSequence childPath) {
//		Map<String,Object> m = parseString(uri)
//		new VfsURI(props,m.uriBuilder,m.userInfo,childPath.toString())
	}

    /** Returns the {@link java.nio.file.Path} object that is associated with this URI.
     *
     * @return Path
     */
    Path getPath() {
        this.path
    }

    URI getURI() {
        this.uri
    }

    String getFriendlyURI() {
        UriUtils.friendlyURI(this.uri)
    }

    /** Returns the filesystem options that are associated with this URI.
     *
     * @return Associated filesystem options. Never {@code null}.
     */
    FileSystemOptions getFileSystemOptions() {
        this.fsOptions
    }

    /** Returns basename of file or folder (including any extensions).
     *
     * @return Basename as a string
     */
    String getName() {
        path.getName(path.nameCount-1)
    }

//	private URIBuilder removeAndUpdateVFSProperties (URIBuilder tmpuri) {
//		def q= tmpuri.query
//		def p=[:]
//		if (q) {
//			q.each { k,v ->
//				if(k ==~ /^(?i:vfs\..+\..+)/) {
//					p[k]=v
//				}
//			}
//
//			tmpuri.setQuery (q - p)
//
//			if(p) {
//				addProperties(p)
//			}
//		}
//
//		return tmpuri
//	}

//	@TypeChecked
//	private def checkScheme (URIBuilder tmpuri) {
//		if (!tmpuri.scheme || !tmpuri.scheme.size()) {
//			throw new URIException("${tmpuri}", "URI must be created with a scheme")
//		}
//	}

//	/** Returns a URIBuilder and some userinfo if need be
//	 *
//	 * @param s String to parse
//	 * @return Object with uriBuilder, userInfo as properties
//	 */
//	private Map<String,Object> parseString( final String s ) {
//		def ret = [:]
//		try {
//			URIBuilder builder = new URIBuilder(s)
//			ret['uriBuilder']= builder
//			if(builder.userInfo?.size()) {
//				ret['userInfo'] = builder.userInfo + '@'
//			}
//		} catch (java.net.URISyntaxException e) {
//			// If this contains a VFS-encrypted password, remove it,
//			// then process the rest
//			def m= s =~ /^(\p{Alpha}[\p{Alnum}+-.:]*:\/\/)?(.+:\{\p{XDigit}+\}\@)(.+)/
//
//			if (!m || !m.size() || m[0].size()!=4) {
//				throw e
//			}
//			ret['uriBuilder']=new URIBuilder("${m[0][1]}${m[0][3]}")
//			ret['userInfo']=m[0][2]
//		}
//		ret
//	}

//	private Map<String,Map<String,Object>> props = [:]
//	private String uri
//
//	//public getFriendlyURI()
//
//	/** A private constructor used when appending child paths
//	 *
//	 * @param props Properties to inherit
//	 * @param tmpuri A URIBuilder object to work with
//	 * @param child A child path to append
//	 * @throw {@link URIException} if additional query parameters or fragments are found.
//	 * @since 1.0
//	 */
//	private VfsURI(Map<String,Map<String,Object>> props, URIBuilder tmpuri, final String userInfo, final String child ) {
//
//		// Construct a basic URI to
//		java.net.URI jnURI = tmpuri.toURI()
//		java.net.URI testuri
//		try {
//			testuri =new URIBuilder("${jnURI.scheme}://${jnURI.host}${jnURI.rawPath}/${child}").toURI()
//		} catch (java.net.URISyntaxException e) {
//			throw new URIException( child,"If not a valid path to append")
//		}
//		if(testuri.rawFragment || testuri.rawQuery ) {
//			throw new URIException( child,"Fragment or query found in child path")
//		}
//		def fragment= jnURI.rawFragment
//		def query = jnURI.rawQuery
//		def port = jnURI.port.toString()
//
//		uri="${jnURI.scheme}://${userInfo?:''}${jnURI.host}${port!='-1'?':'+port:''}${jnURI.rawPath}/${child}${query?'?'+query:''}${fragment?'#'+fragment:''}"
//		this.props=props
//	}

    private final FileSystemOptions fsOptions = new FileSystemOptions()
    private URI uri
    private Path path

    private Tuple2<URI,Map<String,String>> removeVFSProperties(final URI u) {
        null
    }

    /** Configure this object from a Java URI.
     *
     * @param u Provided URI with potential VFS options included.
     * @param opts Default options. Can be null.
     */
    private void configureFromURI(final URI u,final FileSystemOptions opts) {
        Tuple2<URI,Map<String,String>> realURI = removeVFSProperties(u)

        if(opts!=null) {
            fsOptions.addAll(opts.asMap())
        }

        realURI
        this.uri = realURI.first
    }

    @CompileDynamic
    private URI toURI(final String s) {
		if(!s.trim().size()) {
			throw new URIException("(blank)","Whitespace-only URI is not valid")
		}

        URI tmpuri
        String userInfo
		try {
			tmpuri = s.toURI()
		} catch (java.net.URISyntaxException e) {
		 	// If this contains a VFS-encrypted password, remove it,
			// then process the rest
		 	Matcher m= s =~ /^(\p{Alpha}[\p{Alnum}+-.:]*:\/\/)?(.+:\{\p{XDigit}+\}\@)(.+)/

			if (!m || !m.size() || m[0].size()!=4) {
				throw e
			}
		    tmpuri="${m[0][1]}${m[0][3]}".toURI()

			userInfo=m[0][2]
		}

    }
}
