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
package org.ysb33r.vfs.dsl.groovy

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.runtime.GStringImpl
import org.ysb33r.vfs.core.FileSelectInfo
import org.ysb33r.vfs.core.FileSelector
import org.ysb33r.vfs.core.FileSystemOptions
import org.ysb33r.vfs.core.Selectors
import org.ysb33r.vfs.core.URIException
import org.ysb33r.vfs.core.VfsEngine
import org.ysb33r.vfs.core.VfsURI
import org.ysb33r.vfs.core.AntPatternSelector
import org.ysb33r.vfs.dsl.groovy.impl.ConfigDelegator
import org.ysb33r.vfs.dsl.groovy.impl.DefaultFileContentEditor

import java.nio.file.Path
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.regex.Pattern

/**
 *
 * 
 * <pre>
 * import org.ysb33r.vfs.dsl.groovy.VFS
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
 * @since 0.1
 */
@CompileStatic
@Slf4j
class Vfs {


    /** A filter that selects all the descendants of the base folder, but does not select the base folder itself.
     *
      */
    final static FileSelector exclude_self = Selectors.EXCLUDE_SELF

    /** A filter that selects the base file/folder, plus all its descendants.
     *
     */
    final static FileSelector select_all = Selectors.SELECT_ALL

    /** A filter that selects only the direct children of the base folder.
     *
     */
    final static FileSelector direct_children_only = Selectors.CHILDREN_ONLY

    /** A filter that selects only files (not folders).
     *
      */
    final static FileSelector only_files = Selectors.FILES_ONLY

    /** A filter that selects only folders (not files).
     *
     */
    final static FileSelector only_folders	= Selectors.FOLDERS_ONLY

    /** A filter that select the base plus its direct descendants
     *
     */
    final static FileSelector self_and_direct_children = Selectors.CHILDREN_AND_SELF


	/** An overwrite policy that will only overwrite if the source is newer than the target
	 * @since 1.0
	 */
//	final static Closure onlyNewer = CopyMoveOperations.ONLY_NEWER

	/** Constructs a Virtual File System.
	 * 
	 * During construction a number of properties can be passed to the underlying Apache VFS system.
	 * <p>
     * <li> classLoader - ClassLoader
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
	Vfs(Map properties=[:]) {

        this.vfsEngine = properties.containsKey('classLoader') ? new VfsEngine(properties['classLoader'] as ClassLoader) : new VfsEngine()

        //        TemporaryFileStore tfs
        //        if(properties.containsKey('temporaryFileStore')) {
        //            switch(properties.temporaryFileStore) {
        //                case File:
        //                    tfs= Util.tempFileStoreFromPath(properties.temporaryFileStore as File)
        //                    break
        //
        //                case String:
        //                    tfs= Util.tempFileStoreFromPath(properties.temporaryFileStore as String)
        //                    break
        //
        //                case TemporaryFileStore:
        //                    tfs= properties.temporaryFileStore as TemporaryFileStore
        //                    break
        //
        //                default:
        //                    throw new FileSystemException('temporaryFileStore needs to be File/String/TemporaryFileStore')
        //            }
        //        }
        //
  	}

    /** Executes a sequence of operations on the same VFS
     * 
     * @param c
     * @return Whatever the closure returns.
     */
    def call ( final @DelegatesTo(Vfs) Closure c) {
        delegate(c).call()
    }
    
	/** List files or folders.
	 *
	 * @param properties
	 * @li filter A regex, closure of {@link FileSelectInfo} by which to select objects
	 * @li recursive If set to true will traverse down any subfolders. Ignored if filter is a FileSelector
     * @li followSymlinks Set to {@code false} to not follow symlinks. Default is to follow symbolic links.
	 *
	 * @param uri URI pointing to area for which file listing is to be retrieved
	 * @param c Closure that will be a single parameter ({@link VfsURI}). It should return {@code false} is traversal should be stopped.
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
	void ls ( final Map properties=[:], final String uri, final Closure c ) {
		ls(properties,new VfsURI(uri),c)
	}

    void ls (final Map properties=[:], final CharSequence uri, final Closure c ) {
		ls(properties,new VfsURI(uri),c)
	}

    void ls (final Map properties=[:], final URI uri, final Closure c ) {
		ls(properties,new VfsURI(uri),c)
	}

    void ls (final Map properties=[:], final URL uri, final Closure c ) {
        ls(properties,new VfsURI(uri),c)
    }

    void ls (final Map properties=[:], final Path uri, final Closure c ) {
		ls(properties,new VfsURI(uri),c)
	}

    void ls (final Map properties=[:], final File uri, final Closure c ) {
		ls(properties,new VfsURI(uri),c)
	}

    void ls ( final Map properties=[:], final VfsURI uri, Closure c ) {

        FileSelector selector = null
        int maxDepth = 1
        boolean followSymlinks = true

        if(properties.containsKey('recursive')) {
            maxDepth = ((Boolean)(properties['recursive'])) ? -1 : 1
        }

        if(properties.containsKey('followSymlinks')) {
            followSymlinks = (Boolean)(properties['followSymlinks'])
        }

        if(properties.containsKey('filter')) {
            switch(properties['filter']) {
                case FileSelector:
                    selector = (FileSelector)(properties['filter'])
                    break
                case Pattern:
                    selector = Selectors.byRegex(
                        (Pattern)(properties['filter']),
                        maxDepth,
                        followSymlinks
                    )
                    break
                case Closure:
					selector = [
						include : (Closure)(properties['filter']),
						descend : { FileSelectInfo fsi ->
                            -1 == maxDepth|| fsi.depth <= maxDepth
                        },
                        follow : followSymlinks
					] as FileSelector
                    break
                case Predicate:
                    selector = [
                        include : (Predicate<FileSelectInfo>)(properties['filter']),
                        descend : { FileSelectInfo fsi ->
                            -1 == maxDepth|| fsi.depth <= maxDepth
                        },
                        follow : followSymlinks
                    ] as FileSelector
                    break
                default:
                    selector = Selectors.byRegex(
                        ~/"${properties['filter'].toString()}"/,
                        maxDepth,
                        followSymlinks
                    )
            }
        }
        vfsEngine.ls(uri,delegate(c) as Predicate<VfsURI>,selector)

	}

    Iterable<VfsURI> ls(final Map properties=[:], final VfsURI uri) {
        Set<VfsURI> foundURIs = []

        Predicate<VfsURI> addItemsToContainer = { VfsURI encountered ->
            foundURIs.add(encountered)
            true
        } as Predicate<VfsURI>

        ls(properties,uri) { VfsURI encountered ->
            foundURIs.add(encountered)
            true
        }

        foundURIs
    }

    Iterable<VfsURI> ls(final Map properties=[:], final String uri) {
        ls(properties,new VfsURI(uri))
    }

    Iterable<VfsURI> ls(final Map properties=[:], final CharSequence uri) {
        ls(properties,new VfsURI(uri))
    }

    Iterable<VfsURI> ls(final Map properties=[:], final Path uri) {
        ls(properties,new VfsURI(uri))
    }

    Iterable<VfsURI> ls(final Map properties=[:], final File uri) {
        ls(properties,new VfsURI(uri))
    }

    Iterable<VfsURI> ls(final Map properties=[:], final URI uri) {
        ls(properties,new VfsURI(uri))
    }

    Iterable<VfsURI> ls(final Map properties=[:], final URL uri) {
        ls(properties,new VfsURI(uri))
    }


    /** Delete a file or folder on a filesystem.
     *
     * <p> Folders will not be deleted unless they are empty or {@code recursive} is set to {@code true}
     *
     * @param properties - Optional property map
     * @param uri
     * <ul>
     *     <li> recursive - {@code false} by default
     * </ul>
     */
    void rm(final Map properties=[:], final Object uri) {
        throw new NotActiveException("rm() needs implementing")
    }

    /** Delete a file or folder on a filesystem.
     *
     * <p> Folders will not be deleted unless they are empty or {@code recursive} is set to {@code true}
     *
     * @param properties - Optional property map
     * @param uri
     * <ul>
     *     <li> recursive - {@code false} by default
     * </ul>
     * @param ask A closure that can be used to interactively delete a file or not. It will be called for each file
     * found and for when directories are empty. It should return {@code true} if deletion should proceed.
     *
     * @throw FileSystemException if files cannot be deleted due to filesystem not supporting deletion or lack of user
     * permission.
     */
    void rm(final Map properties=[:], final Object uri, Consumer<VfsURI> ask) {
        throw new NotActiveException("rm() needs implementing")
    }

//    /**
//	 *
//	 * @param uri
//    *
//	 */
//	def ls ( Map properties=[:],uri ) { ls(properties,uri,null) }
	
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
	def cat ( Map properties=[:],final String uri,Closure c ) {
//		assert properties != null
//        assert c != null
//
//		if( uri instanceof FileObject && !properties.size()) {
//			return uri.content.inputStream.withStream(c)
//		} else {
//			FileObject fo=resolveURI(properties,uri)
//			AbstractFileSystem afs= fo.fileSystem as AbstractFileSystem
//
//			try {
//				return fo.content.inputStream.withStream(c)
//			}
//			finally {
//				afs.closeCommunicationLink()
//			}
//
//		}
	}

    def cat (final Map properties=[:], final java.net.URI uri, Closure c ) {
        null
    }

    def cat (final Map properties=[:], final VfsURI uri, Closure c ) {
        null
    }

    def cat (final Map properties=[:], final Path uri, Closure c ) {
        null
    }

    def cat (final Map properties=[:], final File uri, Closure c ) {
        null
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
	FileContentEditor overwrite( final String uri  ) {
//		assert uri != null
//		new DefaultFileContentEditor( resolveURI(uri),false )
	}

    FileContentEditor overwrite( final URI uri  ) {
        null
    }

    FileContentEditor overwrite( final VfsURI uri  ) {
        null
    }

    FileContentEditor overwrite( final Path uri  ) {
        null
    }

    FileContentEditor overwrite( final File uri  ) {
        null
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
	def overwrite( final String uri, Closure c ) {
//		assert uri != null
//		new FileContentEditor( resolveURI(uri),false ).with(c)
	}

    def overwrite( final URI uri, Closure c  ) {
        null
    }

    def  overwrite( final VfsURI uri, Closure c  ) {
        null
    }

    def  overwrite( final Path uri, Closure c  ) {
        null
    }

    def  overwrite( final File uri, Closure c  ) {
        null
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
	FileContentEditor append ( final String uri  ) {
//		assert uri != null
//		new FileContentEditor( resolveURI(uri), true )
	}

    FileContentEditor append( final URI uri  ) {
        null
    }

    FileContentEditor append( final VfsURI uri  ) {
        null
    }

    FileContentEditor append( final Path uri  ) {
        null
    }

    FileContentEditor append( final File uri  ) {
        null
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
	def append( final String uri, Closure c ) {
		assert uri != null
		new DefaultFileContentEditor( resolveURI(uri),true ).with(c)
	}

    def append( final URI uri, Closure c  ) {
        null
    }

    def append( final VfsURI uri, Closure c  ) {
        null
    }

    def append( final Path uri, Closure c  ) {
        null
    }

    def append( final File uri, Closure c  ) {
        null
    }


    /** Creates a folder on any VFS that allows this functionality
	 * @param properties Any additional vfs properties
     * @li intermediates. Set to false if intermediate folders should not be created.
	 * @param uri Folder that needs to be created
	 */
	void mkdir ( Map properties=[:],final String uri ) {
        throw new NotActiveException("mkdir needs implementing")
//		assert properties != null
//		def fo= resolveURI(properties,uri)
//        if (properties.intermediates != null && properties.intermediates==false) {
//            def parent = fo.parent
//            if(!parent.exists()) {
//                throw new FileActionException("Cannot create directory - '${friendlyURI(parent)}' does not exist and intermediates==false")
//            }
//        }
//        fo.createFolder()
	}

    void mkdir ( Map properties=[:],final URI uri ) {
        throw new NotActiveException("mkdir needs implementing")
    }

    void mkdir ( Map properties=[:],final VfsURI uri ) {
        throw new NotActiveException("mkdir needs implementing")
    }
    void mkdir ( Map properties=[:],final Path uri ) {
        throw new NotActiveException("mkdir needs implementing")
    }

    void mkdir ( Map properties=[:],final File uri ) {
        throw new NotActiveException("mkdir needs implementing")
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
	void cp ( Map properties=[:],Object from, Object to ) {
//		assert properties != null
//
// 		CopyMoveOperations.copy(
//			resolveURI(properties,from),
//			resolveURI(properties,to),
//			properties.smash as boolean ?: false,
//			properties.overwrite as boolean  ?: false,
//			properties.recursive as boolean  ?: false,
//			properties.filter
//		)
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
	void mv ( Map properties=[:],Object from, Object to ) {
//		assert properties != null
//
//		CopyMoveOperations.move(
//			resolveURI(properties,from),
//			resolveURI(properties,to),
//			properties.smash as boolean?: false,
//			properties.overwrite as boolean ?: false,
//            properties.intermediates as boolean ?: true
//		)
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
    Vfs options(final Closure cfgDSL ) {
        ConfigDelegator cd = new ConfigDelegator(vfsEngine.fileSystemOptions)
        cd.bind(cfgDSL)
        this
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
    Vfs options(final Map<String,?> properties=[:] ) {
        vfsEngine.fileSystemOptions.addAll(properties)
        this
    }

	/** Creates an ANT-style pattern filter.
	 *
	 * @param cfg
	 * @return
     *
	 * @code
	 * def vfs = new VFS()
	 *
	 *
	 *
	 * vfs {
	 *   cp 'ftp://server1/path1/path2', 'sftp://server2/path3',
	 *     overwrite: true, recursive: true,
	 *     filter : antPattern {
	 *         include '**'
	 *         exclude 'foo.txt'
	 *     }
	 * }
	 *
	 * @endcode
	 */
	AntPatternSelector antPattern(Closure cfg) {
		AntPatternSelector aps = new AntPatternSelector()
		Closure c1 = (Closure)(cfg.clone())
        c1.delegate = aps
        c1.resolveStrategy = Closure.DELEGATE_FIRST
        c1.call()
		aps
	}

    /** Returns a printable URI in which the password is masked
     *
     * @param uri
     * @return
     */
//    def friendlyURI( FileObject uri ) {
//		return uri.name.friendlyURI
//	}

    /** Returns a printable URI in which the password is masked
     *
     * @param uri
     * @return
     */
	CharSequence friendlyURI(VfsURI uri ) {
		return friendlyURI(resolveURI(uri))
	}

//	/** Creates an unresolved {@code org.ysb33r.groovy.URI} object
//	 *
//	 * @param uriText
//	 * @return URI
//	 * @since 1.0
//	 */
//	VfsURI uri(CharSequence uriText) {
//		stageURI(uriText.toString())
//	}
//
	/** Checks if URI is a file.
	 * This will resolve the URI on the virtual file system.
	 *
	 * @param uri
	 * @return {@code True} is URI is a folder
     *
     */
    boolean isFile(final Object uri) {
        throw new NotActiveException("isFile needs implementing")
//        resolveURI(uri).type == FileType.FILE
    }

    /** Checks if URI is a folder.
	 * This will resolve the URI on the virtual file system.
	 *
	 * @param uri
	 * @return {@code True} is URI is a folder
     */
    boolean isFolder(final Object uri) {
        throw new NotActiveException("isFolder() needs implementing")
//        resolveURI(uri).type == FileType.FOLDER
    }

    /** Checks to see if URI exists.
     * This will resolve the URI on the virtual file system.
	 *
	 * @param uri
	 * @return {@code True} is URI exists
     */
    boolean exists(final Object uri) {
        throw new NotActiveException("exists() needs implementing")
//        resolveURI(uri).exists()
    }

	/** Checks to see if URI if local
	 * I
	 * @return {@code True} if URI is a local file URI.
	 * @since 1.0
	 */
	boolean local(final Object uri) {
        throw new NotActiveException("local() needs implementing")
//		Util.localURI(uri,fsMgr)
	}

	/** Returns the last modified time of a URI
     *
     * @param uri
     * @return Number of seconds since epoch
     * @throw FileSystemException if URI does not exist.
     */
    long mtime(final Object uri) {
        throw new NotActiveException("mtime() needs implementing")
//        try {
//            resolveURI(uri).content.lastModifiedTime
//        } catch( final org.apache.commons.vfs2.FileSystemException e) {
//            throw new FileSystemException(e)
//        }
    }

    /** Returns the logger instance that is used by this VFS
     *
     * @return
     */
//	 getLogger() {
//		fsMgr.loggerInstance()
//	}

	/** Resolves a URI.
	 * This involves locating it on the virtual file system and potential network traffic.
	 *
	 * @param properties
	 * @param uri
	 * @return An implementation dependent object
	 */
	VfsURI resolveURI (Map properties=[:],Object uri) {
        throw new URIException('',"Needs URI implementation")
        switch(uri) {
            case String:
                break
            case CharSequence:
                break
            case GStringImpl:
                break
            case URL:
                break
            case URI:
                break
            case VfsURI:
                return (VfsURI)uri
            case Path:
                break
            case File:
                break
            default:
                throw new URIException(uri.toString(),"Object of type ${uri.class.name} is not suitable for resolving as URI.")
        }
//		if (uri instanceof FileObject) {
//			if( properties.size() ) {
//				Map vfsProperties = Util.selectVfsOptions(properties)
//				vfsProperties.size() ?	Util.resolveURI(vfsProperties,fsMgr,uri.fileSystem.fileSystemOptions,uri.name.getURI()) : uri
//			} else {
//				return uri
//			}
//		} else {
//			Util.resolveURI(properties,fsMgr,defaultFSOptions,uri)
//		}
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
    @Deprecated
	VfsURI stageURI (Map properties=[:], Object uri) {
        resolveURI(properties,uri)
	}

	/** Enquires whether the URI is on a file system that is capable of listing the contents of folder content.
	 *
	 * @param uri
	 * @return {@code true} is children of folders can be listed.
	 * @since 1.0
	 */
	boolean fsCanListFolderContent (uri) {
        throw new NotActiveException("fsCanListFolderContent() needs implementing")
//		resolveURI(uri).fileSystem.hasCapability(Capability.LIST_CHILDREN)
	}

	/** Returns the type of URI - file_uri, folder_uri or non_existent_uri
     *
     * @param uri
     * @return
     */
    private FileType type(VfsURI uri ) {
        throw new NotActiveException("type() needs implementing")
    }

    private Closure delegate(Closure c) {
        Closure newc=(Closure)(c.clone())
        newc.delegate= this
        newc.resolveStrategy = Closure.DELEGATE_FIRST
        newc
    }

    /** Returns the type of URI - file_uri, folder_uri or non_existent_uri
     *
     * @param uri
     * @return
     */
//    private FileType type( FileObject uri ) {
//        uri.type
//    }

//    private StandardFileSystemManager fsMgr
    private final VfsEngine vfsEngine

}
