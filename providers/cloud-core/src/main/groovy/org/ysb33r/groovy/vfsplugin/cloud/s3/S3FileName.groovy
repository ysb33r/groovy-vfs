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
package org.ysb33r.groovy.vfsplugin.cloud.s3

import groovy.transform.CompileStatic
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.UriParser

/**
 * An SMB URI.  Adds a share name to the generic URI.
 */
@CompileStatic
class S3FileName extends AbstractFileName
{

    private static final char[] USERNAME_RESERVED = ':@/'.toCharArray()
    private static final char[] PASSWORD_RESERVED = '@/?'.toCharArray()

    final String bucket
    private final String accessKey
    private final String secretKey

    protected S3FileName(
        final String scheme,
        final String bucket,
        final String userName,
        final String password,
        final String path,
        final FileType type) {

        super(scheme,path,type)
        this.bucket=bucket
        this.accessKey=userName
        this.secretKey=password
    }

    /**
     * Builds the root URI for this file name.
     */
    @Override
    protected void appendRootUri(StringBuilder buffer, final boolean addPassword) {
        buffer.append("${scheme}://" + credentials(addPassword) + bucket)
    }

    /**
     * Factory method for creating name instances.
     */
    @Override
    public FileName createName(final String path, final FileType type) {
        return new S3FileName(scheme, bucket, accessKey, secretKey, path, type)
    }

    /**
     * append the user credentials
     */
    private String credentials(final boolean addPassword) {
        StringBuilder buffer = new StringBuilder()

        if (accessKey?.size()) {
            UriParser.appendEncoded(buffer, accessKey, USERNAME_RESERVED);
            if (secretKey?.size()) {
                buffer.append(':');
                if (addPassword) {
                    UriParser.appendEncoded(buffer, secretKey, PASSWORD_RESERVED);
                } else {
                    buffer.append("***");
                }
            }
            buffer.append('@')
        }

        return buffer.toString()
    }
}


