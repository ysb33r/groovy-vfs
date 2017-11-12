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

/** Returns information regarding a file or folder that is encountered during a tree traversal.
 *
 * @since 2.0
 */
@CompileStatic
interface FileSelectInfo {

    /** The depth relative to the starting point.
     *
     * <p> If the starting point was a file, then this will always return 0.
     *
     * <p> If the starting point is a folder, this will return 0 for the starting point itself and
     * 1 for the immediate children. Recusrive traversal will increase this value.
     *
     * @return Traversal depth relatuive to start point.
     */
    int getDepth()

    /** The parent folder of the current file or folder.
     *
     * @return Parent folder. Can be null is the current item is the root of the filesystem.
     */
    VfsURI getParent()

    /** The current file or folder under consideration.
     *
     * @return Curretn file or folder.
     */
    VfsURI getCurrent()
}