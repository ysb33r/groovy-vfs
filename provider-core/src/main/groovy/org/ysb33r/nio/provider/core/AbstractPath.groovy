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

import java.nio.file.Path

/** A base class for creating custom Path implementations.
 *
 */
@CompileStatic
abstract class AbstractPath implements Path {

    /** Masks out password when printing URI
     *
     * @return A string where the password has been replaced by a series of asterisks.
     */
    String friendlyURI() {
        UriUtils.friendlyURI(toUri())
    }

    /** Returns the character that is used to sperate path components.
     *
     * @return Separator charcater or null if the filesystem does not have concept of path separation
     */
    protected abstract String getSeparator()

    /** Returns the string that represent an alias for the current directory.
     *
     * @return Current directory alias or null if the filesystem does have a current directory alias string.
     */
    protected abstract String getCurrentDirAlias()

    /** Returns the string that represent an alias for the parent directory.
     *
     * @return Parent directory alias or null if the filesystem does have a parent directory alias string.
     */
    protected abstract String getParentDirAlias()

    /** Returns the root path against which relative paths might get resolved to absolute paths.
     * The algorithm for determining the root is highly implementatio-dependendant and may actually vary between
     * subsequent calls. An example of the latter is where the filesystem is dependent on a current working directory
     * and the latetr is changed between subsequent calls to this method.
     *
     * @return Root path or null if the filesystem does not support absolute roots.
     */
    protected abstract Path getResolvableRoot()


}
