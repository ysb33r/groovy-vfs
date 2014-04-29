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
import org.ysb33r.groovy.dsl.vfs.URI as vfsURI

@TupleConstructor
class Mv implements Cmd {

    List<vfsURI> uris = []

    // Enum ?? []
    // backups ??
    def backupStrategy
    // overwrite is true, false or {}
    def overwrite = true
    boolean stripTrailingSlashes = false
    String backupSuffix = '~'
    boolean targetIsFile = false
    boolean update = false
    boolean verbose = false
    boolean interactive = false

    Integer run(VFS vfs) {

        if(uris.size() < 2) {
            return 1i
        }

        def properties = [ smash : false, intermediates : false, 'overwrite' : overwrite ]
        List<vfsURI> sources = uris.clone()
        def dest = sources.pop()

        if(uris.size() == 2 ) {
            // if targetIsFile, we need to check whether the target exists and if so, whether it is a file
            if(targetIsFile) {
                assert false // NOT IMPLEMENTED YET
            }
        } else {
            // Check that if dest exists and that it a folder
        }

        vfs {
            sources.each { src ->
                mv properties, src, dest
            }
        }

        return 0i
    }

    boolean isInteractive() { return interactive }
}

//    ﻿SYNOPSIS
//    mv [OPTION]... [-T] SOURCE DEST
//    mv [OPTION]... SOURCE... DIRECTORY
//    mv [OPTION]... -t DIRECTORY SOURCE...
//
//    DESCRIPTION
//    Rename SOURCE to DEST, or move SOURCE(s) to DIRECTORY.
//
//    Mandatory arguments to long options are mandatory for short options too.
//
//    --backup[=CONTROL]
//    make a backup of each existing destination file
//
//    -b     like --backup but does not accept an argument
//
//    -f, --force
//    do not prompt before overwriting
//
//    -i, --interactive
//    prompt before overwrite
//
//    -n, --no-clobber
//    do not overwrite an existing file
//
//    If you specify more than one of -i, -f, -n, only the final one takes effect.
//
//    --strip-trailing-slashes
//    remove any trailing slashes from each SOURCE argument
//
//    -S, --suffix=SUFFIX
//    override the usual backup suffix
//
//    ﻿      -t, --target-directory=DIRECTORY
//    move all SOURCE arguments into DIRECTORY
//
//    -T, --no-target-directory
//    treat DEST as a normal file
//
//    -u, --update
//    move  only  when the SOURCE file is newer than the destination file or
//    when the destination file is missing
//
//    -v, --verbose
//    explain what is being done
//
//    --help display this help and exit
//
//    --version
//    output version information and exit
//
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