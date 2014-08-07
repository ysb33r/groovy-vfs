package org.ysb33r.nfs

import groovy.transform.CompileStatic

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
enum BufferAction {
    IDLE   (0),
    LOAD   (1),
    UNLOAD (2),
    EXIT   (3)

    BufferAction(final int a) {action=a}
    private final int action

}
