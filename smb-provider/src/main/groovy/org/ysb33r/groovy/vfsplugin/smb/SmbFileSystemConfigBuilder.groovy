// ============================================================================
// Copyright (C) Schalk W. Cronje 2014
//
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
// ============================================================================

package org.ysb33r.groovy.vfsplugin.smb

import groovy.transform.CompileStatic
import org.apache.commons.vfs2.FileSystemConfigBuilder

class SmbFileSystemConfigBuilder extends FileSystemConfigBuilder {

    static final def OPTIONS = [ name : 'TYPE' ]

    @Override
    @CompileStatic
    Class<? extends FileSystem> getConfigClass() { SmbFileSystem.class }

    SmbFileSystemConfigBuilder() {super('smb.')}

    SmbFileSystemConfigBuilder(final String prefix) {super(prefix)}

    // missingMethods
    // if not set/get MissingMethodException
    // if in OPTIONS
    //   "get${TYPE}"( opts, SmbFileSystemConfigBuilder.class.name + ${NAME} )
    //   setParam( opts, SmbFileSystemConfigBuilder.class.name + ${NAME}, VALUE )
}

//    private static final String _PREFIX = FtpFileSystemConfigBuilder.class.getName();
//public Boolean getPassiveMode(final FileSystemOptions opts)
//{
//    return getBoolean(opts, PASSIVE_MODE);
//}
//private static final String PASSIVE_MODE = _PREFIX + ".PASSIVE";
//   public void setConnectTimeout(final FileSystemOptions opts, final Integer connectTimeout)
//{
//    setParam(opts, CONNECT_TIMEOUT, connectTimeout);
//}//
