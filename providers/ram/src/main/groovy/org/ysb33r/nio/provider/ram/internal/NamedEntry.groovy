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

/**
 * @author Schalk W. Cronjé
 */
interface NamedEntry extends Entry {
    /** The name of a current entry.
     */
    String getName()

    /** Container that is entry belongs to
     *
     * @return Container that entry belongs to or null if there is no container associated with it.
     */
    RamFileData getContainer()
}