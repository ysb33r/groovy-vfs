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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.ysb33r.groovy.dsl.vfs.VFS

@CompileStatic
class Run {
    static void main(String[] args) {
        String name = System.getProperty('vfs.scriptname') ?: 'vfs'
        def vfsexec = Cmdline.parseCmdline(name,args)

        switch(vfsexec) {
            case null:
                System.exit(1)
            case Integer:
                System.exit(vfsexec as Integer)
            case Cmd:
                def r=new Run([vfsexec] as List<Cmd>)
                System.exit(r.execute())
            default:
                def r=new Run(vfsexec as List<Cmd>)
                System.exit(r.execute())
        }
        System.exit(255)
    }

    @CompileDynamic
    Run(final List<Cmd> cmd) {
        vfs = new VFS()
        vfs.script {
            extend {
                provider className: 'org.ysb33r.groovy.vfsplugin.smb.SmbFileProvider', schemes: ['smb','cifs']
                provider className: 'org.ysb33r.groovy.vfsplugin.cloud.s3.S3FileProvider', schemes: ['s3']
            }
        }
        commands = cmd
    }

    Integer execute() {
        def input = System.in
        try {
            commands.each { Cmd cmd ->
                System.in = (cmd.isInteractive() ? input  : null) as InputStream
                cmd.run(vfs)
            }
            return 0
        } catch(final Exception e) {
            System.err.println e
            return 2
        }
    }

    private VFS vfs
    private List<Cmd> commands
}