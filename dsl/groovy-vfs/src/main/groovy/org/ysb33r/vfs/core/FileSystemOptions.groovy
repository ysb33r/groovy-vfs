package org.ysb33r.vfs.core

import groovy.transform.CompileStatic

import java.util.regex.Pattern

/** Filesystem options affect the way the new filesystem operates and how files and directories
 * can be openeded on the filesystem.
 *
 * <p> The container itself is very generic and the conversion of the option to something on the
 * underlying filesystem is very sepcifc to the NIO2 provider.
 *
 * <p> The VFS object for the specific language DSL will need to provide amethod for registering translators
 * from the generic options here to deal with specific providers. The translators will need to be
 * created/registered by the users of the DSL as they will be the only ones with possible knowledge of the
 * target opertational environment.
 *
 * @since 2.0
 */
@CompileStatic
class FileSystemOptions {

    static final Pattern OPTION_REGEX = ~/^(?i:vfs\.)(\p{Alpha}\p{Alnum}+)\.(\p{Alpha}\w+)$/

    void add(final String optName,final Object optValue) {
        throw new FileSystemException("add(name,value) needs an implementation")
    }

    void addAll(final FileSystemOptions opts) {
        throw new FileSystemException("addAll(FSO) needs an implementation")
    }

    void addAll(final Map<String,?> properties) {
        throw new FileSystemException("addAll(Map) needs an implementation")
    }

    Map<String,Object> asMap() {
        null
    }
}
