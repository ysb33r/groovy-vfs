
package org.ysb33r.gradle.vfs

/** Property maps suitable for passing to VFS operations.
 *
 * @author Schalk W. Cronjé
 */
interface VfsOptions {
    /** Returns an option map that can be passed to a VFS object at a later stage
     *
     * @return Map
     */
    Map<String,Object> getOptionMap()
}