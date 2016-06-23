/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2016
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
package org.ysb33r.nio.provider.ram

import groovy.transform.CompileStatic
import org.ysb33r.nio.provider.core.AbstractPosixPath
import org.ysb33r.nio.provider.ram.internal.Entry

import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class RamFilePath extends AbstractPosixPath<RamFileSystem> {

    /** Constructs a Path that is linked to a RAM filesystem
     *
     * @param fs active {@link RamFileSystem}
     * @param authority Authority string. this is the first part following the ram:// scheme.
     * @param base Base path
     * @param more Additional segments.
     */
    RamFilePath(RamFileSystem fs,final String authority, String base,String... more) {
        super(fs,authority,base,more)
    }

    /** Constructs an empty Path that is linked to a RAM filesystem
     *
     * @param fs active {@link RamFileSystem}
     * @param authority Authority string. this is the first part following the ram:// scheme.
     */
    RamFilePath(RamFileSystem fs, final String authority) {
        super(fs,authority)
    }

    /** Create a path  instance from one or more segments.
     *
     * @param part Base part of the path
     * @param more Optional additional segments
     * @return A new path type suitable for filesystem of {@code T}.
     */
    @Override
    protected Path createPath(String part, String... more) {
        new RamFilePath((RamFileSystem)fileSystem,authority,part,more)
    }

    /** Creates an empty path suitable for manipulation of the given {@code T} filesystem.
     *
     * @return A new path type suitable for filesystem of {@code T}.
     */
    @Override
    protected Path createPath() {
        new RamFilePath((RamFileSystem)fileSystem,authority)
    }

    /**
     * Returns the <em>real</em> path of an existing file.
     *
     * <p> The precise definition of this method is implementation dependent but
     * in general it derives from this path, an {@link #isAbsolute absolute}
     * path that locates the {@link Files#isSameFile same} file as this path, but
     * with name elements that represent the actual name of the directories
     * and the file. For example, where filename comparisons on a file system
     * are case insensitive then the name elements represent the names in their
     * actual case. Additionally, the resulting path has redundant name
     * elements removed.
     *
     * <p> If this path is relative then its absolute path is first obtained,
     * as if by invoking the {@link #toAbsolutePath toAbsolutePath} method.
     *
     * <p> The {@code options} array may be used to indicate how symbolic links
     * are handled. By default, symbolic links are resolved to their final
     * target. If the option {@link LinkOption#NOFOLLOW_LINKS NOFOLLOW_LINKS} is
     * present then this method does not resolve symbolic links.
     *
     * Some implementations allow special names such as "{@code ..}" to refer to
     * the parent directory. When deriving the <em>real path</em>, and a
     * "{@code ..}" (or equivalent) is preceded by a non-"{@code ..}" name then
     * an implementation will typically cause both names to be removed. When
     * not resolving symbolic links and the preceding name is a symbolic link
     * then the names are only removed if it guaranteed that the resulting path
     * will locate the same file as this path.
     *
     * @param options Ignored. RAM filesystem do not support links.
     *
     * @return an absolute path represent the <em>real</em> path of the file
     *          located by this object
     *
     * @throws IOException
     *          if the file does not exist or an I/O error occurs
     */
    @Override
    Path toRealPath(LinkOption... options) throws IOException {
        RamFilePath path = (RamFilePath)toAbsolutePath()
        RamFileStore store = (RamFileStore)(fileSystem.fileStores.iterator().next())

        if(null == store.ramFileData.locateEntryFromPath(path)) {
            throw new IOException("Empty path")
        }
        path
    }

    /**
     * Registers the file located by this path with a watch service.
     *
     * <p> In this release, this path locates a directory that exists. The
     * directory is registered with the watch service so that entries in the
     * directory can be watched. The {@code events} parameter is the events to
     * register and may contain the following events:
     * <ul>
     *   <li>{@link StandardWatchEventKinds#ENTRY_CREATE ENTRY_CREATE} -
     *       entry created or moved into the directory</li>
     *   <li>{@link StandardWatchEventKinds#ENTRY_DELETE ENTRY_DELETE} -
     *        entry deleted or moved out of the directory</li>
     *   <li>{@link StandardWatchEventKinds#ENTRY_MODIFY ENTRY_MODIFY} -
     *        entry in directory was modified</li>
     * </ul>
     *
     * <p> The {@link WatchEvent#context context} for these events is the
     * relative path between the directory located by this path, and the path
     * that locates the directory entry that is created, deleted, or modified.
     *
     * <p> The set of events may include additional implementation specific
     * event that are not defined by the enum {@link StandardWatchEventKinds}
     *
     * <p> The {@code modifiers} parameter specifies <em>modifiers</em> that
     * qualify how the directory is registered. This release does not define any
     * <em>standard</em> modifiers. It may contain implementation specific
     * modifiers.
     *
     * <p> Where a file is registered with a watch service by means of a symbolic
     * link then it is implementation specific if the watch continues to depend
     * on the existence of the symbolic link after it is registered.
     *
     * @param watcher
     *          the watch service to which this object is to be registered
     * @param events
     *          the events for which this object should be registered
     * @param modifiers
     *          the modifiers, if any, that modify how the object is registered
     *
     * @return a key representing the registration of this object with the
     *          given watch service
     *
     * @throws UnsupportedOperationException
     *          if unsupported events or modifiers are specified
     * @throws IllegalArgumentException
     *          if an invalid combination of events or modifiers is specified
     * @throws ClosedWatchServiceException
     *          if the watch service is closed
     * @throws NotDirectoryException
     *          if the file is registered to watch the entries in a directory
     *          and the file is not a directory  <i>(optional specific exception)</i>
     * @throws IOException
     *          if an I/O error occurs
     * @throws SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     */
    @Override
    WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("Watcher not implemented")
    }

    /**
     * Registers the file located by this path with a watch service.
     *
     * <p> An invocation of this method behaves in exactly the same way as the
     * invocation
     * <pre>
     *     watchable.{@link #register(WatchService, WatchEvent.Kind [ ], WatchEvent.Modifier [ ]) register}(watcher, events, new WatchEvent.Modifier[0]);
     * </pre>
     *
     * <p> <b>Usage Example:</b>
     * Suppose we wish to register a directory for entry create, delete, and modify
     * events:
     * <pre>
     *     Path dir = ...
     *     WatchService watcher = ...
     *
     *     WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
     * </pre>
     * @param watcher
     *          The watch service to which this object is to be registered
     * @param events
     *          The events for which this object should be registered
     *
     * @return A key representing the registration of this object with the
     *          given watch service
     *
     * @throws UnsupportedOperationException
     *          If unsupported events are specified
     * @throws IllegalArgumentException
     *          If an invalid combination of events is specified
     * @throws ClosedWatchServiceException
     *          If the watch service is closed
     * @throws NotDirectoryException
     *          If the file is registered to watch the entries in a directory
     *          and the file is not a directory  <i>(optional specific exception)</i>
     * @throws IOException
     *          If an I/O error occurs
     * @throws SecurityException
     *          In the case of the default provider, and a security manager is
     *          installed, the {@link SecurityManager#checkRead(String) checkRead}
     *          method is invoked to check read access to the file.
     */
    @Override
    WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException("Watcher not implemented")
    }

}
