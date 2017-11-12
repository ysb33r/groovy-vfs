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
package org.ysb33r.vfs.core

import groovy.transform.CompileStatic

/** Logic for including or excluding files and folder during a traversal.
 *
 * @since 2.0
 */
@CompileStatic
interface FileSelector {

    /** Decide whether file or folder whould be included.
     *
     * @param fsi Information regarding an encountered file or folder.
     * @return {@code true} if file or folder should be included.
     */
    boolean include(final FileSelectInfo fsi)

    /** If the encountered entity is a folder this will be called to decide whether deeper
     * traversal should occur.
     *
     * @param fsi Information regarding an encountered  folder.
     * @return {@code true} is folder should be traversed.
     */
    boolean descend(final FileSelectInfo fsi)

    /** If the encountered item is symbolic link this will be called to decide whether to follow it.
     *
     * @param fsi Information regarding an encountered  folder.
     * @return {@code true} is symbolic link should be followed.
     */
    boolean follow(final FileSelectInfo fsi)
}