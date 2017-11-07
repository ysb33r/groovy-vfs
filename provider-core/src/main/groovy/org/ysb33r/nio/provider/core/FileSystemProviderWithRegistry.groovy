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

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
interface FileSystemProviderWithRegistry {

    /** Given a URI, extract a value suitable for use as a key in a filesystem registry
     *
     * @param uri URI to a filesystem
     * @return Key
     */
    String getKey(final URI uri)

    /** PRovides access to a filesystem registry
     *
     * @return Instance of {@link FileSystemRegistry}
     */
    FileSystemRegistry getFileSystemRegistry()

}