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

package org.ysb33r.vfs.core

import groovy.transform.CompileDynamic
import groovy.transform.EqualsAndHashCode
import groovy.transform.CompileStatic
import org.ysb33r.vfs.auth.BasicCredentials
import org.ysb33r.vfs.auth.Credentials
import org.ysb33r.vfs.auth.CredentialsSupplier

import java.nio.file.Path
import java.nio.file.Paths
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
    VfsURI(final URI u) {
        configureFromURI(u,null)
    }

    /** Creates a URI from a Java URI object and seed it with default options.
     *
     * <p> Any VFS options found in the URI will override the default options
     *
     * @param u URI to use.
     * @param opts Filesystem options to use as starting point.
     */
    VfsURI(final URI u,final FileSystemOptions opts) {
        configureFromURI(u,opts)
    }

    /** Creates a URI from a Java URL object and seed it with default options.
     *
     * <p> Any VFS options found in the URL will override the default options
     *
     * @param u URL to use.
     */
    VfsURI(final URL u) {
        configureFromURI(u.toURI(),null)
    }

    /** Creates a URI from a Java URL object and seed it with default options.
     *
     * <p> Any VFS options found in the URI will override the default options
     *
     * @param u URL to use.
     * @param opts Filesystem options to use as starting point.
     */
    VfsURI(final URL u,final FileSystemOptions opts) {
        configureFromURI(u.toURI(),opts)
    }

    /**
     * Creates a URL from a string or any character sequence.
     *
     * As a further extension to the query string part, any part in the form vfs.SCHEME.PROPERTY will be removed
     * from the URI and interpreted as a NIO2 provider property
     *
     * <p> As from 2.0 the Apache Commons VFS-style encoded password is no longer directly supported. You can still use the
     * specific obfuscation method, but you will have to provide your own {@link org.ysb33r.vfs.auth.CredentialsSupplier} in order to do so.
     *
     * @param cs String
     *
     */
    VfsURI(final CharSequence cs) {
        configureFromURI(toURI(cs.toString()),null)
    }

    /**
     * Creates a URL from a string or any character sequence.
     *
     * As a further extension to the query string part, any part in the form vfs.SCHEME.PROPERTY will be removed
     * from the URI and interpreted as a NIO2 provider property
     *
     * <p> As from 2.0 the Apache Commons VFS-style encoded password is no longer directly supported. You can still use the
     * specific obfuscation method, but you will have to provide your own {@link org.ysb33r.vfs.auth.CredentialsSupplier} in order to do so.
     *
     * @param cs String
     * @param opts Filesystem options to use as starting point.
     *
     */
    VfsURI(final CharSequence cs,final FileSystemOptions opts) {
        configureFromURI(toURI(cs.toString()),opts)
    }

    /**
     * Creates a URL from a Path object.
     *
     * <p> A relative file will be normalised to an absolute file path.
     *
     * @param p Existing NIO2 path.
     */
    VfsURI(final Path p) {
        this.path = p.toAbsolutePath()
        this.uri = this.path.toUri()
    }

    /**
	 * Creates a URI from a local file
	 *
     * <p> A relative file will be normalised to an absolute file path.
     *
	 * @param File
	 */
	VfsURI(final File f) {
        this.path = f.toPath().toAbsolutePath()
        this.uri = this.path.toUri()
	}

    /** Creates a URI from an existing URI and a relative path.
     *
     * @param uri Root URI
     * @param relativePath relativePath
     */
    VfsURI(final VfsURI uri,final CharSequence relativePath) {
        this.path = uri.path.resolve(relativePath.toString())
        this.uri = this.path.toUri()
        this.fsOptions = new FileSystemOptions(uri.fileSystemOptions)
    }

    /** The current credentials associated with the URI.
     *
     * @return CUrrent credentials or {@code null}.
     */
    CredentialsSupplier getCredentials() {
        this.credentials
    }

    /** Provides an object that can return the credentials in a way suitable for the
     * the specific URI.
     *
     * @param creds
     */
    void setCredentials(final CredentialsSupplier creds) {
        this.credentials = creds
    }

    /** Simplifier method for setting the common username/password combination.
     *
     * @param username Username associated with URI
     * @param password Password associated with URI. Can be null.
     */
    void setCredentials( final CharSequence username, final CharSequence  password) {
        this.credentials = Credentials.fromUsernamePassword(username,password)
    }

    /** Remove any associated credentials.
     *
     */
    void removeCredentials() {
        this.credentials = null
    }

    /** Resolves a URI from a path relative to this URI.
     *
     * @param relativePath relativePath.
     * @return New URI.
     */
    VfsURI resolve(final CharSequence relativePath) {
        new VfsURI(this,relativePath)
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
	Map<String,Object> getProperties() {
        fileSystemOptions.asMap()
    }

	/** Add extra VFS properties. Any non-matching properties will be ignored.
	 *
	 * @param properties
	 * @since 1.0
	 */
	VfsURI addProperties(final Map<String,Object> properties) {
        fsOptions.addAll(properties)
		this
	}

    /** Returns the {@link java.nio.file.Path} object that is associated with this URI.
     *
     * @return Path
     */
    Path getPath() {
        this.path
    }

    /** Returns the {@link java.net.URI} representation associated with this URI.
     *
     * @return URI without user information.
     */
    URI getUri() {
        this.uri
    }

    /** Returns the {@link java.net.URI} representation associated with this URI.
     *
     * <p> This will include user information if it is available and of representable type.
     *
     * @return URI with user informaton.
     */
    URI getUriWithCredentials() {
        if(credentials != null) {
            if(credentials instanceof BasicCredentials) {
                BasicCredentials bc = (BasicCredentials)credentials
                new URI(
                    this.uri.scheme,
                    "${UriUtils.encode(bc.username)}:${UriUtils.encode(bc.password)}",
                    this.uri.host,
                    this.uri.port,
                    this.uri.rawPath,
                    this.uri.rawQuery,
                    this.uri.rawFragment
                )
            } else {
                throw new URIException("Credentials type is ${credentials.class.name}",'Credentials associated with this URI is not an instance of org.ysb33r.vfs.auth,BasicCredentials')
            }
        } else {
            this.uri
        }
    }

    /** Returns a string representation where the password has been masked out.
     *
     * @return Printable URI.
     */
    String getFriendlyURI() {
        UriUtils.friendlyURI(getUriWithCredentials())
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

    /** Configure this object from a Java URI.
     *
     * <p> VFS options are removed and process. User infomration is also removed
     * and added as BasicCredentials object.
     *
     * @param u Provided URI with potential VFS options included.
     * @param opts Default options. Can be null.
     */
    private void configureFromURI(final URI u,final FileSystemOptions opts) {

        String scheme = u.scheme

        if(scheme == null || scheme.empty) {
            throw new URIException("${u}", "URI must be created with a scheme")
        }

        Tuple2<URI,Map<String,String>> realURI

        String[] userParts = UriUtils.splitRawUserInfo(u)

        if(userParts != null ) {
            realURI = removeVFSProperties(new URI(u.scheme,null,u.host,u.port,u.rawPath,u.rawQuery,u.rawFragment))
            credentials = Credentials.fromUsernamePassword(
                UriUtils.decode(userParts[0]),
                userParts.size()>1 ? UriUtils.decode(userParts[1]) : null
            )
        } else {
            realURI = removeVFSProperties(u)
        }


        if(opts!=null) {
            fsOptions.addAll(opts)
        }

        fsOptions.addAll(realURI.second)
        this.uri = realURI.first
        this.path = Paths.get(this.uri)
    }

    // Splits out the URI into an ew URI with any VFS query parameters removed.
    // Returns the stripped parameters as a decoded map
    private Tuple2<URI,Map<String,String>> removeVFSProperties(final URI u) {

        Map<String,String> props = [:]

        if(u.rawQuery == null) {
            return new Tuple2<URI,Map<String,String>>(u,props)
        }

        Map<String,String> queryParts = UriUtils.splitQuery(u)
        queryParts.collectEntries(props) { entry ->
            entry.key.toLowerCase().startsWith('vfs.')
        }

        if(props.isEmpty()) {
            return new Tuple2<URI,Map<String,String>>(u,props)
        }

        String newRawQuery = (queryParts - props).collect { String k, String v ->
            "${UriUtils.encode(k)}=${UriUtils.encode(v)}"
        }.join('&')

        new Tuple2<URI,Map<String,String>>(new URI(u.scheme,u.userInfo,u.host,u.port,u.path,newRawQuery,u.fragment),props)
    }

    private URI toURI(final String s) {

		if(!s.trim().size()) {
			throw new URIException("(blank)","Whitespace-only URI is not valid")
		}

        s.toURI()
    }

    private final FileSystemOptions fsOptions = new FileSystemOptions()
    private URI uri
    private Path path
    private CredentialsSupplier credentials


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

}
