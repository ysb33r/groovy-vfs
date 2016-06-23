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
package org.ysb33r.nio.provider.ram

import spock.lang.Specification

import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.spi.FileSystemProvider


class RamFileSystemProviderSpec extends Specification {

    def "RAM Provider is loaded"() {
        expect:
        FileSystemProvider.installedProviders().find { it?.scheme == 'ram' && it instanceof RamFileSystemProvider }
        !FileSystemProvider.installedProviders().find { it?.scheme == 'foobiedoobiedooda' }
    }

    def "Creating a RAM filesystem"() {
        given: "When I have a URI on my RAM filesystem"
        URI uri = 'ram://test'.toURI()

        when: "I load a RAM filesystem via Files"
        FileSystem fs = FileSystems.newFileSystem(uri, [:])
        Path targetFile = fs.getPath('foo')
        Path targetFile2 = Paths.get('ram://test/foo'.toURI())

        then: "The filesystem is expected to be RamFileSystem "
        fs != null
        fs.isOpen()
        fs instanceof RamFileSystem
        fs.provider() instanceof RamFileSystemProvider
        targetFile.fileSystem.provider() == fs.provider()
        targetFile2.fileSystem.provider() == fs.provider()
    }

    def "Creating a new file on a RAM filesystem"() {
        given: "A RAM filesystem is loaded"
        URI uri = 'ram://test/targetFile'.toURI()
        FileSystem fs = FileSystems.newFileSystem(uri, [:])
        Path targetFile = Paths.get(uri)

        when: "A file is created"
        Path newFile = Files.createFile(targetFile)

        then: "The file must exist and of zero length"
        Files.exists(newFile)
        Files.size(newFile) == 0
        !Files.isDirectory(newFile)
        Files.isRegularFile(newFile)
    }
}