// ============================================================================
// Copyright (C) Schalk W. Cronje 2012 - 2013
//
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================

package org.ysb33r.groovy.dsl.vfs.impl

import org.apache.commons.logging.Log
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.FileSystemManager
import org.ysb33r.groovy.dsl.vfs.OptionException
import org.ysb33r.groovy.dsl.vfs.URI;
import groovy.transform.TypeChecked
import groovy.transform.CompileStatic

class Util {

	/** Resolves a file from a URI.
	 * @param properties Map of 'vfs.SCHEME.OPTION' properties. ANy options passed in here will override options in defaultFSOptions.
	 * @param fsMgr Apache VFS FileSystemManager instance
	 * @param defaultFSOptions Default filesystem options that is used as a baseline
	 * @param uri URI instance or something that can be converted to a URI
	 */
	static def resolveURI (properties=[:],FileSystemManager fsMgr,FileSystemOptions defaultFSOptions,uri) {
		def u = uri instanceof URI ? uri : new URI(uri)
		def fo = this.buildOptions(u,fsMgr,defaultFSOptions)
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
	static def buildOptions (Map options,FileSystemManager fsMgr, FileSystemOptions baseFSOpt=null) {
		def fsOpt = baseFSOpt ? baseFSOpt.clone() : new FileSystemOptions() 
		options.each { k,v ->
			def m = k =~ /^(?i:vfs\.)(\p{Alpha}\p{Alnum}+)\.(\p{Alpha}\w+)$/
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
     */
    static def buildOptions ( URI uri, FileSystemManager fsMgr, FileSystemOptions baseFSOpt=null ) {
        def fsOpt = baseFSOpt ? baseFSOpt.clone() : new FileSystemOptions()
        uri.properties().each { scheme,options ->
            options.each { k,v ->
                fsOpt = setOption( scheme,k,fsMgr,fsOpt, v)
            }
        }
        return fsOpt
    }
    
    /** Sets a single option on a FileSystemOptions instance
     * @param scheme The protocol scheme to set the option on i.e. 'ftp'
     * @param opt The specific option to set i.e. 'passiveMode'
     * @param fsMgr The current virtual file system instance that is in use
     * @param fsOpt The file system options instance associated with the VFS that needs updating
     * @param v The object value that the option needs to be updated to
     */
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
                // TODO: vfs.http.cookies org.apache.commons.httpclient.Cookie[]
                // TODO: vfs.http.proxyAuthenticator org.apache.commons.vfs2.UserAuthenticator
                // TODO: vfs.res.classLoader java.lang.ClassLoader
            } else {
                log fsMgr, 'warn', "'${opt}' is not a valid property for '${scheme}'"
            }
        }
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
		builder."${operation}"(fsOpts,value.toBoolean())
		
	}
    
	private static void log (FileSystemManager fsMgr,String level,Object data) {
        if(fsMgr.metaClass.respondsTo(fsMgr,'loggerInstance')) {
            fsMgr.loggerInstance()."${level}" (data)
        }
	}

}
