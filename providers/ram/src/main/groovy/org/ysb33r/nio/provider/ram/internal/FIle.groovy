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

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.Synchronized

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
class File implements Entry {

    static final int OFFSET_BEGIN = 0
    static final int OFFSET_CURRENT = -3
    static final int OFFSET_END = -2

    Attributes attributes = new Attributes(this)
    final Integer blockSize
    final Integer maxBlocks

    File(Map<String,?> props=[:]) {
        if(props['blockSize']) {
            blockSize = props['blockSize'] as Integer
        }
        if(props['maxBlocks']) {
            maxBlocks = props['maxBlocks'] as Integer
        }
    }

    /** Always return {@code true} as this is a file
     *
     * @return {@code true}
     */
    boolean isFile() {true}

    /** Always return {@code false} as this is not a directory
     *
     * @return {@code false}
     */
    boolean isDirectory() {false}

    /** The size if the number of filled blocks, plus the number of bytes in the
     * incompleted block
     *
     * @return File size
     */
    long size() {
        if(data.empty) {
            return 0
        }

        (data.size()-1) * blockSize + lastBlockOffset
    }

    /** Writes data from a byte array at a certain offset.
     *
     * @param src NIO supplied {@link java.nio.ByteBuffer} to read from
     * @param offset Offset into file to write at
     * @return Number of bytes written
     *
     * @throw IOException If offset is negative, trying to write past end of file, or block limit is
     *   exceeded.
     */
    int write(ByteBuffer src, long offset) {
        write(src.array(),offset)
    }

    /** Writes data from a byte array at a certain offset.
     *
     * @param src Byte array to read from
     * @param offset Offset into file to write at
     * @return Number of bytes written
     *
     * @throw IOException If offset is negative, trying to write past end of file, or block limit is
     *   exceeded.
     */
    @Synchronized
    int write(byte[] buf, long offset) {
        if(offset==OFFSET_CURRENT) {
            offset = lastOffset
        } else if (offset == OFFSET_END) {
            offset = size()
        } else if(offset < 0) {
            throw new IOException("Negative offset supplied: ${offset}")
        } else if(offset > size()) {
            throw new IOException("Trying to start write past end of file: ${offset}")
        }

        if(!buf.size()) {
            return 0
        }

        Tuple2<Integer,Integer> block = getBlockIndexAndOffset(offset)
        int requiredBlocks = newBlocksRequired(offset,buf.size())
        boolean blocksAdded = addBlocks(requiredBlocks)

        int currentBlockWritableSize = blockSize - block.second
        int numberFullBlockWrites = 0
        int lastPartialWriteSize = 0
        int currentBlock = block.first
        int currentBlockOffset = block.second
        int bytesWritten = 0

        if(currentBlockWritableSize < buf.size()) {
            numberFullBlockWrites = (int)(buf.size() - currentBlockWritableSize).intdiv(blockSize)
            lastPartialWriteSize = buf.size() - currentBlockWritableSize - numberFullBlockWrites * blockSize
            if( currentBlockWritableSize ) {
                System.arraycopy(buf,0,data[currentBlock],currentBlockOffset,currentBlockWritableSize)
                bytesWritten+= currentBlockWritableSize
            }
            ++currentBlock
        } else {
            currentBlockWritableSize = buf.size()
            System.arraycopy(buf,0,data[currentBlock],currentBlockOffset,currentBlockWritableSize)
            bytesWritten+= currentBlockWritableSize
        }

        if(numberFullBlockWrites > 0) {
            (1..numberFullBlockWrites).each { int index ->
                System.arraycopy(
                    buf,
                    bytesWritten,
                    data[currentBlock],
                    0,
                    blockSize
                )
                bytesWritten+=blockSize
                ++currentBlock
            }
        }

        if(lastPartialWriteSize > 0) {
            System.arraycopy(
                buf,
                bytesWritten,
                data[currentBlock],
                0,
                lastPartialWriteSize
            )
            bytesWritten+=lastPartialWriteSize
        }

        updateOffsets(offset,bytesWritten,blocksAdded)
        bytesWritten
    }

    /** Calculates the block index from the offset
     *
     * @param offset
     * @return
     */
    private Tuple2<Integer,Integer> getBlockIndexAndOffset(long offset) {
        int index = (int)offset.intdiv(blockSize)
        long pos = offset - index*blockSize
        if(pos > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Supplied offset (${offset}) will result in a position (${pos})" +
                "that is out of range.")
        }
        new Tuple2<Integer,Integer>( index, (int)pos )
    }

    /** Adds a block
     *
     * @return Returns {@code true} if blocks were added.
     * @throw {@code IOException} if storage limit will be exceeded.
     */
    @SuppressWarnings('ExplicitCallToMultiplyMethod')
    private boolean addBlocks(int count) {
        if(count>0) {
            if(count+data.size() > maxBlocks) {
                throw new IOException("Data write will exceed maximum number of blocks: ${maxBlocks}")
            } else {
                (1..count).each {
                    data.add([0].multiply(blockSize) as byte[] )
                }
                return true
            }
        } else {
            return false
        }

    }

    /** Updates the offsets after a write occurred
     *
     * @param offset Offset where write has started
     * @param bytesWritten Number of bytes that were written
     * @param blocksAdded Were any blocks added before write started?
     *
     *
     */
    private void updateOffsets(long offset,int bytesWritten,boolean blocksAdded) {
        long lastWriteEndedAt = offset + bytesWritten
        Tuple2<Integer,Integer> block = getBlockIndexAndOffset(lastWriteEndedAt)
        // A block write ends one beyond the last physical position
        // This means it could be outside out of current allocated buffers
        // If we did not end up writing into the last block or beyond it
        // don't bother updating.
        //
        if(block.first == data.size()) {
            // We wrote up to the last byte in the current block without a partial write,
            // there mark the offset as the last byte.
            lastBlockOffset = blockSize
        } else if(block.first == data.size() - 1) {

            if( blocksAdded ) {
                // We assume that as we added blocks and as we ended up in the last block,
                // the offset in the last block would be wherever this ended up as.
                lastBlockOffset = block.second
            } else {
                // Since we did not add blocks, but we ended up in the last block,
                // we now assume that the write that occurred in this block would have updated
                // lastBlockOffset so we can just compare offsets
                if(lastBlockOffset < block.second) {
                    lastBlockOffset == block.second
                }
            }
        }
        lastOffset = lastWriteEndedAt
    }

    @PackageScope
    int newBlocksRequired(long offsetToStartWriting,int numBytesToBeWritten) {
        long position = offsetToStartWriting+numBytesToBeWritten
        int overrun = position % blockSize != 0 ? 1 : 0
        int requiredBlocks = position.intdiv(blockSize) + overrun - data.size()
    }

    @PackageScope ConcurrentLinkedDeque<byte[]> data = new ConcurrentLinkedDeque<byte[]>()
    @PackageScope Integer lastBlockOffset =0
    @PackageScope long lastOffset = 0
}
