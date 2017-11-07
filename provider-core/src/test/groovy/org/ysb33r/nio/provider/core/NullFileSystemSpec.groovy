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

import org.ysb33r.nio.provider.core.helpers.NullFileSystemProvider
import spock.lang.Specification
import java.nio.file.FileSystems
import java.nio.file.FileSystem


class NullFileSystemSpec extends Specification {

    def "Create a null filesystem for experimentation purpose only"() {
        given: "When I have a URI on my null filesystem"
        def nullURI = 'null:null.txt'.toURI()

        when: "I load a null filesystem via Files"
        FileSystem fs = FileSystems.newFileSystem(nullURI, [ key1 : 'value1' ])

        then: "I expect no exception to be thrown"
        fs != null
        fs.provider() instanceof NullFileSystemProvider

        when: ""
        NullFileSystemProvider fsp = (NullFileSystemProvider)fs.provider()
        println fsp.callsMade

        then:
        fsp.callsMade.containsKey('newFileSystem')
    }
}