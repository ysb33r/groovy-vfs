package org.ysb33r.gradle.vfs

import org.gradle.api.file.CopySourceSpec

/** A copy spec in the spirit of {@code org.gradle.api.file.CopySpec}, but specifcally for virtual file systems.
 *
 * @author Schalk W. Cronjé
 * @since 1.0
 */
interface VfsCopySpec extends CopySourceSpec, Cloneable {

    /** Applies an additional set of options to each source within the Spec as well
     * as each child Spec.
     * Existing options will NOT be replaced.
     *
     * @param options {@link VfsOptions} to be applied.
     * @return This updated copy spec
     */
    VfsCopySpec apply(VfsOptions options)

    /** Sets a relative path to the root
     *
     * @param relativePath relative to a root of a parent copy spec
     * @return This copy spec
     */
    VfsCopySpec into (Object relativePath)

    /** Returns a collection of staged or resolved URIs
     * @return {@link VfsURICollection}. Never null.
     */
    VfsURICollection getUriCollection()

    /** Returns the relative resolved from the initial object in a way that is suitable for appending to a URI
     *
     * @return A string representation. Can be null, if no relative path exists.
     */
    String getRelativePath()

    /** Adds the given specs as children of this spec
     *
     * @param sourceSpecs The specs to add
     * @return This copy spec.
     */
    VfsCopySpec with(VfsCopySpec... sourceSpecs)

    /** Allows for iteration of child specs
     *
     * @return An iterator for child specs. Never null.
     */
    Iterable children()
}