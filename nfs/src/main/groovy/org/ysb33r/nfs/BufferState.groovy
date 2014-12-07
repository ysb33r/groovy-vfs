package org.ysb33r.nfs

import groovy.transform.CompileStatic

/**
 * The initial state of a buffer is EMPTY.
 * When file data is read into a file it becomes LOADED.
 * If the buffer contains data that has not yet been written
 * to the file then it is DIRTY.
 * The COMMIT state indicates that the data is written but
 * not yet committed.  Once committed, the state returns
 * to LOADED.
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
enum BufferState {
    EMPTY  (0),	// Has no data
    LOADED (1),	// Has file data
    DIRTY  (2),	// Has new data
    COMMIT (3)	// Not committed

    private int state
    private BufferState(int s) {state=s}
}
