/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2015
 *
 * This software is licensed under the Apache License 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0 for license details
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * This code has been shamelessly poached from the Ant code source base and
 * adapted to fit within a VFS context
 * ============================================================================
 */
package org.ysb33r.groovy.dsl.vfs.impl

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.apache.commons.vfs2.FileSelectInfo
import org.apache.commons.vfs2.FileSelector
import org.ysb33r.groovy.dsl.vfs.impl.ant.SelectorUtils

/** Allows for Ant-style patterns to be used for selecting files. If no include patterns are specified
 * then everything will be included. If some include patterns are specifed only matching files and folders
 * will be included.  If exlcude patterns are specified, files and folders are only included, if they do
 * not match any exclude patterns.
 *
 * Patterns may include:
 *  <li> to match any number of characters
 *  <li> to match any single character
 *  <li> '**' to match any number of directories or files
 *  <p>
 *  Unlike GRadle or Ant '\' cannot be used for folder separation. Only '/' is allowed as all references
 *  are relative URIs
 *  </p>
 * @author Schalk W. Cronjé
 */
@CompileStatic
class AntPatternSelector implements FileSelector, Cloneable {

    boolean caseSensitive = true

    /**
     * Determines if a file or folder should be selected.  This method is
     * called in depthwise order (that is, it is called for the children
     * of a folder before it is called for the folder itself).
     *
     * @param fileInfo the file or folder to select.
     * @return true if the file should be selected.
     * @throws Exception if an error occurs.
     */
    @Override
    boolean includeFile(FileSelectInfo fileInfo)  {
        allowed(fileInfo)
    }

    /**
     * Determines whether a folder should be traversed.  If this method returns
     * true, {@link #includeFile} is called for each of the children of
     * the folder, and each of the child folders is recursively traversed.
     * <p/>
     * <p>This method is called on a folder before {@link #includeFile}
     * is called.
     *
     * @param fileInfo the file or folder to select.
     * @return true if the folder should be traversed.
     * @throws Exception if an error occurs.
     */
    @Override
    boolean traverseDescendents(FileSelectInfo fileInfo)  {
        allowed(fileInfo)
    }


    /** Adds one of more exclude patterns
     *
     * @return
     */
    AntPatternSelector 	exclude(Iterable<String> excludes) {
        addToPatternSet(this.excludes,excludes)
    }

    /** Adds one of more exclude patterns
     *
     * @return
     */
    AntPatternSelector 	exclude(String... excludes) {
        addToPatternSet(this.excludes,excludes as List)
    }

    /** Returns the current set of exclude patterns
     */
    Set<String> getExcludes() {
        this.excludes.keySet()
    }

    /** Returns the current set of include patterns
     */
    Set<String> getIncludes() {
        this.includes.keySet()
    }

    /** Adds one of more include patterns
     *
     * @param includes One or more ANT patterns
     * @return
     */
    AntPatternSelector 	include(Iterable<String> includes){
        addToPatternSet(this.includes,includes)
    }

    /** Adds one of more include patterns
     *
     * @param includes One or more ANT patterns
     * @return
     */
    AntPatternSelector 	include(String... includes) {
        addToPatternSet(this.includes,includes as List)
    }

    /** Replaces the current set of excludes with a new set
     *
     * @param excludes One of more ANT patterns
     * @return
     */
    AntPatternSelector 	setExcludes(Iterable<String> excludes) {
        this.excludes.clear()
        addToPatternSet(this.excludes,excludes)
    }

    /** Replaces the current set of includes with a new set
     *
     * @param includes One of more ANT patterns
     * @return
     */
    AntPatternSelector 	setIncludes(Iterable<String> includes) {
        this.includes.clear()
        addToPatternSet(this.includes,includes)
    }

    @PackageScope
    boolean allowed(FileSelectInfo fileInfo) {
        // Strip fileInfo.baseFolder from fileInfo.file, meaning get a relative path,
        // which can be used for pattern matching.
        allowed(fileInfo.file.name.friendlyURI - fileInfo.baseFolder.name.friendlyURI)
    }

    @PackageScope
    boolean allowed(final String subject) {
        // Split into parts
        def parts = splitRelativeURI(subject)

        // Apply include patterns
        boolean includeIt = false
        if(!this.includes.empty) {
            includeIt = matched(parts,this.includes)
        } else {
            includeIt = true
        }

        if(!this.excludes.empty && includeIt) {
            includeIt = !matched(parts,this.excludes)
        }

        includeIt
    }


    @PackageScope
    boolean matched(final String[] parts,Map<String,String[] > patternSet) {
        patternSet.values().find { String[] v ->
            SelectorUtils.matchPath(v,parts,caseSensitive)
        }
    }

    @PackageScope
    String[] splitRelativeURI(final String subject) {
        SelectorUtils.tokenizePathAsArray(subject)
    }

    @CompileDynamic
    private AntPatternSelector addToPatternSet(Map<String,String[] > patternSet,def iterableList) {
        iterableList.collect {
            patternSet[it.toString()]= SelectorUtils.tokenizePathAsArray(it)
        }
        this
    }

    private Map<String,String[] > includes = [:] // Value will be a tokenised path
    private Map<String,String[] > excludes = [:]
}
