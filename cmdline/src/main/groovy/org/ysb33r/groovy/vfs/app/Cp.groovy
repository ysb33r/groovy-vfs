// ============================================================================
// (C) Copyright Schalk W. Cronje 2014
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
package org.ysb33r.groovy.vfs.app

import groovy.transform.TupleConstructor
import org.ysb33r.groovy.dsl.vfs.VFS
import org.ysb33r.groovy.dsl.vfs.URI
import org.ysb33r.groovy.dsl.vfs.FileActionException

@TupleConstructor
class Cp implements Cmd {

    URI destination = null
    List<URI> sources = []

    /** overwrite is a boolean or a closure
     *
     */
    def overwrite = true

    /** If set the destination is treated as a file
     *
     */
    boolean targetIsFile = false

    /** Only update if newer by last modification time
     *
     */
    boolean update = false

    /** Tell everyone what is happening
     *
     */
    boolean verbose = false

    /** Whether directories should be copied recursively
     *
     */
    boolean recursive = false

    // TODO: Implement update - Needs support from DSL. Maybe property taking a boolean or closure
    // TODO: Implement verbose - Probably needs support from DSL via a closure passed as property
    // TODO: How to define backupStrategy
    // TODO: Implement Backups
    //    The backup suffix is '~', unless set with --suffix  or  SIMPLE_BACKUP_SUFFIX.
    //    The version control method may be selected via the --backup option or through
    //    the VERSION_CONTROL environment variable.  Here are the values:
    //
    //    none, off
    //    never make backups (even if --backup is given)
    //
    //    numbered, t
    //    make numbered backups
    //    ﻿       existing, nil
    //    numbered if numbered backups exist, simple otherwise
    //
    //    simple, never
    //    always make simple backups
    def backupStrategy
    String backupSuffix = '~'

    // mv does not actually have a --parents switch, but adding this here
    // for future addition
    boolean intermediates = false

    boolean interactive = false

    Integer run(VFS vfs) {

        assert destination != null
        assert sources.size()

        def properties = [ smash : false, 'intermediates' : intermediates, 'overwrite' : overwrite, 'recursive':recursive ]

        if(sources.size() == 1 ) {
            if(targetIsFile) {
                if (vfs.isFolder(destination)) {
                    throw new FileActionException( "Destination is a folder, but a file is specified." )
                }
                if(!vfs.exists(destination) && vfs.isFolder(sources[0])) {
                    throw new FileActionException( "Source is a folder, but a destination file is specified." )
                }
            }
        } else {
            if(vfs.isFile(destination)) {
               throw new FileActionException( "Destination is a file, but multiple sources provided.")
            }
        }

        vfs {
            sources.each { src ->
                cp properties, src, destination
            }
        }

        return 0i
    }

    boolean isInteractive() { return interactive }
}

//    The following are not implemented and will probably never be supported.
//
//    -a, --archive
//    same as -dR --preserve=all
//
//    --copy-contents
//    copy contents of special files when recursive
//
//    -d     same as --no-dereference --preserve=links
//
//    -H     follow command-line symbolic links in SOURCE
//
//    -l, --link
//    hard link files instead of copying
//
//    -L, --dereference
//    always follow symbolic links in SOURCE
//
//    -P, --no-dereference
//    never follow symbolic links in SOURCE
//
//    -p     same as --preserve=mode,ownership,timestamps
//
//    --preserve[=ATTR_LIST]
//    preserve  the  specified  attributes  (default:   mode,ownership,time‐
//    stamps), if possible additional attributes: context, links, xattr, all
//
//    --no-preserve=ATTR_LIST
//    don't preserve the specified attributes
//
//    --reflink[=WHEN]
//    control clone/CoW copies. See below
//
//    --sparse=WHEN
//    control creation of sparse files. See below
//
//    --strip-trailing-slashes
//    remove any trailing slashes from each SOURCE argument
//
//    -s, --symbolic-link
//    make symbolic links instead of copying
//
//    By  default,  sparse  SOURCE  files are detected by a crude heuristic and the
//    corresponding DEST file is  made  sparse  as  well.   That  is  the  behavior
//    selected  by  --sparse=auto.  Specify --sparse=always to create a sparse DEST
//    file whenever the SOURCE file contains a long enough sequence of zero  bytes.
//    Use --sparse=never to inhibit creation of sparse files.
//
//    When  --reflink[=always]  is specified, perform a lightweight copy, where the
//    data blocks are copied only when modified.  If this is not possible the  copy
//    fails, or if --reflink=auto is specified, fall back to a standard copy.
//
//    --backup[=CONTROL]
//    make a backup of each existing destination file
//
//    -b     like --backup but does not accept an argument
//
//    -S, --suffix=SUFFIX
//    override the usual backup suffix
//
//    The  backup  suffix is '~', unless set with --suffix or SIMPLE_BACKUP_SUFFIX.
//    The version control method may be selected via the --backup option or through
//    the VERSION_CONTROL environment variable.  Here are the values:
//
//    none, off
//    never make backups (even if --backup is given)
//
//    numbered, t
//    make numbered backups
//
//    existing, nil
//    numbered if numbered backups exist, simple otherwise
//
//    simple, never
//    always make simple backups
//
//    ﻿       As  a  special  case,  cp  makes a backup of SOURCE when the force and backup
//    options are given and SOURCE and DEST are the same name for an existing, reg‐
//    ular file.
//
