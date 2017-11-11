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
// ============================================================================
// (C) Copyright Schalk W. Cronje 2013
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================

package org.ysb33r.vfs.core.helpers

import org.ysb33r.vfs.core.Selectors
import org.ysb33r.vfs.core.VfsEngine
import org.ysb33r.vfs.core.VfsURI

import java.util.function.Predicate


final class ListFolderTestHelper {
    static def assertListable = { VfsEngine vfs, VfsURI rootUrl ->
        Map<String,Integer> listing = [:]
        Predicate<VfsURI> apply = { VfsURI it ->
            listing."${it.name}"= 1
        } as Predicate<VfsURI>

        vfs.ls (rootUrl,apply,Selectors.SELECT_ALL)

        assert listing.'file1.txt' == 1
        assert listing.'file2.txt' == 1
        assert listing.'test-subdir' == 1
        
        true
    }
}

