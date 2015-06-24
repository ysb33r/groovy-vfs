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
//
// ============================================================================
// (C) Copyright Schalk W. Cronje 2013-2015
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
//
package org.ysb33r.groovy.vfs.app

import groovy.transform.TupleConstructor
import org.ysb33r.groovy.dsl.vfs.VFS
import org.ysb33r.groovy.dsl.vfs.URI
import org.ysb33r.groovy.dsl.vfs.FileActionException

@TupleConstructor
class Mv implements Cmd {

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

    // TODO: ISSUE #27 - Implement update - Needs support from DSL. Maybe property taking a boolean or closure
    // TODO: ISSUE #28 - Implement verbose - Probably needs support from DSL via a closure passed as propertyu
    // TODO: ISSUE #29 - How to define backupStrategy
    // TODO: ISSUE #29 - Implement Backups
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

        def properties = [ smash : false, 'intermediates' : intermediates, 'overwrite' : overwrite ]

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
                mv properties, src, destination
            }
        }

        return 0i
    }

    boolean isInteractive() { return interactive }
}

