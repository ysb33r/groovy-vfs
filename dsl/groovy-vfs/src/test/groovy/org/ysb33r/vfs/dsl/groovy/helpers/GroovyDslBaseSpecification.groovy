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
package org.ysb33r.vfs.dsl.groovy.helpers

import org.ysb33r.vfs.dsl.groovy.Vfs
import spock.lang.Specification

class GroovyDslBaseSpecification extends Specification {

    static final File testFsReadOnlyRoot =      new File(
        System.getProperty('TESTFSREADROOT') ?: 'core/src/test/resources/test-files'
    ).absoluteFile
    static private final File testFsWriteRoot = new File(
        System.getProperty('TESTFSWRITEROOT') ?: 'dsl/groovy-vfs/build/tmp/test-files',
        'groovy-vfs'
    ).absoluteFile

    void setup() {
        if(testFsWriteRoot.exists()) {
            testFsWriteRoot.deleteDir()
        }
        testFsWriteRoot.mkdirs()
    }

    Vfs setupVfs() {
        VfsBuilder.build('Standard Vfs Test')
    }

}