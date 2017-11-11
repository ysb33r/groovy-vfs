package org.ysb33r.vfs.core

import groovy.transform.CompileStatic

/** Logic for including or excluding files and folder during a traversal.
 *
 * @since 2.0
 */
@CompileStatic
interface FileSelector {

    /** Decide whether file or folder whould be included.
     *
     * @param fsi Information regarding an encountered file or folder.
     * @return {@code true} if file or folder should be included.
     */
    boolean include(final FileSelectInfo fsi)

    /** If the encountered entity is a folder this will be called to decide whether deeper
     * traversal should occur.
     *
     * @param fsi Information regarding an encountered  folder.
     * @return {@code true} is folder should be traversed.
     */
    boolean descend(final FileSelectInfo fsi)

    /** If the encountered item is symbolic link this will be called to decide whether to follow it.
     *
     * @param fsi Information regarding an encountered  folder.
     * @return {@code true} is symbolic link should be followed.
     */
    boolean follow(final FileSelectInfo fsi)
}