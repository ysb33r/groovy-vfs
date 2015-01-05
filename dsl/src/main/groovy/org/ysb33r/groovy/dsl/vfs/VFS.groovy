// ============================================================================
// (C) Copyright Schalk W. Cronje 2012-2014
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================


/**
 * 
 */
package org.ysb33r.groovy.dsl.vfs

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.commons.vfs2.CacheStrategy
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileSelectInfo
import org.apache.commons.vfs2.FilesCache
import org.apache.commons.vfs2.provider.FileReplicator
import org.ysb33r.groovy.dsl.vfs.impl.FileContentEditor
import org.ysb33r.groovy.dsl.vfs.impl.StandardFileSystemManager

import java.util.regex.Pattern
import org.apache.commons.logging.impl.NoOpLog
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.provider.AbstractFileSystem
import org.ysb33r.groovy.dsl.vfs.impl.CopyMoveOperations
import org.ysb33r.groovy.dsl.vfs.impl.Util
import org.ysb33r.groovy.dsl.vfs.impl.ConfigDelegator
import org.ysb33r.groovy.dsl.vfs.impl.ProviderDelegator
import org.apache.commons.logging.Log
import org.ysb33r.groovy.dsl.vfs.impl.ProviderSpecification
import org.apache.commons.vfs2.provider.TemporaryFileStore
import org.apache.commons.vfs2.FileType
import static org.apache.commons.vfs2.Selectors.*

/**
 *
 * 
 * <pre>
 * import org.ysb33r.groovy.dsl.vfs.VFS
 * 
 * def vfs = new VFS()
 * 
 * 
 * vfs.cp ftp://foo.example/myfile sftp://bar.example/yourfile
 * 
 * vfs << {
 *   cp http://first.example/myfile, sftp://second.example/yourfile
 *   mv sftp://second.example/yourfile, ftp://third.example/theirfile
 *   
 *   options {
 *      ftp {
 *          passive true
 *      }
 *   }
 *   
 *   ls http://first.example {
 *   }
 *    
 *   options 'vfs.ftp.passive' : true
 *   
 *   cat http://first.example/myfile {
 *   }
 * }
 * 
 * </pre>
 * 
 * @author Schalk W. Cronjé
 * @since 0.1 */
@CompileStatic
class VFS {

    /** A filter that selects all the descendants of the base folder, but does not select the base folder itself.
     *
      */
    final static FileSelector exclude_self = EXCLUDE_SELF

    /** A filter that selects the base file/folder, plus all its descendants.
     *
     */
    final static FileSelector select_all = SELECT_ALL

    /** A filter that selects only the direct children of the base folder.
     *
     */
    final static FileSelector direct_children_only = SELECT_CHILDREN

    /** A filter that selects only files (not folders).
     *
      */
    final static FileSelector only_files = SELECT_FILES

    /** A filter that selects only folders (not files).
     *
     */
    final static FileSelector only_folders	= SELECT_FOLDERS

    /** A filter that select the base plus its direct descendants
     *
     */
    final static FileSelector self_and_direct_children = SELECT_SELF_AND_CHILDREN

    private StandardFileSystemManager fsMgr
	private FileSystemOptions defaultFSOptions

	/** Constructs a Virtual File System.
	 * 
	 * During construction a number of properties can be passed to the underlying Apache VFS system.
	 * <p> 
     * <li> cacheStrategy - Sets the cache strategy to use when dealing with file object data
     * <li> defaultProvider - Default provider for unknown schemas
     * <li> filesCache - Sets the file cache implementation
     * <li> logger Sets the logger to use. Unlike the Apache VFS2 default behaviour, not providing this property, will turn off VFS logging completely
     * <li> replicator - Sets the replicator
     * <li> temporaryFileStore - Sets the temporary file store
     * <li> ignoreDefaultProviders - Don't load any providers (overrides scanForVfsProviderXml, legacyPluginLoader)
     * <li> scanForVfsProviderXml - Look for META-INF/vfs-provider.xml files
     * <li> legacyPluginLoader - Load using providers.xml file from Apache VFS (implies scanForVfsProviderXml)
     * <p>
     * In addition any global filesystem options can also be set 
     * vfs.FILESYSTEM.OPTION i.e. <code> 'vfs.ftp.passiveMode' : true </code>
     * 
     * @param properties Default properties for initialising the system 
	 */
	VFS( Map properties=[:], Closure pluginLoader = null ) {
		fsMgr = new StandardFileSystemManager()
		
        Log vfslog = properties.containsKey('logger') ? (properties['logger'] as Log): new NoOpLog()

        if(properties.containsKey('defaultProvider')) {
            vfslog.debug "'defaultProvider' ignored as from v1.0. Use Provider configuration closure instead."
        }

        TemporaryFileStore tfs
        if(properties.containsKey('temporaryFileStore')) {
            switch(properties.temporaryFileStore) {
                case File:
                    tfs= Util.tempFileStoreFromPath(properties.temporaryFileStore as File)
                    break

                case String:
                    tfs= Util.tempFileStoreFromPath(properties.temporaryFileStore as String)
                    break

                case TemporaryFileStore:
                    tfs= properties.temporaryFileStore as TemporaryFileStore
                    break

                default:
                    throw new FileSystemException('temporaryFileStore needs to be File/String/TemporaryFileStore')
            }
        }

        boolean legacy = properties.legacyPluginLoader
        boolean scanForVfsXml = legacy ?: properties.scanForVfsProviderXml
        ProviderSpecification ps = ProviderSpecification.DEFAULT_PROVIDERS
        if( properties.containsKey('ignoreDefaultProviders') && properties.ignoreDefaultProviders != false ) {
            ps = new ProviderSpecification()
            legacy = false
            scanForVfsXml = false
        }


		fsMgr.init (
            ps,
            legacy,
            scanForVfsXml,
            tfs,
            properties.replicator as FileReplicator,
            vfslog,
            properties.cacheStrategy as CacheStrategy,
            properties.filesCache as FilesCache
        )

		defaultFSOptions = Util.buildOptions(properties,fsMgr) as FileSystemOptions
  	}

	/**
	 * Executes a sequence of operations on the same VFS
	 */
    @CompileDynamic
	def script ( @DelegatesTo(VFS) Closure c ) {
		def newc=c.clone()
		newc.delegate=this
		newc.call()
	}
	
    /** 
     * 
     * @param c
     * @return
     */
    def call ( @DelegatesTo(VFS) Closure c) {script(c)}
    
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
    @CompileDynamic
	def ls ( Map properties=[:],uri,Closure c ) {
		assert properties != null
		def children
		def ruri=resolveURI(properties,uri)
		boolean recurse = properties.containsKey('recursive') ? (properties['recursive'] as boolean): false
		
		if( properties.containsKey('filter') ) {
			
			def selector
			def traverse = { FileSelectInfo fsi -> fsi.depth==0 || recurse }
			switch (properties.filter) {
				case Pattern :
					selector = [
						'includeFile' : { FileSelectInfo fsi -> fsi.file.name.baseName ==~ properties.filter },
						'traverseDescendents' : traverse
					]
					break
				case FileSelector:
					selector=properties.filter
					break
				case Closure:
					selector = [
						'includeFile' : { FileSelectInfo fsi -> (properties.filter as Closure).call(fsi) },
						'traverseDescendents' : traverse
					]
					break
				default:
					selector = [
						'includeFile' : { FileSelectInfo fsi -> fsi.file.name.baseName ==~ /"${properties.filter.toString()}"/ },
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
			Closure newc=c.clone()
			newc.delegate=this
			return children.collect { newc.call(it) }
		} else {
			return children
		}
	}

	/**
	 *
	 * @param uri
    *
	 */
	def ls ( Map properties=[:],uri ) { ls(properties,uri,null) }
	
	/** Allows for the content of a local or remote file object to be read
     *
	 * @param properties - Optional property map
     * @param uri
     * @param c - Closure that takes an InputStream as argument
     *
     * @return result of closure
     *
     * @code
     * vfs.cat ('file:///etc/resolv.conf') { strm ->
     *   println strm.text
     * }
     * @endcode
     */
	def cat ( Map properties=[:],uri,Closure c ) {
		assert properties != null
        assert c != null
        FileObject fo=resolveURI(properties,uri)
        AbstractFileSystem afs= fo.fileSystem as AbstractFileSystem

        try {
		    return fo.content.inputStream.withStream(c)
        }
        finally {
            afs.closeCommunicationLink()
        }
	}

	/** Write content to a local or remote file object
	 *
	 * @param uri
	 *
	 * @return Proxy object which will take a 'with' keyword
	 *
	 * @code
	 * vfs.overwrite 'file:///etc/resolv.conf' with 'Some text'
	 * @endcode
	 */
	FileContentEditor overwrite( uri  ) {
		assert uri != null
		new FileContentEditor( resolveURI(uri),false )
	}

	/** Write content to a local or remote file object
	 *
	 * @param uri
	 * @param c - Closure that takes an OutputStream as argument
	 *
	 * @return Result of overwrite
	 *
	 * @code
	 * vfs.overwrite 'file:///etc/resolv.conf', { strm ->
	 *   strm << 'Some text'
	 * }
	 * @endcode
	 */
	def overwrite( uri,Closure c ) {
		assert uri != null
		new FileContentEditor( resolveURI(uri),false ).with(c)
	}

	/** Appends content to a local or remote file object
	 *
	 * @param uri
	 *
	 * @return Proxy object which will take a 'with' keyword
	 *
	 * @code
	 * vfs.append 'file:///etc/resolv.conf' with 'Some text'
	 * @endcode
	 */
	FileContentEditor append ( uri  ) {
		assert uri != null
		new FileContentEditor( resolveURI(uri), true )
	}

	/** Write content to a local or remote file object
	 *
	 * @param uri
	 * @param c - Closure that takes an OutputStream as argument
	 *
	 * @return Result of overwrite
	 *
	 * @code
	 * vfs.append 'file:///etc/resolv.conf', { strm ->
	 *   strm << 'Some text'
	 * }
	 * @endcode
	 */
	def append( uri,Closure c ) {
		assert uri != null
		new FileContentEditor( resolveURI(uri),true ).with(c)
	}

	/** Creates a folder on any VFS that allows this functionality
	 * @param properties Any additional vfs properties
     * @li intermediates. Set to false if intermediate folders should not be created.
	 * @param uri Folder that needs to be created
	 */
    @CompileDynamic
	def mkdir ( Map properties=[:],uri ) {
		assert properties != null
		def fo= resolveURI(properties,uri)
        if (properties.intermediates != null && properties.intermediates==false) {
            def parent = fo.parent
            if(!parent.exists()) {
                throw new FileActionException("Cannot create directory - '${friendlyURI(parent)}' does not exist and intermediates==false")
            }
        }
        fo.createFolder()
	}

	/** Copies files.
	 * @param from URI to copy from. If source is a folder all descendants will be copied recursively.
	 * See filter property for selectively copying descendants
	 * @param to URI to copy to. If destination is a folder, it will be placed inside folder. 
	 * If destination is a file it will be replaced if overwrite property is true. 
	 * @param properties 
	 * @li overwrite. Set to true to force overwrite of an existing target. Set to false, not to allow overwrites. 
	 * Can also be a closure, in which case it needs to be of interface boolean(from,to).
	 * @li filter.  A filter to select which file objects to copy. The filter is ignored if the source is a file.
	 * @li smash. Set to true, to replace an existing target file/directory with a source directory/file
	 * @li recursive. Used when source is a directory to indicate that directories should be copied recursively. 
	 *     Like -r/--recursive in POSIX cp.  
	 * 
	 * The following rules apply, if no filter is provided or if the source matches the provided filter
	 * 
	 * <table>
	 * <tr><th>From FileType</th><th>To FileType</th>
	 *                                    <th>Overwrite?</th>
	 *                                                <th>Smash?</th>
	 *                                                          <th>Recursive</th>
	 *                                                                        <th>Action</th></tr>
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
	 *   <td>FOLDER</td><td>FILE</td>     <td>No</td> <td>No</td> <td>No</td><td>Don't copy </td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FILE</td>     <td>Yes</td><td>No</td> <td>No</td><td>Don't copy </td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FILE</td>     <td>-</td>  <td>Yes</td><td>--</td><td>Replace file with folder </td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>No</td> <td>No</td> <td>No</td><td>Don't copy</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>No</td> <td>No</td> <td>Yes</td><td>Copy as a subfolder. Existing files will be skipped</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>Yes</td><td>No</td> <td>Yes</td><td>Copy as subfolder, replacing any same-named files along the way. </td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>--</td> <td>Yes</td><td>--</td><td>Replace existing folder and its contents with the content of the source folder </td>
	 * </tr>
	 * </table>
	 *    
	 */
	def cp ( Map properties=[:],from,to ) {
		assert properties != null

 		CopyMoveOperations.copy(
			resolveURI(properties,from),
			resolveURI(properties,to),
			properties.smash as boolean ?: false,
			properties.overwrite as boolean  ?: false,
			properties.recursive as boolean  ?: false,
			properties.filter
		)
	}

	/**
	 * @param from Source URI to move.
	 * @param to Source URI to move to 
	 * If destination is a file it will be replaced if overwrite property is true. 
	 * @param properties 
	 * @li overwrite. Set to true to force overwrite of an existing target. Set to false, not to allow overwrites. 
	 * Can also be a closure, in which case it needs to be of interface boolean(from,to).
	 * @li filter.  A filter to select which file objects to copy. The filter is ignored if the source is a file.
	 * @li smash. Set to true, to replace an existing target file/directory with a source directory/file.
     * @li intermediates. Default behaviour is to create non-existing intermediate subdirectories of target path. Set to 'false' if this behaviour is undesired.
	 * The following rules apply, if no filter is provided or if the source matches the provided filter
	 * 
	 * <table>
	 * <tr><th>From FileType</th><th>To FileType</th>
	 *                                    <th>Overwrite?</th>
	 *                                                <th>Smash?</th>
	 *                                                           <th>Action</th></tr>
	 * <tr>
	 *   <td>FILE</td>  <td>IMAGINARY</td><td>No</td> <td>No</td> <td>Create new file, delete old file</td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FILE</td>     <td>No</td> <td>No</td> <td>Don't move</td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FILE</td>     <td>Yes</td><td>No</td> <tdOverwirte existing file with source, delete old file></td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FOLDER</td>   <td>No</td> <td>No</td> <td>Move file into folder except if same-name target file exists</td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FOLDER</td>   <td>Yes</td><td>No</td> <td>Move file into folder, replacing any existing same-name target file</td>
	 * </tr><tr>
	 *   <td>FILE</td>  <td>FOLDER</td>   <td>-</td>  <td>Yes</td><td>Replace fodler with file</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>IMAGINARY</td><td>No</td> <td>No</td> <td>Create new folder with content. Delete old folder</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FILE</td>     <td>No</td> <td>No</td> <td>Don't move</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FILE</td>     <td>Yes</td><td>No</td> <td>Don't move</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FILE</td>     <td>-</td>  <td>Yes</td><td>Replace file with folder</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>No</td> <td>No</td> <td>Move folder as a sub-folder of destination. Fails if same-name target exists</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>Yes</td><td>No</td> <td>Move folder as a sub-folder of destination. Fails is same-name target exists and not empty.</td>
	 * </tr><tr>
	 *   <td>FOLDER</td><td>FOLDER</td>   <td>--</td> <td>Yes</td><td>Delete old folder. Move source folder in place.</td>
	 * </tr>
	 * </table>
	 */
	def mv ( Map properties=[:],from,to ) {
		assert properties != null
		
		CopyMoveOperations.move(
			resolveURI(properties,from),
			resolveURI(properties,to),
			properties.smash as boolean?: false,
			properties.overwrite as boolean ?: false,
            properties.intermediates as boolean ?: true
		)
	}

    /** Changes the default virtual file system options
     * 
     * @code
     * def vfs = new VFS()
     * 
     * vfs.options 'vfs.ftp.passiveMode' : true
     * 
     * vfs << {
     *   options {
     *     ftp {
     *      passiveMode  true
     *     }
     *   }  
     * }
     * 
     * @endcode
    */
    @CompileDynamic
    def options( Closure cfgDSL ) {
        defaultFSOptions = new ConfigDelegator( fsManager : fsMgr, fsOpts : defaultFSOptions ) .bind (cfgDSL)
    }
    
    /** Changes the default virtual file system options
     * 
     * @code
     * def vfs = new VFS()
     * 
     * vfs.options 'vfs.ftp.passiveMode' : true
     * 
     * vfs << {
     *   options 'vfs.ftp.passiveMode' : true
     * }
     * 
     * @endcode
    */
    def options( Map properties=[:] ) {
        defaultFSOptions = Util.buildOptions(properties, fsMgr, defaultFSOptions) as FileSystemOptions
    }

    /** Allows to additional scheme providers, operation providers, mime type maps and extension maps to be added
     *
     * @param providerDSL
     * @return
     */
    def extend( Closure providerDSL ) {
        new ProviderDelegator( fsManager : fsMgr ) .bind (providerDSL)
    }

    /** Returns a printable URI in which the password is masked
     *
     * @param uri
     * @return
     */
    def friendlyURI( FileObject uri ) {
		return uri.name.friendlyURI
	}

    /** Returns a printable URI in which the password is masked
     *
     * @param uri
     * @return
     */
	def friendlyURI( URI uri ) {
		return friendlyURI(resolveURI(uri))
	}

	/** Creates an unresolved {@code org.ysb33r.groovy.URI} object
	 *
	 * @param uriText
	 * @return URI
	 * @since 1.0
	 */
	URI uri(CharSequence uriText) {
		stageURI(uriText.toString())
	}

	/** Checks if URI is a file.
	 * This will resolve the URI on the virtual file system.
	 *
	 * @param uri
	 * @return {@code True} is URI is a folder
     *
     */
    boolean isFile(uri) {
        resolveURI(uri).type == FileType.FILE
    }

    /** Checks if URI is a folder.
	 * This will resolve the URI on the virtual file system.
	 *
	 * @param uri
	 * @return {@code True} is URI is a folder
     */
    boolean isFolder(uri) {
        resolveURI(uri).type == FileType.FOLDER
    }

    /** Checks to see if URI exists.
     * This will resolve the URI on the virtual file system.
	 *
	 * @param uri
	 * @return {@code True} is URI exists
     */
    boolean exists(uri) {
        resolveURI(uri).exists()
    }

	/** Checks to see if URI if local
	 * I
	 * @return {@code True} if URI is a local file URI.
	 * @since 1.0
	 */
	@CompileDynamic
	boolean local(uri) {
		Util.localURI(uri,fsMgr)
	}

	/** Returns the last modified time of a URI
     *
     * @param uri
     * @return Number of seconds since epoch
     * @throw FileSystemException if URI does not exist.
     */
    long mtime(uri) {
        try {
            resolveURI(uri).content.lastModifiedTime
        } catch( final org.apache.commons.vfs2.FileSystemException e) {
            throw new FileSystemException(e)
        }
    }

    /** Returns the logger instance that is used by this VFS
     *
     * @return
     */
	Log getLogger() {
		fsMgr.loggerInstance()
	}

	/** Resolves a URI.
	 * This involves locating it on the virtual file system and potential network traffic.
	 *
	 * @param properties
	 * @param uri
	 * @return An implementation dependent object
	 */
    @CompileDynamic
	FileObject resolveURI (Map properties=[:],uri) {
		if (uri instanceof FileObject) {
			properties.size() ?	Util.resolveURI(properties,fsMgr,uri.fileSystem.fileSystemOptions,uri.name.getURI()) : uri
		} else {
			Util.resolveURI(properties,fsMgr,defaultFSOptions,uri)
		} 
	}

	/** Stages a URI to see if it can be resolved, but does not locate it on the virtual file system.
	 * Does not invoke network traffic.
	 *
	 * @param properties
	 * @param uri
	 * @return An implementation dependent object
	 * @since 1.0
	 */
	@CompileDynamic
	URI stageURI (Map properties=[:],def uri) {
		URI u = new URI(uri)
		Util.validURI(u,fsMgr)
		u.addProperties(properties)
	}

	/** Returns the type of URI - file_uri, folder_uri or non_existent_uri
     *
     * @param uri
     * @return
     */
    private FileType type( URI uri ) {
        this.type(resolveURI(uri))
    }

    /** Returns the type of URI - file_uri, folder_uri or non_existent_uri
     *
     * @param uri
     * @return
     */
    private FileType type( FileObject uri ) {
        uri.type
    }

}
