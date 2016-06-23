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

import spock.lang.Specification


/**
 * @author Schalk W. Cronjé
 */
class RamFileDataSpec extends Specification {

    def "Manipulating a filestore"() {
        given: 'A basic ramfile store'
        RamFileData root = new RamFileData()

        when: 'I create a file in the root'
        root.createFile 'index.txt'

        then: "I expect an entry"
        root.directory.size() == 1
        root.directory['index.txt']?.isFile()
        !root.directory['index.txt'].isDirectory()

        and: 'The file should have zero size'
        root.directory['index.txt'].size() == 0
    }
}