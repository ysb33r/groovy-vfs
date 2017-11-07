package org.ysb33r.vfs.dsl.groovy

/**
 * @since 2.0
 */
interface FileContentEditor {
    def with(CharSequence cs)
    def with(Closure cs)
}