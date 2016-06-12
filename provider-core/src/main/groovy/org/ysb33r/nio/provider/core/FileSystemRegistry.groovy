package org.ysb33r.nio.provider.core

import groovy.transform.CompileStatic

import java.nio.file.FileSystem
import java.nio.file.FileSystemAlreadyExistsException

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
interface FileSystemRegistry {

    /** See if a filesystem related to the specific key exists
     *
     * @param key Filesystem key
     * @return {@code true} is filesystem exists for the given key.
     */
    boolean contains(final String key)

    /** Adds a filesystems if it does not exist.
     *
     * @param key Key to use to store filesystem
     * @param fs {@code FileSystem} instance.
     * @throw {@code FileSystemAlreadyExistsException} if a filesystem exists for the given key.
     */
    void add(final String key, FileSystem fs)

    /** Returns the filesystem that is associated with the specific key.
     *
     * @param key Key to use to retrieve filesystem
     * @return {@code FileSystem} instance.
     * @throw {@code FileSystemNotFoundException} is no filesystem is associated with the given key.
     */
    FileSystem get(final String key)
}