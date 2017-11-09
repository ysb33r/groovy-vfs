package org.ysb33r.vfs.core

import groovy.transform.CompileStatic

import java.nio.file.Files
import java.util.regex.Pattern

/** A grouping of useful file selectors.
 *
 * @since 2.0
 */
@CompileStatic
class Selectors {

    /** Selects a file or folder by regular expression.
     *
     * @param regex Regex to apply to basename of file or folder.
     * @param maxDepth Set to 0 or 1 if child folders should not be traversed, -1 for infinite traversal or any other value
     * for depth-limited traversal.
     * @param followSymlinks If symoblic links are encountered whether they should be followed.
     * @return {@link FileSelector} instance.
     */
    static FileSelector byRegex(final Pattern regex, int maxDepth, boolean followSymlinks) {
        [
            include : { FileSelectInfo fsi ->
                fsi.current.name ==~ regex && fsi
            },
            descend : { FileSelectInfo fsi ->
                -1 == maxDepth|| fsi.depth <= maxDepth
            },
            follow : followSymlinks
        ] as FileSelector
    }

    /** Wraps another {@link FileSelector} to never follow symbolic links.
     *
     * @param fs {@link FileSelector} to wrap.
     * @return A selector that will not follow symbolic links
     */
    static FileSelector noFollowSymlinks(final FileSelector fs) {
        new FileSelector() {
            @Override
            boolean include(final FileSelectInfo fsi) {
                fs.include(fsi)
            }

            @Override
            boolean descend(final FileSelectInfo fsi) {
                fs.descend(fsi)
            }

            @Override
            boolean follow(final FileSelectInfo fsi) {
                return false
            }
        }
    }

    /** Selects all the descendants of the starting point folder, but not the starting point itself.
     *
     * <p> This is a selector with infinite depth traversal.
     */
    static final FileSelector EXCLUDE_SELF = new FileSelector() {
        @Override
        boolean include(final FileSelectInfo fsi) {
            fsi.depth > 0
        }

        @Override
        boolean descend(final FileSelectInfo fsi) {
            true
        }

        @Override
        boolean follow(final FileSelectInfo fsi) {
            true
        }
    }

    /** Selects all the starting point, all of it's children and all of their descendents.
     *
     * <p> This is a selector with infinite depth traversal.
     */
    static FileSelector SELECT_ALL = new FileSelector() {
        @Override
        boolean include(final FileSelectInfo fsi) {
            true
        }

        @Override
        boolean descend(final FileSelectInfo fsi) {
            true
        }

        @Override
        boolean follow(final FileSelectInfo fsi) {
            true
        }
    }

    /** Selects all of the children of the starting point, but does not descend into child folders.
     *
     */
    static FileSelector CHILDREN_ONLY = new FileSelector() {
        @Override
        boolean include(final FileSelectInfo fsi) {
            fsi.depth > 0
        }

        @Override
        boolean descend(final FileSelectInfo fsi) {
            false
        }

        @Override
        boolean follow(final FileSelectInfo fsi) {
            true
        }
    }

    /** Selects the starting point and all of its children, but does not descend into child folders.
     *
     */
    static FileSelector CHILDREN_AND_SELF = new FileSelector() {
        @Override
        boolean include(final FileSelectInfo fsi) {
            true
        }

        @Override
        boolean descend(final FileSelectInfo fsi) {
            false
        }

        @Override
        boolean follow(final FileSelectInfo fsi) {
            true
        }
    }

    /** Selects only the starting point
     *
     */
    static FileSelector SELECT_SELF = new FileSelector() {
        @Override
        boolean include(final FileSelectInfo fsi) {
            fsi.depth == 0
        }

        @Override
        boolean descend(final FileSelectInfo fsi) {
            false
        }

        @Override
        boolean follow(final FileSelectInfo fsi) {
            true
        }
    }

    /** Selects only children of the starting point that can be identified as files.
     *
     */
    static FileSelector FILES_ONLY = new FileSelector() {
        @Override
        boolean include(FileSelectInfo fsi) {
            fsi.depth == 1 && Files.isRegularFile(fsi.current.path)
        }

        @Override
        boolean descend(FileSelectInfo fsi) {
            false
        }

        @Override
        boolean follow(FileSelectInfo fsi) {
            true
        }
    }

    /** Selects only children of the starting point that can be identified as folders.
     *
     * <p> Folders are not traversed.
     *
     */
    static FileSelector FOLDERS_ONLY = new FileSelector() {
        @Override
        boolean include(FileSelectInfo fsi) {
            fsi.depth == 1 && Files.isDirectory(fsi.current.path)
        }

        @Override
        boolean descend(FileSelectInfo fsi) {
            false
        }

        @Override
        boolean follow(FileSelectInfo fsi) {
            true
        }
    }


}
