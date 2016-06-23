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

import org.ysb33r.nio.provider.ram.internal.Entry
import org.ysb33r.nio.provider.ram.internal.RamFileData
import org.ysb33r.nio.provider.ram.internal.File
import sun.reflect.generics.reflectiveObjects.NotImplementedException

import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel

/**
 * @author Schalk W. Cronjé
 */
class RamFileSeekableByteChannel implements SeekableByteChannel{

    RamFileSeekableByteChannel(File entry,boolean deleteOnClose) {
        throw new NotImplementedException()
    }

    /**
     * Reads a sequence of bytes from this channel into the given buffer.
     *
     * <p> Bytes are read starting at this channel's current position, and
     * then the position is updated with the number of bytes actually read.
     * Otherwise this method behaves exactly as specified in the {@link
     * ReadableByteChannel} interface.
     */
    @Override
    int read(ByteBuffer dst) throws IOException {
        return 0
    }

    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * <p> Bytes are written starting at this channel's current position, unless
     * the channel is connected to an entity such as a file that is opened with
     * the {@link java.nio.file.StandardOpenOption#APPEND APPEND} option, in
     * which case the position is first advanced to the end. The entity to which
     * the channel is connected is grown, if necessary, to accommodate the
     * written bytes, and then the position is updated with the number of bytes
     * actually written. Otherwise this method behaves exactly as specified by
     * the {@link WritableByteChannel} interface.
     */
    @Override
    int write(ByteBuffer src) throws IOException {
        return 0
    }

    /**
     * Returns this channel's position.
     *
     * @return This channel's position,
     *          a non-negative integer counting the number of bytes
     *          from the beginning of the entity to the current position
     *
     * @throws ClosedChannelException
     *          If this channel is closed
     * @throws IOException
     *          If some other I/O error occurs
     */
    @Override
    long position() throws IOException {
        return 0
    }

    /**
     * Sets this channel's position.
     *
     * <p> Setting the position to a value that is greater than the current size
     * is legal but does not change the size of the entity.  A later attempt to
     * read bytes at such a position will immediately return an end-of-file
     * indication.  A later attempt to write bytes at such a position will cause
     * the entity to grow to accommodate the new bytes; the values of any bytes
     * between the previous end-of-file and the newly-written bytes are
     * unspecified.
     *
     * <p> Setting the channel's position is not recommended when connected to
     * an entity, typically a file, that is opened with the {@link
     * java.nio.file.StandardOpenOption # APPEND APPEND} option. When opened for
     * append, the position is first advanced to the end before writing.
     *
     * @param newPosition
     *         The new position, a non-negative integer counting
     *         the number of bytes from the beginning of the entity
     *
     * @return This channel
     *
     * @throws ClosedChannelException
     *          If this channel is closed
     * @throws IllegalArgumentException
     *          If the new position is negative
     * @throws IOException
     *          If some other I/O error occurs
     */
    @Override
    SeekableByteChannel position(long newPosition) throws IOException {
        return null
    }

    /**
     * Returns the current size of entity to which this channel is connected.
     *
     * @return The current size, measured in bytes
     *
     * @throws ClosedChannelException
     *          If this channel is closed
     * @throws IOException
     *          If some other I/O error occurs
     */
    @Override
    long size() throws IOException {
        return 0
    }

    /**
     * Truncates the entity, to which this channel is connected, to the given
     * size.
     *
     * <p> If the given size is less than the current size then the entity is
     * truncated, discarding any bytes beyond the new end. If the given size is
     * greater than or equal to the current size then the entity is not modified.
     * In either case, if the current position is greater than the given size
     * then it is set to that size.
     *
     * <p> An implementation of this interface may prohibit truncation when
     * connected to an entity, typically a file, opened with the {@link
     * java.nio.file.StandardOpenOption # APPEND APPEND} option.
     *
     * @param size
     *         The new size, a non-negative byte count
     *
     * @return This channel
     *
     * @throws NonWritableChannelException
     *          If this channel was not opened for writing
     * @throws ClosedChannelException
     *          If this channel is closed
     * @throws IllegalArgumentException
     *          If the new size is negative
     * @throws IOException
     *          If some other I/O error occurs
     */
    @Override
    SeekableByteChannel truncate(long size) throws IOException {
        return null
    }

    /**
     * Tells whether or not this channel is open.
     *
     * @return <tt>true</tt> if, and only if, this channel is open
     */
    @Override
    boolean isOpen() {
        return false
    }

    /**
     * Closes this channel.
     *
     * <p> After a channel is closed, any further attempt to invoke I/O
     * operations upon it will cause a {@link ClosedChannelException} to be
     * thrown.
     *
     * <p> If this channel is already closed then invoking this method has no
     * effect.
     *
     * <p> This method may be invoked at any time.  If some other thread has
     * already invoked it, however, then another invocation will block until
     * the first invocation is complete, after which it will return without
     * effect. </p>
     *
     * @throws IOException  If an I/O error occurs
     */
    @Override
    void close() throws IOException {
        if(entry != null) {
            if(deleteOnClose) {
                0
            }
            entry = null
        }
    }

    File entry
    boolean deleteOnClose = false
}
