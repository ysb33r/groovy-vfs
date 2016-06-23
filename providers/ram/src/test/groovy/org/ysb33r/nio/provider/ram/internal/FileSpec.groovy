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

import java.nio.ByteBuffer

/**
 * @author Schalk W. Cronjé
 */
class FileSpec extends Specification {

    static final int TEST_BLOCKSIZE = 10
    static final int TEST_MAXBLOCKS = 9

    File testFile = new File(blockSize : TEST_BLOCKSIZE, maxBlocks : TEST_MAXBLOCKS)

    def "RAM file implementation deque"() {
        expect: "Default settings"
        testFile.blockSize == TEST_BLOCKSIZE
        testFile.maxBlocks == TEST_MAXBLOCKS
        testFile.file == true
        testFile.directory == false
        testFile.size() == 0
    }

    def "Calculate number of blocks required"() {
        given: "A blocksize of #TEST_BLOCKSIZE and an empty file"
        0 // NOOP

        expect:
        requiredBlocks == testFile.newBlocksRequired(offset,writeSize)

        where:
        offset | writeSize || requiredBlocks
        0      | 10        || 1
        0      | 5         || 1
        0      | 11        || 2
        0      | 12        || 2
    }

    def "Writing the same number of bytes as the blocksize at the beginning of a brand new file"() {
        given: "An array of bytes"
        def inputArray = [1,2,3,4,5,6,7,8,9,10] as byte[]

        when: "Bytes are written at the start"
        int bytesWritten = testFile.write(inputArray,0)

        then: "One block will be created"
        testFile.data.size() == 1

        and: "Number of bytes written will be same as the input buffer size"
        bytesWritten == inputArray.size()

        and: "The size of the file will be the same as the number of bytes written"
        testFile.size() == inputArray.size()
        testFile.currentOffset == inputArray.size()

        and: "Last block offset will be same as block size"
        testFile.lastBlockOffset == inputArray.size()
    }

    def "Writing smaller number of bytes as the blocksize at the beginning of a brand new file"() {
        given: "An array of bytes"
        def inputArray = [1,2,3,4,5] as byte[]

        when: "Bytes are written at the start"
        int bytesWritten = testFile.write(inputArray,0)

        then: "One block will be created"
        testFile.data.size() == 1

        and: "Number of bytes written will be same as the input buffer size"
        bytesWritten == inputArray.size()

        and: "The size of the file will be the same as the number of bytes written"
        testFile.size() == inputArray.size()
        testFile.currentOffset == inputArray.size()

        and: "Last block offset will be same as number of bytes written"
        testFile.lastBlockOffset == inputArray.size()
    }

    def "Writing number of bytes just larger as the blocksize at the beginning of a brand new file"() {
        given: "An array of bytes"
        def inputArray = [1,2,3,4,5,6,7,8,9,10,11] as byte[]

        when: "Bytes are written at the start"
        int bytesWritten = testFile.write(inputArray,0)

        then: "Two blocks will be created"
        testFile.data.size() == 2

        and: "Number of bytes written will be same as the input buffer size"
        bytesWritten == inputArray.size()

        and: "The size of the file will be the same as the number of bytes written"
        testFile.size() == inputArray.size()
        testFile.currentOffset == inputArray.size()

        and: "Last block offset will be second position within second block"
        testFile.lastBlockOffset == 1
    }

    def "Twice writing number of bytes just smaller as the blocksize at the beginning of a brand new file"() {
        given: "An array of bytes"
        def inputArray = [1,2,3,4,5,6] as byte[]

        when: "Bytes are written at the start"
        int bytesWritten = testFile.write(inputArray,0) + testFile.write(inputArray,inputArray.size())

        then: "Two blocks will be created"
        testFile.data.size() == 2

        and: "Number of bytes written will be twice the input buffer size"
        bytesWritten == inputArray.size() * 2

        and: "The size of the file will be the same as the number of bytes written"
        testFile.size() == inputArray.size() * 2
        testFile.currentOffset == inputArray.size() * 2

        and: "Last block offset will be third position within second block"
        testFile.lastBlockOffset == 2
    }

    def "Overwriting number of bytes just smaller as the blocksize at the beginning of a brand new file"() {
        given: "An array of bytes"
        def inputArray1 = [1,2,3,4,5,6] as byte[]
        def inputArray2 = [7,8,9] as byte[]

        when: "Bytes are written at the start"
        int bytesWritten1 = testFile.write(inputArray1,0)
        int bytesWritten2 = testFile.write(inputArray2,0)

        then: "One block will be created"
        testFile.data.size() == 1

        and: "Number of bytes written will be the input buffer sizes"
        bytesWritten1 == inputArray1.size()
        bytesWritten2 == inputArray2.size()

        and: "The size of the file will be the largest of the batches of bytes written"
        testFile.size() == inputArray1.size()
        testFile.currentOffset == inputArray2.size()

        and: "Last block offset will be third position within second block"
        testFile.lastBlockOffset == inputArray11.size()

        and: "The data will reflect overwritten bytes"
        testFile.data[0] == ([7,8,9,4,5,6] as byte [])
    }

}