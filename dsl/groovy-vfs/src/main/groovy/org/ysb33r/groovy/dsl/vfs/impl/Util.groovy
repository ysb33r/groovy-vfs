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

package org.ysb33r.groovy.dsl.vfs.impl

import groovy.transform.CompileDynamic
import org.apache.commons.logging.Log
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemManager
import org.ysb33r.groovy.dsl.vfs.FileSystemException
import org.ysb33r.groovy.dsl.vfs.OptionException
import org.ysb33r.groovy.dsl.vfs.URI;
import groovy.transform.CompileStatic
import org.apache.commons.vfs2.provider.TemporaryFileStore
import org.apache.commons.vfs2.impl.DefaultFileReplicator
import org.ysb33r.groovy.dsl.vfs.URIException

import java.util.regex.Pattern

@CompileStatic
class Util {

    static final Pattern OPTION_REGEX = ~/^(?i:vfs\.)(\p{Alpha}\p{Alnum}+)\.(\p{Alpha}\w+)$/

	/** Resolves a file from a URI.
	 * @param properties Map of 'vfs.SCHEME.OPTION' properties. ANy options passed in here will override options in defaultFSOptions.
	 * @param fsMgr Apache VFS FileSystemManager instance
	 * @param defaultFSOptions Default filesystem options that is used as a baseline
	 * @param uri URI instance or something that can be converted to a URI
     * @return Returns an implementation-dependent VFS-located object (currently {@code org.apache.commons.vfs2.FileObject}).
	 */
    @CompileDynamic
	static FileObject resolveURI (Map properties=[:],FileSystemManager fsMgr,FileSystemOptions defaultFSOptions,def uri) {
		def u = uri instanceof URI ? uri : new URI(uri)
		def fo = this.buildOptions(u,fsMgr,defaultFSOptions)
		fsMgr.resolveFile(u.toString(), properties ? this.buildOptions(properties,fsMgr,fo) : fo )
	}

    /** Returns a map only containing vfs options
     *
     * @param options Map with any kinds of options including options starting with vfs.
     * @return Filtered Map
     */
    @CompileDynamic
    static Map<String,Object> selectVfsOptions(final Map<String,Object> options) {
        Map<String,Object> filteredMap = [:]
        options.each { k,v ->
            def m = k =~ OPTION_REGEX
            if (m.matches()) {
                filteredMap[k] = v
            }
        }
        filteredMap
    }

	/** Traverses a map extracting keys in the form of 'vfs.SCHEME.FSOPTION'.
	 * Keys not of this form or not supported by the current file system 
	 * manager will be ignored.
	 * @param options A map of options with keys in the form of 
	 * 'vfs.SCHEME.FSOPTION'.
	 * @param options 
	 * @param fsMgr File system manager
	 * @param baseFSOpt If supplied this is used as the initial file system options
     * @return An implementation-dependent object of options (current @{code org.apache.commons.vfs2.FileSystemOptions}).
	 */
    @CompileDynamic
	static def buildOptions (Map options,FileSystemManager fsMgr, FileSystemOptions baseFSOpt=null) {
		def fsOpt = baseFSOpt ? baseFSOpt.clone() : new FileSystemOptions() 
		options.each { k,v ->
			def m = k =~ OPTION_REGEX
			if (m.matches()) {
				def scheme = m[0][1]
				def opt = m[0][2]
                fsOpt = setOption( scheme, opt, fsMgr, fsOpt, v )
			}
		} 
		return fsOpt
	}

    /** Extracts property information from a URI object and returns a FileSystemOptions instance
     * @param uri URI object
     * @param fsMgr File system manager
     * @param baseFSOpt If supplied this is used as the initial file system options
     * @return An implementation-dependent object of options (current @{code org.apache.commons.vfs2.FileSystemOptions}).
     */
    @CompileDynamic
    static def buildOptions ( URI uri, FileSystemManager fsMgr, FileSystemOptions baseFSOpt=null ) {
        def fsOpt = baseFSOpt ? baseFSOpt.clone() : new FileSystemOptions()
        uri.properties.each { scheme,options ->
            options.each { k,v ->
                fsOpt = setOption( scheme,k,fsMgr,fsOpt, v)
            }
        }
        return fsOpt
    }

    /** Allows a child path to be appended to an existing URI.
     * Additional query parameters or fragments are not allowed and will result in an exception.
     *
     * @param uri Parent URI
     * @param cs Relative path to be appended
     * @return A new path with the child path appended. All non-VFS query string parameters will remain intact and
     * all existing properties will be inherited.
     * @throw {@link org.ysb33r.groovy.dsl.vfs.URIException} if additional query parameters or fragments are found.
     * @since 1.0
     */
    static FileObject addRelativePath(FileObject fo,CharSequence cs) {
        String path= cs.toString()
        java.net.URI jnURI
        try {
            jnURI =  path.toURI()
        } catch(URISyntaxException e) {
            throw new URIException(path,'This path is suitable as a relative path')
        }
        if(jnURI.rawFragment || jnURI.rawQuery || jnURI.absolute) {
            throw new URIException(path,'Absolute paths, queries or fragments are not allowed when adding relative paths')
        }
        fo.resolveFile(path)
    }

    /** Allows a child path to be appended to an existing URI.
     * Additional query parameters or fragments are not allowed and will result in an exception.
     *
     * @param uri Parent URI
     * @param cs Relative path to be appended
     * @return A new path with the child path appended. All non-VFS query string parameters will remain intact and
     * all existing properties will be inherited.
     * @throw {@link org.ysb33r.groovy.dsl.vfs.URIException} if additional query parameters or fragments are found.
     * @since 1.0
     */
    static URI addRelativePath(URI uri,CharSequence cs) {
        uri / cs
    }

    /** Checks whether the URI is valid within the context of the given file system manager.
     *
     * @param uri URI instance to be checked
     * @param fsMgr File system manager
     * @return An implementation-dependent type if the URI if valid
     * @throw FileSystemException if not valid
     */
    static FileName validURI(URI uri,FileSystemManager fsMgr) {
        fsMgr.resolveURI(uri.toString())
    }

    /** Checks whether the scheme is considered to be a local file system
     *
     * @param scheme Schemt o be checked
     * @return {@code True} if local filesystem scheme
     * @since 1.0
     */
    static boolean localScheme(final String scheme) {
        scheme == 'file'
    }


    /** Checks whether the URI is local within the context of the given file system manager.
     *
     * @param uri URI instance to be checked
     * @param fsMgr File system manager
     * @return An implementation-dependent type if the URI if valid
     * @since 1.0
     */
    static boolean localURI(FileObject uri,FileSystemManager fsMgr) {
        localScheme(uri.name.scheme)
    }

    /** Checks whether the URI is local within the context of the given file system manager.
     *
     * @param uri URI instance to be checked
     * @param fsMgr File system manager
     * @return An implementation-dependent type if the URI if valid
     * @since 1.0
     */
    static boolean localURI(FileName uri,FileSystemManager fsMgr) {
        localScheme(uri.scheme)
    }

    /** Checks whether the URI is local within the context of the given file system manager.
     *
     * @param uri URI instance to be checked
     * @param fsMgr File system manager
     * @return An implementation-dependent type if the URI if valid
     * @throw FileSystemException if not valid
     * @since 1.0
     */
    static boolean localURI(URI uri,FileSystemManager fsMgr) {
        localURI(validURI(uri,fsMgr),fsMgr)
    }

    /** Checks whether the URI is local within the context of the given file system manager.
     *
     * @param uri URI instance to be checked
     * @param fsMgr File system manager
     * @return An implementation-dependent type if the URI if valid
     * @throw FileSystemException if not valid
     * @since 1.0
     */
    static boolean localURI(File uri,FileSystemManager fsMgr) {
        localURI(validURI(new URI(uri),fsMgr),fsMgr)
    }

    /** Checks whether the URI is local within the context of the given file system manager.
     *
     * @param uri URI instance to be checked
     * @param fsMgr File system manager
     * @return An implementation-dependent type if the URI if valid
     * @throw FileSystemException if not valid
     * @since 1.0
     */
    static boolean localURI(CharSequence uri,FileSystemManager fsMgr) {
        localURI(validURI(new URI(uri.toString()),fsMgr),fsMgr)
    }


    /** Sets a single option on a FileSystemOptions instance
     * @param scheme The ip scheme to set the option on i.e. 'ftp'
     * @param opt The specific option to set i.e. 'passiveMode'
     * @param fsMgr The current virtual file system instance that is in use
     * @param fsOpt The file system options instance associated with the VFS that needs updating
     * @param v The object value that the option needs to be updated to
     * @return An implementation-dependent object of options (current @{code org.apache.commons.vfs2.FileSystemOptions}).
     */
    @CompileDynamic
    static def setOption ( String scheme, String opt, FileSystemManager fsMgr, FileSystemOptions fsOpt,  v ) {

        final String LEVEL = 'debug'
        
        def builder = fsMgr.getFileSystemConfigBuilder(scheme)
        if(builder) {
            def setterName = "set${opt.capitalize()}"
            if (builder.metaClass.respondsTo(builder,setterName,FileSystemOptions,String) ) {
                log fsMgr, LEVEL, "Setting ${scheme} option (String): ${setterName} to ${v} "
                this.setValue( builder,setterName,fsOpt,v )
            } else if ( builder.metaClass.respondsTo(builder,setterName,FileSystemOptions,Integer) ) {
                log fsMgr, LEVEL, "Setting ${scheme} option (Integer): ${setterName} to ${v} "
                this.setValue( builder,setterName,fsOpt,v as Integer )
            } else if ( builder.metaClass.respondsTo(builder,setterName,FileSystemOptions,Boolean) ) {
                log fsMgr ,LEVEL, "Setting ${scheme} option (Boolean): ${setterName} to ${v} "
                this.setBooleanValue( builder,setterName,fsOpt,v )
            } else if (builder.metaClass.respondsTo(builder,setterName,FileSystemOptions,String[]) ) {
                // vfs.ftp.shortMonthNames
                throw new java.lang.Exception("Cannot support arrays as yet")
                // TODO: ISSUE #10 - vfs.http.cookies org.apache.commons.httpclient.Cookie[]
                // TODO: ISSUE #17 - vfs.http.proxyAuthenticator org.apache.commons.vfs2.UserAuthenticator
            } else {
                log fsMgr, 'warn', "'${opt}' is not a valid property for '${scheme}'"
            }
        }
        return fsOpt
    }

    /** Creates an Apache VFS TemporaryFileStore instance from a File instance
     *
     * @param f Directory to use
     * @return TemporaryFileStore
     */
    static TemporaryFileStore tempFileStoreFromPath( File f ) {
        new DefaultFileReplicator(f)
    }

    /** Creates an Apache VFS TemporaryFileStore instance from a directory name
     *
     * @param s Directory to use
     * @return TemporaryFileStore
     */
    static TemporaryFileStore tempFileStoreFromPath( String s ) {
        tempFileStoreFromPath(new File(s))
    }

    @CompileDynamic
	private static def setValue  ( builder,operation,fsOpts,value ) {
		builder."${operation}"(fsOpts,value)
	}


    @CompileDynamic
	private static def setBooleanValue (builder,operation,fsOpts,value) {
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
				if (value.metaClass.respondsTo(value,'asBoolean')) {
					builder."${operation}"(fsOpts,value.asBoolean())					
				} else if (value.metaClass.respondsTo(value,'toString')) {
					this.setBooleanValueFromValidString(builder,operation,fsOpts,value.toString())
				} else {
					throw new OptionException("Only Boolean or types with toBoolean() are allowed for setting Boolean options")
				}			
		}
	}

    @CompileDynamic
	private static def setBooleanValueFromValidString (builder,operation,fsOpts,String value ) {
		builder."${operation}"(fsOpts,value.toBoolean())
		
	}

    @CompileDynamic
	private static void log (FileSystemManager fsMgr,String level,Object data) {
        if(fsMgr.metaClass.respondsTo(fsMgr,'loggerInstance')) {
            fsMgr.loggerInstance()."${level}" (data)
        }
	}

}
