
package org.ysb33r.gradle.vfs

/** A single source used for copying or moving.
 *
 * @author Schalk W. Cronjé
 */
interface VfsCopySource {
    /** Source object in non-evaluated object form.
     *
     * @return Object as created by a build script. Never null.
     */
    Object getSource()

    /** Options map to be applied to the source when staging or resolving to URI.
     * Can include praxis options..
     *
     * @return Options map. Never null.
     */
    VfsOptions getOptions()
}