// ============================================================================
// (C) Copyright Schalk W. Cronje 2014
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
package org.ysb33r.groovy.vfsplugin.cloud.core

import groovy.transform.CompileStatic
import org.apache.commons.vfs2.util.MonitorOutputStream
import org.jclouds.blobstore.BlobStore
import org.jclouds.blobstore.domain.Blob

/**
 * Created by schalkc on 12/05/2014.
 */
@CompileStatic
class BlobOutputStream extends MonitorOutputStream {

    private Blob blob
    private Closure updater
    private File cachedFile

    BlobOutputStream(File tmpFile,Blob bs,final boolean append, final Closure upd) {
        super(new FileOutputStream(tmpFile,append))
        blob = bs
        cachedFile = tmpFile
        updater = upd
    }

    @Override
    protected void onClose()  {
        blob.setPayload(cachedFile)
        updater(blob)
    }

}
