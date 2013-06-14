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

package org.ysb33r.gradle.vfs

import org.ysb33r.groovy.dsl.vfs.VFS
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
//import org.gradle.process.ExecResult
import org.apache.commons.logging.LogFactory
import org.gradle.api.invocation.Gradle
import org.gradle.api.internal.MissingMethodException

/**
 *
 * Create a task and add VFS closures to execute
 * @code
 * task copyFromJenkins ( type:Vfs ) << {
 *   cp 'http://jenkins:8080/job/JobName/123/artifacts/somefile.txt' 'somefile.txt'  
 * } 
 * @encode
 * 
 * Configure the VFS options
 * @code
 * copyFromJenkins {
 *   vfs.http.urlCharset 'UTF-8'
 * }
 * @endcode
 *
 * @author Schalk W. Cronjé
 */
class Vfs extends DefaultTask {

  private VFS __vfs
   
  VFS getVFS() { __vfs }
  
  /** Constructs the basic task class and sets executable path
   * if not already set.
   */
  Vfs() {
      __vfs = new VFS ( 
          logger : LogFactory.getLog('vfs'),
          temporaryFileStore : "${this.project.gradle.gradleUserHomeDir}/vfs"
      )
      
      __vfs.fsMgr.schemes.each { scheme ->
          this.metaClass."${scheme}" = { Closure c->
              "vfs.${scheme}"
          }
      }
  }

  def call(Closure c) {
      Closure cfg =c.clone()
      cfg.delegate = this
      cfg()
  }
  
  /*
  @TaskAction
  void noop() {}
  */
}
