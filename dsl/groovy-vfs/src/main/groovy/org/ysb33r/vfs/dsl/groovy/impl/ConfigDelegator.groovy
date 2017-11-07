/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2017
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

package org.ysb33r.vfs.dsl.groovy.impl

import groovy.transform.CompileStatic
import org.ysb33r.vfs.dsl.groovy.FileSystemOptions
import org.ysb33r.vfs.dsl.groovy.SyntaxException


/** A configurator that allows for declarative option-specification.
 *
 *
 *
 */
@CompileStatic
class ConfigDelegator {

    ConfigDelegator(final FileSystemOptions fso) {
        this.fsOptions = fso
    }

    /** Binds a configurating closure and configure the associated {@link FileSystemOptions} object.
     *
     * @param c Configurating closure
     * @return Configured {@link FileSystemOptions} instance.
     */
    FileSystemOptions bind(Closure c) {
        Closure configurator = (Closure)(c.clone())
        configurator.delegate = this
        configurator.resolveStrategy = Closure.DELEGATE_FIRST
        configurator()
        fsOptions
    }

    String propertyMissing(String property) {
        property
    }
    
    def methodMissing(final String method, Object... args) {

        if(args.size() != 1) {
            throw new SyntaxException("Syntax issue in VFS options configuration DSL. '${method}' can only be passed a closure")
        }

        Object object = args[0]
        
        switch (object) {
            case Closure :
                new OptionsDelegator(method.toLowerCase(),fsOptions).bind((Closure)object)
                break

             default:
                throw new SyntaxException("Syntax issue in VFS options configuration DSL. '${method}' can only be passed a closure")
        }
    }

    private FileSystemOptions fsOptions

    static class OptionsDelegator {

        OptionsDelegator(final String scheme,final FileSystemOptions fso) {
            this.fsOptions = fso
            this.scheme = scheme
        }

        void bind(final Closure cfg) {
            Closure configurator = (Closure)(cfg.clone())
            configurator.delegate = this
            configurator.resolveStrategy = Closure.DELEGATE_FIRST
            configurator()
        }

        def methodMissing(final String method,Object... args) {
            final String option = "vfs.${scheme}.${method}"
            if(!args.size()) {
                throw new SyntaxException("Syntax issue in VFS options configuration DSL. '${method}' requires a value")
            } else if(args.size() > 1) {
                fsOptions.add(option,args as List)
            } else {
               fsOptions.add(option,args[0])
            }
        }

        private FileSystemOptions fsOptions
        private String scheme

    }
}

