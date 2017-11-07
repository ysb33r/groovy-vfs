package org.ysb33r.vfs.dsl.groovy

import groovy.transform.CompileStatic
import org.ysb33r.vfs.core.FileSystemException

import java.util.regex.Pattern

/**
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
}
