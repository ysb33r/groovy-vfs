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
// Lots of thanks goes to Luke Daley for helping out with sending up Ratpack:
// https://gist.github.com/alkemist/7943781
// ============================================================================

package org.ysb33r.groovy.dsl.vfs.services


import java.io.File
//import ratpack.launch.LaunchConfig
//import ratpack.launch.LaunchConfigBuilder
//import ratpack.server.RatpackServerBuilder
//import static ratpack.groovy.Groovy.chain as bootstrap
//
//class HttpServer {
//    
//    static final def ROOT = "http://localhost:${System.getProperty('ratpack.port')}"
//    static final File TESTFSREADONLYROOT = new File("${System.getProperty('TESTFSREADROOT')}/src/test/resources/test-files")
//    
//    def server
//    
//    HttpServer() {
//        
//        def launchConfig = LaunchConfigBuilder.
//          baseDir(TESTFSREADONLYROOT).
//          port(System.getProperty('ratpack.port').toInteger()).
//          build { cfg ->
//            ratpack.groovy.Groovy.chain (cfg) {
//              get ("file1.txt") {
//                  println "*** ${request.method.head}"
//                  if(request.method.head) {
//                      send ''
//                  } else {
//                    render file('file1.txt')
//                  }
//                }
//            }
//          }
//        
//        server = RatpackServerBuilder.build(launchConfig)
//
//    }
//
//    void start() {
//        server.start()
//    }
//    
//    void stop() {
//        server.stop()
//    }
//    
//}
