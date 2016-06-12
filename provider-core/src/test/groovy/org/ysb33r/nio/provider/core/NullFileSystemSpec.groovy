package org.ysb33r.nio.provider.core

import org.ysb33r.nio.provider.core.helpers.NullFileSystemProvider
import spock.lang.Specification
import java.nio.file.FileSystems
import java.nio.file.FileSystem


class NullFileSystemSpec extends Specification {

    def "Create a null filesystem for experimentation purpose only"() {
        given: "When I have a URI on my null filesystem"
        def nullURI = 'null:null.txt'.toURI()

        when: "I load a null filesystem via Files"
        FileSystem fs = FileSystems.newFileSystem(nullURI, [ key1 : 'value1' ])

        then: "I expect no exception to be thrown"
        fs != null
        fs.provider() instanceof NullFileSystemProvider

        when: ""
        NullFileSystemProvider fsp = (NullFileSystemProvider)fs.provider()
        println fsp.callsMade

        then:
        fsp.callsMade.containsKey('newFileSystem')
    }
}