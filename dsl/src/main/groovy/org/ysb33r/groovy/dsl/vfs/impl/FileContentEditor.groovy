// ============================================================================
// Copyright (C) Schalk W. Cronje 2014
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
// ============================================================================
package org.ysb33r.groovy.dsl.vfs.impl

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.commons.vfs2.FileContent
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.provider.AbstractFileSystem

/**
 * Created by schalkc on 12/12/14.
 */
@CompileStatic
@Canonical
class FileContentEditor {

    FileObject file
    boolean append

    def with(CharSequence cs) {
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
        AbstractFileSystem afs= file.fileSystem as AbstractFileSystem

        try {
            (file.content as FileContent).getOutputStream(append) << strm
            file.close()
        }
        finally {
            afs.closeCommunicationLink()
        }

    }
}
