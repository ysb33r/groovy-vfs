// ============================================================================
// (C) Copyright Schalk W. Cronje 2014
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
package org.ysb33r.groovy.vfs.app

import groovy.transform.TupleConstructor
import org.ysb33r.groovy.dsl.vfs.VFS

@TupleConstructor
class Cat implements Cmd {

//    number nonempty output lines, overrides -n
//    display $ at end of each line
//    number all output lines (6 char width)
//    suppress repeated empty output lines
//    display TAB characters as ^I
//    use ^ and M- notation, except for LFD and TAB
    boolean numberNonEmptyLines = false
    boolean showEndOfLines = false
    boolean numberLines = false
    boolean suppressRepeatedEmptyLines = false
    boolean showTabs = false
    boolean showNonPrinting = false
    List<org.ysb33r.groovy.dsl.vfs.URI> uris = []
    PrintStream out = System.out

    Integer run(VFS vfs) {
        if (numberNonEmptyLines || showEndOfLines || numberLines || suppressRepeatedEmptyLines || showTabs || showNonPrinting) {
            modifyOutput(vfs)
        } else {
            quickCat(vfs)
        }
        return 0i
    }

    private void modifyOutput(VFS vfs) {
        vfs {
            uris.each {
                cat(it) { strm ->
                    Long lineCount=1
//                    boolean notFirstLine=false
                    boolean prevLineBlank=false

                    strm.eachLine { line ->
                        boolean printLine = true
                        boolean isBlank = !line.size()

                        // Suppress first before numbering
                        if (suppressRepeatedEmptyLines) {
                            if (prevLineBlank && isBlank) {
                                printLine = false
                            }
                            prevLineBlank = isBlank
                        }

                        if (printLine) {
//                            if (notFirstLine) {
//                                out.println()
//                            }
                            if ((numberNonEmptyLines && !isBlank) || (numberLines && !numberNonEmptyLines)) {
                                out.printf '%6d\t', lineCount
                                ++lineCount
                            }

                            if(showNonPrinting) {
                                line = line.replaceAll(/\t/) { match ->
                                    match
                                }

                            } else if(showTabs) {
                                line = line.replaceAll(/\t/,'^I')
                            }
                            out << line
                            if(showEndOfLines) {
                                out << '$'
                            }
                            out.println()
                        }

                        //notFirstLine = true
                    }
                }
            }
        }
    }

    private void quickCat(VFS vfs) {
        vfs {
            uris.each {
                cat(it) { strm ->
                    out << strm
                }
            }
        }
    }
}

