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

import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileSystem
import java.nio.file.spi.FileSystemProvider

/** Provides a base for a {@code FileSystemProvider}
 *
 */
@CompileStatic
abstract class AbstractFileSystemProvider extends FileSystemProvider {

    String getScheme() {this.scheme}

    AbstractFileSystemProvider(final String scheme) {
        super()
        this.scheme = System.getProperty(getSchemeSystemPropertyName(scheme)) ?: scheme
    }

    /** Returns the section out of a URI that will represent a path on this filesystem
     *
     * @param uri
     * @return
     */
    protected String extractFilePath(final URI uri) {
        uri.path
    }

    /** Validates the URI whether it is valid within the context of this provider
     *
     * @param uri
     * @throw {@code IllegalArgumentException} if URI is not valid.
     */
    protected void validateURI(final URI uri) {
        if(uri.isOpaque()) {
            throw new IllegalArgumentException("Opaque URIs (${uri.scheme} not followed by //) are not allowed in this context.")
        }
        if(uri.scheme != scheme) {
            throw new IllegalArgumentException("Provided scheme '${uri.scheme}' is invalid in this context. Expected '${scheme}'.")
        }
    }

    /** Returns a property name which can be used to override the default scheme name.
     *
     * @param scheme Default name of scheme
     * @return System property name
     */
    protected String getSchemeSystemPropertyName(final String scheme) {
        "org.ysb33r.nio.provider.${scheme}.scheme"
    }

    private final String scheme
}
