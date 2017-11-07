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
package org.ysb33r.groovy.dsl.vfs.impl

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.commons.vfs2.FileContent
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileType
import org.apache.commons.vfs2.provider.AbstractFileSystem
import org.ysb33r.groovy.dsl.vfs.FileActionException

/**
 * Created by schalkc on 12/12/14.
 */
@CompileStatic
@Canonical
class FileContentEditor {

    FileObject file
    boolean append

    def with(CharSequence cs) {

        assertNoDirectory()
        AbstractFileSystem afs= file.fileSystem as AbstractFileSystem

        try {
            (file.content as FileContent).getOutputStream(append) << cs
            file.close()
        }
        finally {
            afs.closeCommunicationLink()
        }

    }

    @CompileDynamic
    def with(Closure cl) {

        assertNoDirectory()
        AbstractFileSystem afs= file.fileSystem as AbstractFileSystem

        try {
            cl(file.content.getOutputStream(append))
            file.close()
        }
        finally {
            afs.closeCommunicationLink()
        }

    }

    def with(InputStream strm) {

        assertNoDirectory()
        AbstractFileSystem afs= file.fileSystem as AbstractFileSystem

        try {
            (file.content as FileContent).getOutputStream(append) << strm
            file.close()
        }
        finally {
            afs.closeCommunicationLink()
        }

    }

    private void assertNoDirectory() {
        if(file.type == FileType.FOLDER) {
            throw new FileActionException("Cannot write data to ${file.name.friendlyURI} as it is a directory")
        }
    }
}
