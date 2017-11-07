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
package org.ysb33r.nio.provider.core

import groovy.transform.CompileStatic

import java.nio.file.FileSystem
import java.nio.file.FileSystemAlreadyExistsException

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
interface FileSystemRegistry {

    /** See if a filesystem related to the specific key exists
     *
     * @param key Filesystem key
     * @return {@code true} is filesystem exists for the given key.
     */
    boolean contains(final String key)

    /** Adds a filesystems if it does not exist.
     *
     * @param key Key to use to store filesystem
     * @param fs {@code FileSystem} instance.
     * @throw {@code FileSystemAlreadyExistsException} if a filesystem exists for the given key.
     */
    void add(final String key, FileSystem fs)

    /** Returns the filesystem that is associated with the specific key.
     *
     * @param key Key to use to retrieve filesystem
     * @return {@code FileSystem} instance.
     * @throw {@code FileSystemNotFoundException} is no filesystem is associated with the given key.
     */
    FileSystem get(final String key)
}