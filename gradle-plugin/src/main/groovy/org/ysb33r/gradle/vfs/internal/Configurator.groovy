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

import groovy.util.logging.Slf4j
import org.apache.commons.lang.NotImplementedException
import org.gradle.api.GradleException
import org.ysb33r.gradle.vfs.VfsOptions
import org.ysb33r.groovy.dsl.vfs.impl.AntPatternSelector

/**
 * @author Schalk W. Cronjé
 */
@Slf4j
class Configurator implements VfsOptions {

    static Configurator execute( Closure cfg ) {
        def config= new Configurator()
        def c = cfg.clone()
        c.delegate=config
        c.call()
        config
    }

    @Override
    Map<String,Object> getOptionMap() {
        if(selector) {
            optionMap + [ filter : selector ]
        } else {
            optionMap
        }
    }

    void options(Map opts) {
        if(selector && opts.containsKey('filter')) {
            log.warn "Cannot supply a custom filter if ANT-style patterns are being used"
            optionMap+= opts.remove('filter')
        } else {
            optionMap+= opts
        }
    }

//    void options(Closure opts) {
//
//    }

    def methodMissing(String name, args) {
        if(name ==~ /include|exclude|[sg]etIncludes|[sg]etExcludes|[sg]etCaseSensitive/) {
            if(selector == null) {
                selector = new AntPatternSelector()
            }
            selector.invokeMethod(name,args)
        } else {
            throw new MissingMethodException(name, delegate, args)
        }
    }

    private Map<String,Object> optionMap = [:]
    private AntPatternSelector selector

}
