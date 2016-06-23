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

/** A collection of utilities to work with URIs
 *
 */
@CompileStatic
class UriUtils {
    /** Returns a string with the password filtered out
     *
     * @param uri URI to print
     * @return
     */
    static String friendlyURI(final URI uri) {
        if(uri.userInfo?.size()) {
            String[] parts = uri.userInfo.split(':',2)
            if(parts.size() ==1) {
                new URI(uri.scheme,parts[0],uri.host,uri.port,uri.path,uri.query,uri.fragment).toString()
            } else {
                new URI(uri.scheme,"${parts[0]}:*****",uri.host,uri.port,uri.path,uri.query,uri.fragment).toString()
            }
        } else {
            uri.toString()
        }
    }
}
