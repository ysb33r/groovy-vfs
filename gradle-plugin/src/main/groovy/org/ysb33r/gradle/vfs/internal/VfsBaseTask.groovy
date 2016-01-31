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

package org.ysb33r.gradle.vfs.internal

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.Incubating
import org.gradle.api.tasks.Input
import org.ysb33r.gradle.vfs.VfsCopySpec
import org.ysb33r.gradle.vfs.VfsOptions
import org.ysb33r.gradle.vfs.VfsProxy
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.VfsURICollection
import org.ysb33r.groovy.dsl.vfs.VFS

/**
 * Created by schalkc on 01/01/15.
 *
 * @since 1.0
 */
@Incubating
@CompileStatic
abstract class VfsBaseTask extends DefaultTask {

    VfsBaseTask() {
        super()
        outputs.upToDateWhen { VfsBaseTask task ->
            task.isUpToDate()
        }

        this.vfs = VfsProxy.request(super.project)

        setEnabled !super.project.gradle.startParameter.isOffline()
    }

    /** Checks the state of remote objects and decides whether the object can be up to date
     *
     * @return {@code true} if the object can be considered up to date
     */
    abstract boolean isUpToDate()

    /** Returns a default set of VFS action options (praxis) in case no task-wide set if defined for all URIs
     * within the task. For implementation values see the appropriate operations in the VFS DSL.
     *
     * @return Map of options
     */
    abstract Map<String,Object>  defaultPraxis()

    @Input
    Map<String,Object> getOptions() {
        this.options
    }

//    @CompileDynamic
//    def options(Closure opts) {
//        def exec = opts.clone()
//        exec.delegate = this
//        options += [ '', exec ]
//    }

    def setOptions(Map<String,Object> opts) {
        this.options.clear()
        this.options+= opts
    }

    def options(Map<String,Object> opts) {
        this.options+= opts
    }

    VFS getVfs() {
        this.vfs
    }

    protected Map<String,Object> getPraxis() {
        this.praxis ?: defaultPraxis()
    }

    protected Map<String,Object> praxis( Map<String,Object> opts) {
        if(this.praxis == null) {
            this.praxis = opts
        } else {
            this.praxis += opts
        }
    }

    protected Map<String,Object> setPraxis( Map<String,Object> opts) {
        this.praxis = opts
    }

    protected VfsURI stage(Object uri)  {
        UriUtils.uriWithOptions(getOptions(),vfs,uri)
    }

    protected VfsURICollection stage(List<Object> uris)  {
        UriUtils.uriWithOptions(getOptions(),vfs,uris)
    }

    protected VfsURICollection stage(DefaultVfsCopySpec spec)  {
        Map<String,Object> opts = getOptions() + getPraxis()
        spec.apply( [getOptionMap : { -> opts } ] as VfsOptions ).uriCollection
    }

    private VFS vfs
    private Map<String,Object> praxis
    private Map<String,Object> options = [:]
}
