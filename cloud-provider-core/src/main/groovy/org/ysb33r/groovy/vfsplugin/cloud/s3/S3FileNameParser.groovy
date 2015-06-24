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
package org.ysb33r.groovy.vfsplugin.cloud.s3

import groovy.transform.CompileStatic
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.provider.HostFileNameParser
import org.apache.commons.vfs2.provider.UriParser
import org.apache.commons.vfs2.provider.VfsComponentContext

/**
 * Created by schalkc on 08/05/2014.
 */
@CompileStatic
class S3FileNameParser extends HostFileNameParser {

    static final S3FileNameParser instance = new S3FileNameParser()

    protected S3FileNameParser() {super(0)}

    @Override
    FileName parseUri(final VfsComponentContext context, final FileName base, final String filename) {
        final StringBuilder name = new StringBuilder()
        final HostFileNameParser.Authority auth = extractToPath(filename, name)

        String bucket = (auth.port>0) ? "${auth.hostName}:${auth.port}" : auth.hostName

        UriParser.canonicalizePath(name, 0, name.length(), this)
        UriParser.fixSeparators(name)
        FileType ft = UriParser.normalisePath(name)
        String path = name.toString()

        return new S3FileName(auth.scheme,bucket,auth.userName,auth.password,path,ft)
    }

}
