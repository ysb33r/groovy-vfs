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
package org.ysb33r.nio.provider.ram.internal

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.time.Instant

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class Attributes implements BasicFileAttributes {

    final Entry entry

    Attributes(Entry entry) {
        this.entry = entry
    }

    /**
     * Returns the time of last modification.
     *
     * @return a {@code FileTime} representing the time the file was last
     *          modified
     */
    @Override
    FileTime lastModifiedTime() {
        entry ? modifiedTime : FileTime.from(Instant.EPOCH)
    }

    /**
     * Returns the time of last access. RAM file system does not support
     * last access time and will return last modified time.
     *
     *
     * @return a {@code FileTime} representing the time of last modified time
     */
    @Override
    FileTime lastAccessTime() {
        lastModifiedTime()
    }

    /**
     * Returns the creation time. The creation time is the time that the file
     * was created.
     *
     * <p> If the file system implementation does not support a time stamp
     * to indicate the time when the file was created then this method returns
     * an implementation specific default value, typically the {@link
     * # lastModifiedTime ( ) last-modified-time} or a {@code FileTime}
     * representing the epoch (1970-01-01T00:00:00Z).
     *
     * @return a {@code FileTime} representing the time the file was created
     */
    @Override
    FileTime creationTime() {
        entry ? creationTime : FileTime.from(Instant.EPOCH)
    }

    /**
     * Tells whether the file is a regular file with opaque content.
     *
     * @return {@code true} if the file is a regular file with opaque content
     */
    @Override
    boolean isRegularFile() {
        entry?.isFile()
    }

    /**
     * Tells whether the file is a directory.
     *
     * @return {@code true} if the file is a directory
     */
    @Override
    boolean isDirectory() {
        entry?.isDirectory()
    }

    /**
     * Tells whether the file is a symbolic link.
     *
     * @return {@code true} if the file is a symbolic link
     */
    @Override
    boolean isSymbolicLink() {
        false
    }

    /**
     * Tells whether the file is something other than a regular file, directory,
     * or symbolic link.
     *
     * @return {@code true} if the file something other than a regular file,
     *         directory or symbolic link
     */
    @Override
    boolean isOther() {
        entry == null
    }

    /**
     * Returns the size of the file (in bytes). The size may differ from the
     * actual size on the file system due to compression, support for sparse
     * files, or other reasons. The size of files that are not {@link
     * # isRegularFile regular} files is implementation specific and
     * therefore unspecified.
     *
     * @return the file size, in bytes
     */
    @Override
    long size() {
        entry.size()
    }

    /**
     * Returns an object that uniquely identifies the given file, or {@code
     * null} if a file key is not available. On some platforms or file systems
     * it is possible to use an identifier, or a combination of identifiers to
     * uniquely identify a file. Such identifiers are important for operations
     * such as file tree traversal in file systems that support <a
     * href="../package-summary.html#links">symbolic links</a> or file systems
     * that allow a file to be an entry in more than one directory. On UNIX file
     * systems, for example, the <em>device ID</em> and <em>inode</em> are
     * commonly used for such purposes.
     *
     * <p> The file key returned by this method can only be guaranteed to be
     * unique if the file system and files remain static. Whether a file system
     * re-uses identifiers after a file is deleted is implementation dependent and
     * therefore unspecified.
     *
     * <p> File keys returned by this method can be compared for equality and are
     * suitable for use in collections. If the file system and files remain static,
     * and two files are the {@link java.nio.file.Files#isSameFile same} with
     * non-{@code null} file keys, then their file keys are equal.
     *
     * @return an object that uniquely identifies the given file, or {@code null}
     *
     * @see java.nio.file.Files#walkFileTree
     */
    @Override
    Object fileKey() {
        return null
    }

    @PackageScope
    void setModifiedTime(FileTime) {

    }

    private final FileTime creationTime
    private FileTime modifiedTime = creationTime
}