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
import org.ysb33r.nio.provider.core.SystemUtils
import org.ysb33r.nio.provider.ram.internal.RamFileData

import java.nio.file.FileStore
import java.nio.file.attribute.FileAttributeView
import java.nio.file.attribute.FileStoreAttributeView
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class RamFileStore extends FileStore {

    RamFileStore(Map<String,?> env,final String name) {
        this.name = name

        store = new RamFileData(
            blockSize: getPropertyValue(env,'blocksize',8192),
            maxBlocks : getPropertyValue(env,'maxblocks',3200)
        )
    }

    /**
     * Returns the name of this file store. The format of the name is highly
     * implementation specific. It will typically be the name of the storage
     * pool or volume.
     *
     * <p> The string returned by this method may differ from the string
     * returned by the {@link Object#toString() toString} method.
     *
     * @return the name of this file store
     */
    @Override
    @SuppressWarnings(['ConfusingMethodName'])
    String name() {
        this.name
    }

    /**
     * Returns the <em>type</em> of this file store. The format of the string
     * returned by this method is highly implementation specific. It may
     * indicate, for example, the format used or if the file store is local
     * or remote.
     *
     * @return a string representing the type of this file store
     */
    @Override
    String type() {
        ConcurrentHashMap.class.canonicalName
    }

    /**
     * Tells whether this file store is read-only. A file store is read-only if
     * it does not support write operations or other changes to files. Any
     * attempt to create a file, open an existing file for writing etc. causes
     * an {@code IOException} to be thrown.
     *
     * @return {@code true} if, and only if, this file store is read-only
     */
    @Override
    boolean isReadOnly() {
        false
    }

    /**
     * Returns the size, in bytes, of the file store.
     *
     * @return the size of the file store, in bytes
     *
     * @throws IOException
     *          if an I/O error occurs
     */
    @Override
    @SuppressWarnings('GetterMethodCouldBeProperty')
    long getTotalSpace() throws IOException {
        return -1
    }

    /**
     * Returns the number of bytes available to this Java virtual machine on the
     * file store.
     *
     * <p> The returned number of available bytes is a hint, but not a
     * guarantee, that it is possible to use most or any of these bytes.  The
     * number of usable bytes is most likely to be accurate immediately
     * after the space attributes are obtained. It is likely to be made inaccurate
     * by any external I/O operations including those made on the system outside
     * of this Java virtual machine.
     *
     * @return the number of bytes available
     *
     * @throws IOException
     *          if an I/O error occurs
     */
    @Override
    @SuppressWarnings('GetterMethodCouldBeProperty')
    long getUsableSpace() throws IOException {
        return -1
    }

    /**
     * Returns the number of unallocated bytes in the file store.
     *
     * <p> The returned number of unallocated bytes is a hint, but not a
     * guarantee, that it is possible to use most or any of these bytes.  The
     * number of unallocated bytes is most likely to be accurate immediately
     * after the space attributes are obtained. It is likely to be
     * made inaccurate by any external I/O operations including those made on
     * the system outside of this virtual machine.
     *
     * @return the number of unallocated bytes
     *
     * @throws IOException
     *          if an I/O error occurs
     */
    @Override
    @SuppressWarnings('GetterMethodCouldBeProperty')
    long getUnallocatedSpace() throws IOException {
        return -1
    }

    /**
     * Tells whether or not this file store supports the file attributes
     * identified by the given file attribute view.
     *
     * <p> Invoking this method to test if the file store supports {@link
     * BasicFileAttributeView} will always return {@code true}. In the case of
     * the default provider, this method cannot guarantee to give the correct
     * result when the file store is not a local storage device. The reasons for
     * this are implementation specific and therefore unspecified.
     *
     * @param type
     *          the file attribute view type
     *
     * @return {@code true} if, and only if, the file attribute view is
     *          supported
     */
    @Override
    boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        return false
    }

    /**
     * Tells whether or not this file store supports the file attributes
     * identified by the given file attribute view.
     *
     * <p> Invoking this method to test if the file store supports {@link
     * BasicFileAttributeView}, identified by the name "{@code basic}" will
     * always return {@code true}. In the case of the default provider, this
     * method cannot guarantee to give the correct result when the file store is
     * not a local storage device. The reasons for this are implementation
     * specific and therefore unspecified.
     *
     * @param name
     *          the {@link FileAttributeView#name name} of file attribute view
     *
     * @return {@code true} if, and only if, the file attribute view is
     *          supported
     */
    @Override
    boolean supportsFileAttributeView(String name) {
        return false
    }

    /**
     * Returns a {@code FileStoreAttributeView} of the given type.
     *
     * <p> This method is intended to be used where the file store attribute
     * view defines type-safe methods to read or update the file store attributes.
     * The {@code type} parameter is the type of the attribute view required and
     * the method returns an instance of that type if supported.
     *
     * @param type
     *          the {@code Class} object corresponding to the attribute view
     *
     * @return a file store attribute view of the specified type or
     * {@code null} if the attribute view is not available
     */
    @Override
    def <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
        return null
    }

    /**
     * Reads the value of a file store attribute.
     *
     * <p> The {@code attribute} parameter identifies the attribute to be read
     * and takes the form:
     * <blockquote>
     * <i>view-name</i><b>:</b><i>attribute-name</i>
     * </blockquote>
     * where the character {@code ':'} stands for itself.
     *
     * <p> <i>view-name</i> is the {@link FileStoreAttributeView#name name} of
     * a {@link FileStore AttributeView} that identifies a set of file attributes.
     * <i>attribute-name</i> is the name of the attribute.
     *
     * <p> <b>Usage Example:</b>
     * Suppose we want to know if ZFS compression is enabled (assuming the "zfs"
     * view is supported):
     * <pre>
     *    boolean compression = (Boolean)fs.getAttribute("zfs:compression");
     * </pre>
     *
     * @param attribute
     *          the attribute to read

     * @return the attribute value; {@code null} may be a valid valid for some
     *          attributes
     *
     * @throws UnsupportedOperationException
     *          if the attribute view is not available or it does not support
     *          reading the attribute
     * @throws IOException
     *          if an I/O error occurs
     */
    @Override
    Object getAttribute(String attribute) throws IOException {
        return null
    }

    RamFileData getRamFileData() {
        store
    }

    /** Returns a property value by looking a user supplied value, a system property or a compiled-in default.
     *
     * @param env Properties to find setting
     * @param name Name of property
     * @param defaultValue Default value in case poprerties of system property does not have setting
     * @return If property is supplied it is returned, otehrsie if found as a system property that is returned
     *   otherise the default value is returned.
     *
     * @throw CLassCastException if property of the given name does exist, but cannto be converted to {@b Integer}/
     */
    private static Integer getPropertyValue(final Map<String,?> env,final String name,final Integer defaultValue) {
        Integer sysProp = SystemUtils.getIntegerProperty("org.ysb33r.nio.provider.ram.{$name}")
        Integer envProp = env.hasProperty(name) ? env[name] : null
        envProp ?: (sysProp ?: defaultValue)
    }
    private final String name
    private final RamFileData store
}
