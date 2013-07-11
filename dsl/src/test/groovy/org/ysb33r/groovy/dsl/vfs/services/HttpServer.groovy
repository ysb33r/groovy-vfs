// ============================================================================
// (C) Copyright Schalk W. Cronje 2013
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================

package org.ysb33r.groovy.dsl.vfs.services


import java.io.File

import static org.ratpackframework.groovy.RatpackScript.ratpack
import org.ratpackframework.groovy.templating.TemplateRenderer
import org.ratpackframework.http.internal.MethodHandler

class HttpServer {
    
    static final def ROOT = "http://localhost:${System.getProperty('ratpack.port')}"
    static final File TESTFSREADONLYROOT = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
    
    void start() {
    }
    
    void stop() {
    }
    
    private final static RATPACK = ratpack {
            handlers {

                get {
                    println "*** ${request.method.head}"
                    response.send ''
                    
                }
                get "/file1.txt" {
                    response.sendFile "text/plain", new File("${TESTFSREADONLYROOT}/file1.txt")
                }
            }
        }
}