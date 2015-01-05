//
// ============================================================================
// (C) Copyright Schalk W. Cronje 2013-2015
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
//
package org.ysb33r.groovy.vfs.app

import groovy.transform.TupleConstructor
import org.ysb33r.groovy.dsl.vfs.VFS
import static java.nio.charset.StandardCharsets.US_ASCII

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

    boolean isInteractive() { false }

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
                            if ((numberNonEmptyLines && !isBlank) || (numberLines && !numberNonEmptyLines)) {
                                out.printf '%6d\t', lineCount
                                ++lineCount
                            }

                            if(showNonPrinting) {
                                line= line.replaceAll(/\p{Cntrl}/) { match ->
                                    if (match[0] != "\n" && match[0] != "\t") {
                                        byte[] tmp = match[0].toString().getBytes(US_ASCII)
                                        if (tmp[0] == 127) {
                                            return "^?"
                                        } else {
                                            tmp[0] = tmp[0] + 64
                                            return "^" + new String(tmp, 0, 1)
                                        }
                                    } else {
                                        return match[0]
                                    }
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

