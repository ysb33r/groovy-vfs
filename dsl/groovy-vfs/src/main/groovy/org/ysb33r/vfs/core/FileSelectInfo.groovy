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