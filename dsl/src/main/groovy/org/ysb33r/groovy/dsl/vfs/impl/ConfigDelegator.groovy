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

package org.ysb33r.groovy.dsl.vfs.impl

import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.FileSystemOptions
import org.ysb33r.groovy.dsl.vfs.SyntaxException
import groovy.transform.*

@TupleConstructor
class ConfigDelegator {

    FileSystemManager fsManager
    FileSystemOptions fsOpts

    private String scheme

    def bind(Closure c) {
        c.delegate = this
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c()
        fsOpts
    }

    def propertyMissing(String property) { property }
    
    def methodMissing(String method, args) {
        def object = args[0]
        
        switch (object) {
            case Closure :
                if (scheme != null ) {
                    throw new SyntaxException("Syntax issue in VFS options configuration DSL")
                }
                 
                if( ! fsManager.getFileSystemConfigBuilder(method) ) {
                    throw new SyntaxException("'${method}' is not currently supported in this DSL. Maybe you are missing a third-party dependency")
                }

                scheme = method
                object.delegate = this
                object.resolveStrategy = Closure.DELEGATE_FIRST
                object()
                scheme= null
                break

             default:
                if( scheme == null ) {
                    throw new SyntaxException("Syntax issue in VFS options configuration DSL. '${method}' is not valid in this context.")
                }
                
                fsOpts= Util.setOption( scheme, method, fsManager, fsOpts, args[0] )
        }
    }
}

