package org.ysb33r.vfs.dsl.groovy

import java.util.function.Consumer

/**
 * @since 2.0
 */
interface FileContentEditor {
    def with(final CharSequence cs)
    def with(final Consumer<OutputStream> cs)
    def with(final InputStream is)
}