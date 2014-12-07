// ============================================================================
// Copyright (C) Schalk W. Cronje 2013
//
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================

package org.ysb33r.groovy.dsl.vfs.impl

import org.ysb33r.groovy.dsl.vfs.SyntaxException
import org.ysb33r.groovy.dsl.vfs.FileSystemException

import groovy.transform.*

@TupleConstructor
class ProviderDelegator {

    StandardFileSystemManager fsManager

    private String scheme

    def bind(Closure c) {
        c.delegate = this
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c()
    }

    boolean defaultProvider( String className ) {
        try {
            fsManager.setDefaultProvider(fsManager.createFileProvider(className))
            true
        } catch(final FileSystemException e) {
            loggerInstance().debug "Skipping ${provider.className} due to missing class: `${e}`"
            false
        }
    }

    boolean provider( providerMap =[:] ) {
        if(providerMap.size() == 0) {
            throw new SyntaxException("Map passed to provider cannot be empty")
        }
        fsManager.addProvider(new Provider( providerMap ) )
    }

    void operationProvider( providerMap = [:] ) {
        if(providerMap.size() == 0) {
            throw new SyntaxException("Map passed to operationProvider cannot be empty")
        }
        fsManager.addOperationProvider(new OperationProvider( providerMap ) )
    }

    @CompileStatic
    void mimeType( String key,String scheme ) {
        fsManager.addMimeTypeMap(key,scheme)
    }

    @CompileStatic
    void ext( String extension,String scheme  ) {
        fsManager.addExtensionMap(extension,scheme)
    }
}

