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
import groovy.transform.TailRecursive
import org.ysb33r.nio.provider.ram.RamFilePath

import java.nio.ByteBuffer
import java.nio.file.FileAlreadyExistsException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

/** A RAM Filesystem is represented as a concurrent hash map. They key to the
 * map is the name of the file. The value in the map is tuple of attributes and
 * an object representing a file (as an byte array) or a directory (as another hashmap).
 *
 */
@CompileStatic
class RamFileData {

    final Integer blockSize
    final Integer maxBlocks

    ConcurrentHashMap<String,Entry> directory = new ConcurrentHashMap<String,Entry>()
    Attributes attributes = new Attributes(null)

    /** Locates entry by name
     *
     * @param name Item to retrieve
     * @return Item if found, else null
     */
    Entry getByName(final String name) {
        directory[name]
    }

    Entry createFile(final String name) {
        if(name.empty) {
            throw new IOException('Filename cannot be empty')
        }
        if(directory.hasProperty(name)) {
            throw new FileAlreadyExistsException("File '${name}' exists.")
        }
        File entry = new File( blockSize: this.blockSize, maxBlocks : this.maxBlocks)
        directory[name] = entry
        directory[name]
    }

    /** Looks for an entry by walking the tree
     *
     * @param path Normalized Path object to use
     * @return Entry if found. Returns null if path is an empty object.
     * @throw {@code java.io.IOException} if not found
     */
    Entry locateEntryFromPath(final RamFilePath path) {
        locateEntryFromPath(path,0)
    }

    /** Looks for an entry by walking the tree.
     *
     * @param path Normalized Path object to use
     * @param index Index in path at which to start looking
     * @return Entry if found. Returns null if path is an empty object.
     * @throw {@code java.io.IOException} if not found
     */
    Entry locateEntryFromPath(final RamFilePath path,int index) {
        Entry container = locateContainerByPath(path,index)?.container.getByName(
            path.getName(path.nameCount-1).toString()
        )
//        (Entry)(container?.directory[container.name])
    }

    /** Looks for the container of the path by walking the tree.
     *
     * @param path Normalized Path object to use
     * @param index Index in path at which to start looking
     * @return Container if found. Returns null if path is an empty object.
     * @throw {@code java.io.IOException} if not found
     */
    @TailRecursive
    NamedEntry locateContainerByPath(final RamFilePath path,int index) {
        if(!path.nameCount) {
            null
        } else {
            final String NAME = path.getName(index).toString()
            if(directory.hasProperty(NAME)) {
                boolean moreSegments = path.nameCount - 1 > index
                if(moreSegments && directory[NAME].directory) {
                    return locateContainerByPath(path,index+1)
                } else if(moreSegments && directory[NAME].file) {
                    throw new IOException("Found a file where a directory was expected: ${path}")
                } else {
                    return new DefaultNamedEntry( name : NAME, container : this )
                }
            } else {
                throw new IOException("Path ${path} lookup failed at index ${index}")
            }
        }
    }

    /** Check if path exists in map.
     *
     * @param path Path to walk
     * @return {@code true} if path has been found.
     */
    boolean exists(final RamFilePath path) {
        try {
            return locateEntryFromPath(path) != null
        } catch(IOException) {
            return false
        }
    }

    static class Directory implements Entry {
        boolean isFile() {false}
        boolean isDirectory() {true}
        long size() {0}

        Attributes attributes = new Attributes(this)
        RamFileData entries
    }

}
