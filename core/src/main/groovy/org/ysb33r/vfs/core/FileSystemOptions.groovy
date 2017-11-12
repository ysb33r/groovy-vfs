package org.ysb33r.vfs.core

import groovy.transform.CompileStatic

import java.util.regex.Pattern

/** Filesystem options affect the way the new filesystem operates and how files and directories
 * can be openeded on the filesystem.
 *
 * <p> The container itself is very generic and the conversion of the option to something on the
 * underlying filesystem is very specific to the NIO2 provider.
 *
 * <p> The VFS object for the specific language DSL will need to provide a method for registering translators
 * from the generic options here to deal with specific providers. The translators will need to be
 * created/registered by the users of the DSL as they will be the only ones with possible knowledge of the
 * target opertational environment.
 *
 * @since 2.0
 */
@CompileStatic
class FileSystemOptions {

    static final Pattern OPTION_REGEX = ~/^(?i:vfs\.)(\p{Alpha}\p{Alnum}+)\.(\p{Alpha}\w+)$/

    FileSystemOptions() {
    }

    FileSystemOptions(final FileSystemOptions other) {
        addAll(other)
    }

    void add(final String optName,final Object optValue) {

        if(optName =~ OPTION_REGEX) {
            this.options.put(optName.toLowerCase(),optValue)
        }
    }

    void addAll(final FileSystemOptions opts) {
        this.options.putAll(opts.asMap())
    }

    void addAll(final Map<String,?> properties) {
        properties.each { final String key, final Object value ->
            add(key,value)
        }
    }

    Map<String,Object> asMap() {
        this.options
    }


    private final Map<String,Object> options = [:]
}
