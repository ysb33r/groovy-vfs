package org.ysb33r.groovy.dsl.vfs

import spock.lang.Specification


/**
 * Created by schalkc on 12/12/14.
 */
class VFSSpec extends Specification {

    VFS vfs

    void setup() {
        vfs=new VFS()
    }

    def "Char sequences must convert to URI instances"() {
        given:
          def result
          vfs {
              result = uri 'sftp://user:pass@server/dir?vfs.sftp.userDirIsRoot=1'
          }

        expect:
            result.properties.sftp.userDirIsRoot == '1'
            result.toString() == 'sftp://user:pass@server/dir'
    }
}