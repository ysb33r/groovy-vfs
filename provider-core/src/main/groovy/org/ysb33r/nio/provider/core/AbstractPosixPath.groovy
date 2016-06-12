package org.ysb33r.nio.provider.core

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

import java.nio.file.FileSystem
import java.nio.file.Path

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
@EqualsAndHashCode
abstract class AbstractPosixPath<T extends FileSystem> extends AbstractPath implements Path {


    /** Constructs a path with one of more components that is tied to the current Filesystem.
     *
     * @param fs Filesystem this path is tied to.
     * @param path Base element of path
     * @param more Optional child elements of the path.
     */
    protected AbstractPosixPath(T fs, final URI baseURI,final String path, final String... more) {
        fileSystem = fs
        elements = split(fs.separator,path)

        if(path.startsWith(fs.separator)) {
            absolute = true
        }

        more.each { String it ->
            elements+= split(fs.separator,it)
        }

        authority = baseURI.authority
    }

    /** Constructs an empty path that is tied to the current filesystem T.
     *
     * @param fs Filesystem this path is tied to.
     */
    protected AbstractPosixPath(T fs, final String authority) {
        fileSystem = fs
        elements = []
        authority
    }

    /**
     * Returns a URI to represent this path.
     *
     * <p> This method constructs an absolute {@link URI} with a {@link
     * URI # getScheme ( ) scheme} equal to the URI scheme that identifies the
     * provider. The exact form of the scheme specific part is highly provider
     * dependent.
     *
     * <p> In the case of the default provider, the URI is hierarchical with
     * a {@link URI#getPath() path} component that is absolute. The query and
     * fragment components are undefined. Whether the authority component is
     * defined or not is implementation dependent. There is no guarantee that
     * the {@code URI} may be used to construct a {@link java.io.File java.io.File}.
     * In particular, if this path represents a Universal Naming Convention (UNC)
     * path, then the UNC server name may be encoded in the authority component
     * of the resulting URI. In the case of the default provider, and the file
     * exists, and it can be determined that the file is a directory, then the
     * resulting {@code URI} will end with a slash.
     *
     * <p> The default provider provides a similar <em>round-trip</em> guarantee
     * to the {@link java.io.File} class. For a given {@code Path} <i>p</i> it
     * is guaranteed that
     * <blockquote><tt>
     * {@link Paths#get(URI) Paths.get}(</tt><i>p</i><tt>.toUri()).equals(</tt><i>p</i>
     * <tt>.{@link #toAbsolutePath() toAbsolutePath}())</tt>
     * </blockquote>
     * so long as the original {@code Path}, the {@code URI}, and the new {@code
     * Path} are all created in (possibly different invocations of) the same
     * Java virtual machine. Whether other providers make any guarantees is
     * provider specific and therefore unspecified.
     *
     * <p> When a file system is constructed to access the contents of a file
     * as a file system then it is highly implementation specific if the returned
     * URI represents the given path in the file system or it represents a
     * <em>compound</em> URI that encodes the URI of the enclosing file system.
     * A format for compound URIs is not defined in this release; such a scheme
     * may be added in a future release.
     *
     * @return the URI representing this path
     *
     * @throws java.io.IOError
     *          if an I/O error occurs obtaining the absolute path, or where a
     *          file system is constructed to access the contents of a file as
     *          a file system, and the URI of the enclosing file system cannot be
     *          obtained
     *
     */
    @Override
    URI toUri() {
        if(absolute) {
            new URI(scheme,authority,pathAsStr,null,null)
        } else {
            toAbsolutePath().toUri()
        }
    }

    /**
     * Resolves the given path against this path's {@link #getParent parent}
     * path. This is useful where a file name needs to be <i>replaced</i> with
     * another file name. For example, suppose that the name separator is
     * "{@code /}" and a path represents "{@code dir1/dir2/foo}", then invoking
     * this method with the {@code Path} "{@code bar}" will result in the {@code
     * Path} "{@code dir1/dir2/bar}". If this path does not have a parent path,
     * or {@code other} is {@link #isAbsolute() absolute}, then this method
     * returns {@code other}. If {@code other} is an empty path then this method
     * returns this path's parent, or where this path doesn't have a parent, the
     * empty path.
     *
     * @param other
     *          the path to resolve against this path's parent
     *
     * @return the resulting path
     *
     * @see #resolve(Path)
     */
    @Override
    Path resolveSibling(Path other) {
        if(other.absolute || parent == null) {
            other
        } else {
            parent.resolve(other)
        }
    }

    /**
     * Converts a given path string to a {@code Path} and resolves it against
     * this path's {@link #getParent parent} path in exactly the manner
     * specified by the {@link #resolveSibling(Path) resolveSibling} method.
     *
     * @param other
     *          the path string to resolve against this path's parent
     *
     * @return the resulting path
     *
     * @throws InvalidPathException
     *          if the path string cannot be converted to a Path.
     *
     * @see java.io.FileSystem#getPath
     */
    @Override
    Path resolveSibling(String other) {
        resolveSibling(createPath(other))
    }

    /**
     * Compares two abstract paths lexicographically. The ordering defined by
     * this method is provider specific, and in the case of the default
     * provider, platform specific. This method does not access the file system
     * and neither file is required to exist.
     *
     * <p> This method may not be used to compare paths that are associated
     * with different file system providers.
     *
     * @param other the path compared to this path.
     *
     * @return zero if the argument is {@link #equals equal} to this path, a
     *          value less than zero if this path is lexicographically less than
     *          the argument, or a value greater than zero if this path is
     *          lexicographically greater than the argument
     *
     * @throws ClassCastException
     *          if the paths are associated with different providers
     */
    @Override
    int compareTo(Path other) {
        if(fileSystem.provider() != other.fileSystem.provider()) {
            throw new ClassCastException("Cannot compare ${this} (${this.class.name}) to ${other} (${other.class.name}) " +
                "as they are from different providers")
        }
        this.toString() <=> other.toString()
    }

    /**
     * Returns the file system that created this object.
     *
     * @return the file system that created this object
     */
    @Override
    FileSystem getFileSystem() {
        this.fileSystem
    }

    /**
     * Returns a {@link File} object representing this path. Where this {@code
     * Path} is associated with the default provider, then this method is
     * equivalent to returning a {@code File} object constructed with the
     * {@code String} representation of this path.
     *
     * <p> If this path was created by invoking the {@code File} {@link
     * File # toPath toPath} method then there is no guarantee that the {@code
     * File} object returned by this method is {@link #equals equal} to the
     * original {@code File}.
     *
     * @return a {@code File} object representing this path
     *
     * @throws UnsupportedOperationException
     *          if this {@code Path} is not associated with the default provider
     */
    @Override
    File toFile() {
        throw new UnsupportedOperationException()
    }

    /**
     * Tells whether or not this path is absolute.
     *
     * <p> An absolute path is complete in that it doesn't need to be combined
     * with other path information in order to locate a file.
     *
     * @return {@code true} if, and only if, this path is absolute
     */
    @Override
    boolean isAbsolute() {
        this.absolute
    }

    /**
     * Returns the number of name elements in the path.
     *
     * @return the number of elements in the path, or {@code 0} if this path
     *          only represents a root component
     */
    @Override
    int getNameCount() {
        elements.size()
    }

    /**
     * Returns a name element of this path as a {@code Path} object.
     *
     * <p> The {@code index} parameter is the index of the name element to return.
     * The element that is <em>closest</em> to the root in the directory hierarchy
     * has index {@code 0}. The element that is <em>farthest</em> from the root
     * has index {@link #getNameCount count} {@code -1}.
     *
     * The default behaviour is to check the indexes and then just create a new relative path
     * based upon a valid index, otherwise throw an exception.
     *
     * @param index
     *          the index of the element
     *
     * @return the name element
     *
     * @throws IllegalArgumentException
     *          if {@code index} is negative, {@code index} is greater than or
     *          equal to the number of elements, or this path has zero name
     *          elements
     */
    @Override
    Path getName(int index) {
        if(index < 0) {
            throw new IllegalArgumentException("Index cannot be negative (was ${index})")
        } else if (index >= elements.size()) {
            throw new IllegalArgumentException("Index is invalid (was ${index})")
        }
        createPath(elements[index])
    }

    /**
     * Returns the root component of this path as a {@code Path} object,
     * or {@code null} if this path does not have a root component.
     *
     * The default behaviour is to assume a single root system and return an equivalent of '/' for absolute paths
     * and null for others
     *
     * @return a path representing the root component of this path,
     *          or {@code null}
     */
    @Override
    Path getRoot() {
        isAbsolute() ? createPath(separator) : null
    }

    /**
     * Returns the name of the file or directory denoted by this path as a
     * {@code Path} object. The file name is the <em>farthest</em> element from
     * the root in the directory hierarchy.
     *
     * @return a path representing the name of the file or directory, or
     * {@code null} if this path has zero elements
     */
    @Override
    Path getFileName() {
        createPath(elements.empty ? null : elements[-1])
    }

    /**
     * Returns the <em>parent path</em>, or {@code null} if this path does not
     * have a parent.
     *
     * <p> The parent of this path object consists of this path's root
     * component, if any, and each element in the path except for the
     * <em>farthest</em> from the root in the directory hierarchy. This method
     * does not access the file system; the path or its parent may not exist.
     * Furthermore, this method does not eliminate special names such as "."
     * and ".." that may be used in some implementations. On UNIX for example,
     * the parent of "{@code /a/b/c}" is "{@code /a/b}", and the parent of
     * {@code "x/y/.}" is "{@code x/y}". This method may be used with the {@link
     * # normalize normalize} method, to eliminate redundant names, for cases where
     * <em>shell-like</em> navigation is required.
     *
     * <p> If this path has one or more elements, and no root component, then
     * this method is equivalent to evaluating the expression:
     * <blockquote><pre>
     * subpath(0,&nbsp;getNameCount()-1);
     * </pre></blockquote>
     *
     * @return a path representing the path's parent
     */
    @Override
    Path getParent() {
        if(elements.empty) {
            null
        } else if(nameCount == 1) {
            absolute ? root : null
        } else {
            followAbsoluteStatus((AbstractPosixPath<T>)subpath(0,nameCount-1))
        }
    }

    /**
     * Returns a relative {@code Path} that is a subsequence of the name
     * elements of this path.
     *
     * <p> The {@code beginIndex} and {@code endIndex} parameters specify the
     * subsequence of name elements. The name that is <em>closest</em> to the root
     * in the directory hierarchy has index {@code 0}. The name that is
     * <em>farthest</em> from the root has index {@link #getNameCount
     * count} {@code -1}. The returned {@code Path} object has the name elements
     * that begin at {@code beginIndex} and extend to the element at index {@code
     * endIndex-1}.
     *
     * @param beginIndex
     *          the index of the first element, inclusive
     * @param endIndex
     *          the index of the last element, exclusive
     *
     * @return a new {@code Path} object that is a subsequence of the name
     *          elements in this {@code Path}
     *
     * @throws IllegalArgumentException
     *          if {@code beginIndex} is negative, or greater than or equal to
     *          the number of elements. If {@code endIndex} is less than or
     *          equal to {@code beginIndex}, or larger than the number of elements.
     */
    @Override
    Path subpath(int beginIndex, int endIndex) {
        if(beginIndex < 0 || endIndex < 0) {
            throw new IllegalArgumentException("Index cannot be negative (was ${beginIndex},${endIndex})")
        } else if (endIndex <= beginIndex) {
            throw new IllegalArgumentException("endIndex <= beginIndex (was ${beginIndex},${endIndex})")
        } else if (endIndex > elements.size()) {
            throw new IllegalArgumentException("Index is invalid (was ${beginIndex},${endIndex})")
        }
        int beginMore = beginIndex + 1
        int endMore = endIndex - 1

        if(endMore == beginIndex) {
            createPath(elements[beginIndex])
        } else {
            createPath(elements[beginIndex],elements[(beginIndex+1)..(endIndex-1)] as String[])
        }
    }

    /**
     * Tests if this path starts with the given path.
     *
     * <p> This path <em>starts</em> with the given path if this path's root
     * component <em>starts</em> with the root component of the given path,
     * and this path starts with the same name elements as the given path.
     * If the given path has more name elements than this path then {@code false}
     * is returned.
     *
     * <p> Whether or not the root component of this path starts with the root
     * component of the given path is file system specific. If this path does
     * not have a root component and the given path has a root component then
     * this path does not start with the given path.
     *
     * <p> If the given path is associated with a different {@code FileSystem}
     * to this path then {@code false} is returned.
     *
     * @param other
     *          the given path
     *
     * @return {@code true} if this path starts with the given path; otherwise
     * {@code false}
     */
    @Override
    boolean startsWith(Path other) {
        if(fileSystem != other.fileSystem) {
            return false
        }

        AbstractPosixPath<T> otherPath =  (AbstractPosixPath<T>)other

        if(elements.empty && otherPath.elements.empty) {
            return true
        }

        if(otherPath.elements.size()  > elements.size()) {
            return false
        }

        if(absolute != otherPath.absolute) {
            return false
        }

        int count = otherPath.elements.size()-1

        for( int index = 0; index < count ; ++index) {
            if (elements[index] != otherPath.elements[index]) {
                return false
            }
        }

        return true
    }

    /**
     * Tests if this path starts with a {@code Path}, constructed by converting
     * the given path string, in exactly the manner specified by the {@link
     * # startsWith ( Path ) startsWith(Path)} method. On UNIX for example, the path
     * "{@code foo/bar}" starts with "{@code foo}" and "{@code foo/bar}". It
     * does not start with "{@code f}" or "{@code fo}".
     *
     * @param other
     *          the given path string
     *
     * @return {@code true} if this path starts with the given path; otherwise
     * {@code false}
     *
     * @throws InvalidPathException
     *          If the path string cannot be converted to a Path.
     */
    @Override
    boolean startsWith(String other) {
        startsWith(createPath(other))
    }

    /**
     * Tests if this path ends with the given path.
     *
     * <p> If the given path has <em>N</em> elements, and no root component,
     * and this path has <em>N</em> or more elements, then this path ends with
     * the given path if the last <em>N</em> elements of each path, starting at
     * the element farthest from the root, are equal.
     *
     * <p> If the given path has a root component then this path ends with the
     * given path if the root component of this path <em>ends with</em> the root
     * component of the given path, and the corresponding elements of both paths
     * are equal. Whether or not the root component of this path ends with the
     * root component of the given path is file system specific. If this path
     * does not have a root component and the given path has a root component
     * then this path does not end with the given path.
     *
     * <p> If the given path is associated with a different {@code FileSystem}
     * to this path then {@code false} is returned.
     *
     * @param other
     *          the given path
     *
     * @return {@code true} if this path ends with the given path; otherwise
     * {@code false}
     */
    @Override
    boolean endsWith(Path other) {
        if(fileSystem != other.fileSystem) {
            return false
        }

        AbstractPosixPath<T> otherPath =  (AbstractPosixPath<T>)other

        if(elements.empty && otherPath.elements.empty) {
            return true
        }

        if(otherPath.elements.size()  > elements.size()) {
            return false
        }

        if(absolute != otherPath.absolute) {
            return false
        }

        int count = -otherPath.elements.size()

        for( int index = -1; index >= count ; --index) {
            if (elements[index] != otherPath.elements[index]) {
                return false
            }
        }

        return true
    }

    /**
     * Tests if this path ends with a {@code Path}, constructed by converting
     * the given path string, in exactly the manner specified by the {@link
     * # endsWith ( Path ) endsWith(Path)} method. On UNIX for example, the path
     * "{@code foo/bar}" ends with "{@code foo/bar}" and "{@code bar}". It does
     * not end with "{@code r}" or "{@code /bar}". Note that trailing separators
     * are not taken into account, and so invoking this method on the {@code
     * Path}"{@code foo/bar}" with the {@code String} "{@code bar/}" returns
     * {@code true}.
     *
     * @param other
     *          the given path string
     *
     * @return {@code true} if this path ends with the given path; otherwise
     * {@code false}
     *
     * @throws {@code InvalidPathException}
     *          If the path string cannot be converted to a Path.
     */
    @Override
    boolean endsWith(String other) {
        endsWith(createPath(other))
    }

    /**
     * Returns a path that is this path with redundant name elements eliminated.
     *
     * <p> The precise definition of this method is implementation dependent but
     * in general it derives from this path, a path that does not contain
     * <em>redundant</em> name elements. In many file systems, the "{@code .}"
     * and "{@code ..}" are special names used to indicate the current directory
     * and parent directory. In such file systems all occurrences of "{@code .}"
     * are considered redundant. If a "{@code ..}" is preceded by a
     * non-"{@code ..}" name then both names are considered redundant (the
     * process to identify such names is repeated until it is no longer
     * applicable).
     *
     * <p> This method does not access the file system; the path may not locate
     * a file that exists. Eliminating "{@code ..}" and a preceding name from a
     * path may result in the path that locates a different file than the original
     * path. This can arise when the preceding name is a symbolic link.
     *
     * @return the resulting path or this path if it does not contain
     *          redundant name elements; an empty path is returned if this path
     *          does have a root component and all name elements are redundant
     *
     * @see <a href='https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html'>getParent()</a>
     * @see <a href='https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html'>toRealPath()</a>
     */
    @Override
    Path normalize() {
        AbstractPosixPath<T> normPath = createPath() as AbstractPosixPath<T>
        if(absolute) {
            normPath.markAbsolute()
        }
        List<String> normalised = []
        int skipCount = 0
        for ( String element in elements.reverse() )  {
            if( element == parentDirAlias ) {
                skipCount++
            } else if (skipCount > 0 ) {
                skipCount--
            } else {
                normalised+= element
            }
        }
        normalised.removeIf { String it -> it == currentDirAlias }
        normPath.elements = normalised.reverse()
        normPath
    }

    /** Converts the path instance to a string representation.
     * The default implementation renders a path that will indicate absolute or relative state and will
     * seperate path segements using the path separator.
     * @return
     */
    @Override
    String toString() {
        getPathAsStr()
    }

    /**
     * Returns a {@code Path} object representing the absolute path of this
     * path.
     *
     * <p> If this path is already {@link Path#isAbsolute absolute} then this
     * method simply returns this path. Otherwise, this method resolves the path
     * in an implementation dependent manner, typically by resolving the path
     * against a file system default directory. Depending on the implementation,
     * this method may throw an I/O error if the file system is not accessible.
     *
     * @return a {@code Path} object representing the absolute path
     *
     * @throws java.io.IOError
     *          if an I/O error occurs
     * @throws SecurityException
     *          In the case of the default provider, a security manager
     *          is installed, and this path is not absolute, then the security
     *          manager's {@link SecurityManager#checkPropertyAccess(String)
     *          checkPropertyAccess} method is invoked to check access to the
     *          system property {@code user.dir}
     */
    @Override
    Path toAbsolutePath() {
        if(absolute) {
            this.normalize()
        } else {
            resolvableRoot.resolve(this.normalize())
        }
    }

    /**
     * Resolve the given path against this path.
     *
     * <p> If the {@code other} parameter is an {@link #isAbsolute() absolute}
     * path then this method trivially returns {@code other}. If {@code other}
     * is an <i>empty path</i> then this method trivially returns this path.
     * Otherwise this method considers this path to be a directory and resolves
     * the given path against this path. In the simplest case, the given path
     * does not have a {@link #getRoot root} component, in which case this method
     * <em>joins</em> the given path to this path and returns a resulting path
     * that {@link #endsWith ends} with the given path. Where the given path has
     * a root component then resolution is highly implementation dependent and
     * therefore unspecified.
     *
     * @param other
     *          the path to resolve against this path
     *
     * @return the resulting path
     *
     * @throw UnsupportedOperationException if {@code other} is relative, not-empty and on a different filesystem.
     * @see #relativize
     */
    @Override
    Path resolve(Path other) {
        if(other.absolute) {
            other
        } else if (!other.nameCount) {
            this
        } else if (fileSystem != other.fileSystem) {
            throw new UnsupportedOperationException("Cannot resolve ${other} (${other.class.name}) against " +
                "${this} (${this.class.name}) as they are from different filesystems")
        } else {
            AbstractPosixPath<T> otherPath = (AbstractPosixPath<T>)other
            List<String> newElements = elements + otherPath.elements
            newElements.empty ? this : createPath(
                "${this.absolute ? separator : ''}${newElements[0]}",
                (newElements.size() == 1 ? [] : newElements[1..-1]) as String[]
            )
        }
    }

    /**
     * Converts a given path string to a {@code Path} and resolves it against
     * this {@code Path} in exactly the manner specified by the {@link
     * # resolve ( Path ) resolve} method. For example, suppose that the name
     * separator is "{@code /}" and a path represents "{@code foo/bar}", then
     * invoking this method with the path string "{@code gus}" will result in
     * the {@code Path} "{@code foo/bar/gus}".
     *
     * @param other
     *          the path string to resolve against this path
     *
     * @return the resulting path
     *
     * @throws <a href="https://docs.oracle.com/javase/8/docs/api/java/nio/file/InvalidPathException.html">InvalidPathException</a>
     *          if the path string cannot be converted to a Path.
     *
     * @see java.io.FileSystem#getPath
     */
    @Override
    Path resolve(String other) {
        resolve(createPath(other))
    }

    /** The path separator is whatever the filesystem separator is
     *
     * @return Path separator
     */
    @Override
    protected String getSeparator() {
        fileSystem.separator
    }

    /** Returns the schema that is associated with this path.
     *  The default implementation queries the provider of the filesystem.
     *
     * @return String representing scheme
     */
    protected String getScheme() {
        fileSystem.provider().scheme
    }

    /** Returns the string that represent an alias for the current directory.
     *
     * @return Current directory alias or null if the filesystem does have a current directory alias string.
     */
    @Override
    protected String getCurrentDirAlias() { CURRENT_DIR }

    /** Returns the string that represent an alias for the parent directory.
     *
     * @return Parent directory alias or null if the filesystem does have a parent directory alias string.
     */
    @Override
    protected String getParentDirAlias() { PARENT_DIR }

    /** Returns the path as separator-delimited segments. If the path is absolute it is prefixed by
     * the separator.
     */
    protected String getPathAsStr() {
        "${absolute ? separator : ''}${elements.join(separator)}"
    }

    /** Create a path  instance from one or more segments.
     *
     * @param part Base part of the path
     * @param more Optional additional segments
     * @return A new path type suitable for filesystem of {@code T}.
     */
    protected abstract Path createPath( final String part ,final String ... more )

    /** Creates an empty path suitable for manipulation of the given {@code T} filesystem.
     *
     * @return A new path type suitable for filesystem of {@code T}.
     */
    protected abstract Path createPath( )

    /** Returns an absolute root for the context of this filesystem. Details are highly
     * system-specific.
     *
     * A trivial implementation may just simply return the root path fo the filesystem.
     * Complex implementations may interrogate other details i.e. the default file provider looks
     * at the current working directory.
     *
     * @return Absolute root path of file system within context.
     */
    @Override
    protected Path getResolvableRoot() {
        AbstractPosixPath<T> rootPath = (AbstractPosixPath<T>)createPath(separator)
        rootPath.markAbsolute()
        rootPath
    }

    // Forces a path to be absolute
    private Path markAbsolute() {
        this.absolute = true
        this
    }

    // Marks the provided path absolute if this instance is also absolute
    private Path followAbsoluteStatus(AbstractPosixPath<T> path) {
        if(absolute) {
            path.markAbsolute()
        }
        path
    }

    // Splits a path string into segments using the path separator
    private static List<String> split(final String separator, final String path) {
        List<String> ret = path.split(separator) as List
        ret.removeIf { it.empty }
        return ret
    }

    protected List<String> elements
    protected final String authority
    protected final T fileSystem
    private boolean absolute = false

    private final String CURRENT_DIR = '.'
    private final String PARENT_DIR = '..'
}
