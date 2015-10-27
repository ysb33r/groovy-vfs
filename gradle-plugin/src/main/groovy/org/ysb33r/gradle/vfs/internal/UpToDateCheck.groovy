/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2015
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
package org.ysb33r.gradle.vfs.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import org.gradle.api.logging.Logger
import org.ysb33r.gradle.vfs.VfsCopySpec
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.VfsURICollection
import org.ysb33r.groovy.dsl.vfs.VFS
import org.ysb33r.groovy.dsl.vfs.impl.Util

/** Collection of class methods for determining how up to date a destination is compared to a source.
 *
 * @author Schalk W. Cronjé
 * @since 1.0
 */
@CompileStatic
class UpToDateCheck {

    /** Iterates over all of the file hierarchy within in {@link VfsURICollection} and checks whether the equivalent file
     * would exist on a target. If the source and remote schemes support GET_MODIFIED_DATE, then those dates would also be
     * used as part of the up-to-date decision
     *
     * @param logger
     * @param vfs
     * @param sources
     * @param destRoot
     * @return {@code false} as soon as the first remote target does not exist or is older than the source.
     */
    static boolean forUriCollection( Logger logger, VFS vfs, VfsURICollection sources, def destRoot) {

        final FileObject destFileObject = vfs.resolveURI(destRoot)

        // Using exceptions for flow control. I don;t really like it,
        // but currently it is the only way to go as vfs.ls does not implement an Iterator interface
        try {
            sources.each { vfsURI ->
                def src = (vfsURI as VfsURI).resolve()
                FileName srcRoot = vfs.resolveURI(src.uri).name
                def options = [:]

                boolean checkModifiedDate = true // should only be true if both src and dest filesystems support it

                if(vfs.fsCanListFolderContent(src.uri)) {
                    if (src.praxis.filter) {
                        options['filter'] = src.praxis.filter
                    }

                    vfs.ls options, src.uri, { FileObject fo ->
                        String relSrc = srcRoot.getRelativeName(fo.name)
                        def dest = Util.addRelativePath(destFileObject, relSrc)
                        throwIfOutOfDate vfs,src.uri,dest,checkModifiedDate
                    }
                } else {
                    def dest = Util.addRelativePath destFileObject, srcRoot.baseName
                    throwIfOutOfDate vfs,src.uri,dest,checkModifiedDate
                }
            }
        }
        catch (final OutOfDateException e) {
            logger.debug e.message
            return false
        }
        return true
    }

    /** Checks whether the source is newer than the destination. Only dayes on files are checked, folders are not.
     *
     * @param logger
     * @param vfs
     * @param rootSpec
     * @param destRoot
     * @return {@code true} is destination is considered older than source
     */
    static boolean forCopySpec( Logger logger, VFS vfs, VfsCopySpec rootSpec, VfsURI destRoot ) {
        VfsURI dest = destRoot.resolve()
        if(!vfs.exists(destRoot.uri)) {
            logger.debug "Target is out of date: ${friendlyURI(vfs,dest)} does not exist."
            return false
        }

        if(!forUriCollection( logger, vfs,rootSpec.uriCollection , destRoot.uri)) {
            return false
        }

        for( Object ch : rootSpec.children()) {
            VfsCopySpec child = ch as VfsCopySpec
            VfsURI childDest = ResolvedURI.create([:], vfs, Util.addRelativePath(dest.uri as FileObject, child.relativePath))
            if(!forUriCollection(logger,vfs,child.uriCollection,childDest.uri)) {
                return false
            }
        }

        return true
    }

    /** Helper to throw {@link OutOfDateException} if a destination target is out of date
     *
     * @paran vfs VFS object to use
     * @param src Source {@code FileObject}
     * @param dest Destination {@code FileObject}
     * @param checkModifiedDate Whether modification dates should be compared
     */
    private static void throwIfOutOfDate( VFS vfs, Object src, FileObject dest, boolean checkModifiedDate) {
        if (!vfs.exists(dest)) {
            throw new OutOfDateException("'${vfs.friendlyURI(dest)}' does not exist")
        }

        if (checkModifiedDate && vfs.mtime(dest) < vfs.mtime(src)) {
            throw new OutOfDateException("'${vfs.friendlyURI(dest)}' is out of date")
        }
    }

    /** Creates a 'friendly' version of a URI suitable for printing in logs.
     *
     * @param vfs
     * @param uri
     * @return
     */
    @CompileDynamic
    private static String friendlyURI(VFS vfs,VfsURI uri) {
        vfs.friendlyURI( uri.uri )
    }

    /** An internal class used for flow control (yuck)
     *
     */
    private static class OutOfDateException extends Exception {
        OutOfDateException(final String msg) {super(msg)}
    }
}
