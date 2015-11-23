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
import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystem
import org.apache.commons.vfs2.FileSystemException
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
     * @param noSourceModifiedDateCheck Don't check the source modification dates
     * @return {@code false} as soon as the first remote target does not exist or is older than the source.
     */
    static boolean forUriCollection(
        Logger logger,
        VFS vfs,
        VfsURICollection sources,
        def destRoot,
        boolean noSourceModifiedDateCheck
    ) {

        final FileObject destFileObject = vfs.resolveURI(destRoot)
        final boolean destHasModifiedDate = filesystemHasLastModifiedDate(destFileObject)

        // Using exceptions for flow control. I don't really like it,
        // but currently it is the only way to go as vfs.ls does not implement an Iterator interface
        try {
            sources.each { vfsURI ->
                def src = (vfsURI as VfsURI).resolve()
                FileObject srcFileObject = vfs.resolveURI(src.uri)
                FileName srcRoot = srcFileObject.name
                def options = [:]

                boolean checkModifiedDate = !noSourceModifiedDateCheck && destHasModifiedDate && filesystemHasLastModifiedDate(srcFileObject)

                if(vfs.fsCanListFolderContent(src.uri)) {
                    if (src.praxis.filter) {
                        options['filter'] = src.praxis.filter
                    }

                    vfs.ls options, src.uri, { FileObject fo ->
                        String relSrc = srcRoot.getRelativeName(fo.name)
                        throwIfOutOfDate vfs,src.uri,Util.addRelativePath(destFileObject, relSrc),checkModifiedDate
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

    /** Checks whether the source is newer than the destination. Only dates on files are checked, folders are not.
     *
     * @param logger
     * @param vfs
     * @param rootSpec
     * @param destRoot
     * @param noSourceModifiedDateCheck Don't check the source modification dates
     * @return {@code true} is destination is considered older than source
     */
    static boolean forCopySpec( Logger logger, VFS vfs, VfsCopySpec rootSpec, VfsURI destRoot, boolean noSourceModifiedDateCheck ) {
        VfsURI dest = destRoot.resolve()
        if(!vfs.exists(destRoot.uri)) {
            logger.debug "Target is out of date: ${friendlyURI(vfs,dest)} does not exist."
            return false
        }

        if(!forUriCollection( logger, vfs,rootSpec.uriCollection , destRoot.uri,noSourceModifiedDateCheck)) {
            logger.debug "Sources for rootSpec has changed"
            return false
        }

        for( Object ch : rootSpec.children()) {
            VfsCopySpec child = ch as VfsCopySpec
            VfsURI childDest = ResolvedURI.create([:], vfs, Util.addRelativePath(dest.uri as FileObject, child.relativePath))
            if(!forUriCollection(logger,vfs,child.uriCollection,childDest.uri,noSourceModifiedDateCheck)) {
                logger.debug "Sources targeting child target ${friendlyURI(vfs,childDest)} has changed"
                return false
            }
        }

        logger.debug "Target ${friendlyURI(vfs,dest)} is up to date"
        return true
    }

    /** Checks whether a single-layer or multi-layered filesystem can chekc the modified date throughout
     *
     * @param fo VFS2 FileObject (must not be null)
     * @return {@code true} is all layers can supprot {@code GET_LAST_MODIFIED_DATE}
     */
    private static boolean filesystemHasLastModifiedDate(FileObject fo) {
        assert fo != null
        while(fo != null) {
            FileSystem fs = fo.fileSystem

            if (!fs.hasCapability(Capability.GET_LAST_MODIFIED)) {
                return false
            }

            fo = fs.parentLayer
        }

        return true
    }

    /** Helper to throw {@link OutOfDateException} if a destination target is out of date
     * If either filesystem does not support modification dates, then the modification chek will be ignored.
     *
     * @paran vfs VFS object to use
     * @param src Source {@code FileObject}
     * @param dest Destination {@code FileObject}
     * @param checkModifiedDate Whether modification dates should be compared
     *
     * @throw OutOfDateException if destination is deemed out of date
     *
     */
    private static void throwIfOutOfDate( VFS vfs, Object src, FileObject dest, boolean checkModifiedDate) {
        if (!vfs.exists(dest)) {
            throw new OutOfDateException("'${vfs.friendlyURI(dest)}' does not exist")
        }

        try {
            if (checkModifiedDate && vfs.mtime(dest) < vfs.mtime(src)) {
                throw new OutOfDateException("'${vfs.friendlyURI(dest)}' is out of date")
            }
        } catch(final FileSystemException e) {
            vfs.logger.debug "'${vfs.friendlyURI(dest)}' error when checking modification date: ${e.message}"
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
