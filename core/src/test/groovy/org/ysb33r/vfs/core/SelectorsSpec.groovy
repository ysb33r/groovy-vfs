package org.ysb33r.vfs.core

import org.ysb33r.vfs.core.helpers.CoreBaseSpecification
import spock.lang.Unroll

import static org.ysb33r.vfs.core.Selectors.*
import static org.ysb33r.vfs.core.FileType.*

class SelectorsSpec extends CoreBaseSpecification {

    static final File testFsReadOnlyRoot = CoreBaseSpecification.testFsReadOnlyRoot
    static final FileSelector regexMaxDepth = byRegex(~/file[13].*|test-subdir.*/, -1,false)
    static final FileSelector regexOneLevel = byRegex(~/file[13].*|test-subdir.*/, 1,false)
    static final VfsURI readUriRoot = new VfsURI(testFsReadOnlyRoot)

    static final Map tree = [
        root   : [ depth : 0, type : FOLDER, relPath : '.' ],
        file1  : [ depth : 1, type : FILE,   relPath : 'file1.txt'   ],
        file2  : [ depth : 1, type : FILE,   relPath : 'file2.txt'   ],
        file3  : [ depth : 2, type : FILE,   relPath : 'test-subdir/file3.txt'   ],
        file4  : [ depth : 2, type : FILE,   relPath : 'test-subdir/file3.txt'   ],
        subdir : [ depth : 1, type : FOLDER, relPath : 'test-subdir' ]
    ]

    @Unroll
    void  'Selection criteria for standard filters (#selectorName)'() {

        given:
        Map data = [:]
        tree.each { String name, Map attrs ->
            FileSelectInfo fsi = new FileSelectInfo() {

                int getDepth() {
                    attrs.depth
                }

                VfsURI getParent() {
                    return null
                }

                VfsURI getCurrent() {
                    readUriRoot.resolve(attrs.relPath)
                }
            }

            data.put(name.toString(),[ info : fsi])
        }


        expect:
        (selector as FileSelector).include(data.root.info) == root
        (selector as FileSelector).include(data.file1.info) == file1
        (selector as FileSelector).include(data.file2.info) == file2
        (selector as FileSelector).include(data.file3.info) == file3
        (selector as FileSelector).include(data.file4.info) == file4
        (selector as FileSelector).include(data.subdir.info) == subdir
        (selector as FileSelector).descend(data.root.info) == droot
        (selector as FileSelector).descend(data.subdir.info) == dsubdir

        where:
        selector         | root  | file1 | file2 | subdir | file3 | file4 | droot | dsubdir | selectorName
        SELECT_ALL       | true  | true  | true  | true   | true  | true  | true  | true    | 'select_all'
        SELECT_SELF      | true  | false | false | false  | false | false | false | false   | 'select_self'
        EXCLUDE_SELF     | false | true  | true  | true   | true  | true  | true  | true    | 'exclude_self'
        CHILDREN_ONLY    | false | true  | true  | true   | false | false | true  | false   | 'children_only'
        CHILDREN_AND_SELF| true  | true  | true  | true   | false | false | true  | false   | 'children_and_self'
        FILES_ONLY       | false | true  | true  | false  | false | false | true  | false   | 'files_only'
        FOLDERS_ONLY     | false | false | false | true   | false | false | true  | false   | 'folders_only'
        regexMaxDepth    | false | true  | false | true   | true  | false | true  | true    | 'regex-max-depth'
        regexOneLevel    | false | true  | false | true   | false | false | true  | false   | 'regex-one-level'

    }
    // On Linux/Mac create symlinks and repeat some tests
    // specifically add noFollowSymlinks
}