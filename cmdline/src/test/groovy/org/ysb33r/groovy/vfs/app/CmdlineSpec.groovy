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

import spock.lang.*

class CmdlineSpec extends Specification {

    @Shared StringWriter errors
    @Shared StringWriter usage
    @Shared Cmdline cmdline
    @Shared Cmdline cmdline_E
    @Shared Cmdline cmdline_U
    //def cmdline = new Cmdline(name:'TEST')

    void setup() {
        errors = new StringWriter()
        usage = new StringWriter()
        cmdline = new Cmdline(name: 'TEST')
        cmdline_E = new Cmdline(name: 'TEST_E', errorWriter: new PrintWriter(errors))
        cmdline_U = new Cmdline(name: 'TEST_U', usageWriter: new PrintWriter(usage))
        //def cmdline = new Cmdline(name:'TEST')
    }

    def "A non-scheme URI should convert to a file: URI"() {
        expect:
            cmdline.buildURIs( ['/a/file'] )[0].toString() == 'file:///a/file'
    }

    def "No command-line parameters should display error msg and exit"() {
        given:
            def parser = cmdline_E.findParser([] as String[])

        expect:
            parser == null

    }

    def "--help on it's own should display basic help page"() {
        given:
            def cmd = cmdline_U.parse(['--help'] as String[])
            cmdline_U.usageWriter.flush()


        expect:
            usage =~ /Usage: \[1\] TEST_U --help/
            usage =~ /\[1\] Display this help and exit/
            usage =~ /\[3\] TEST_U --script=ScriptFileName/
            usage =~ /\[3\] Runs a script of commands as in \[4\] \(drop leading TEST_U\)/
            cmd instanceof Integer
    }

    def "-h on it's own should display basic help page"() {
        given:
            def cmd = cmdline_U.parse(['-h'] as String[]) as Integer
            cmdline_U.usageWriter.flush()

        expect:
            cmd instanceof Integer
            usage =~ /\[2\] TEST_U --version/
            usage =~ /\[4\] TEST_U COMMAND \[COMMAND-OPTIONS\] URIs\.{3}/
            usage =~ /\[2\] Display program version and exit/

    }

    def "--version displays version and exits"() {
        given:
            def cmd = cmdline_U.parse(['--version'] as String[]) as Integer
            cmdline_U.usageWriter.flush()

        expect:
            cmd instanceof Integer
            usage =~ /TEST_U version /
    }

    def "-V displays version and exits"() {
        given:
        def cmd = cmdline_U.parse(['-V'] as String[]) as Integer
        cmdline_U.usageWriter.flush()

        expect:
        cmd instanceof Integer
        // We don't rest for the version number itself as this requires the jar to be loaded
        usage =~ /TEST_U version /
    }

    @Ignore
    def "--help / -h with command should display help for command"() {
        expect:
            cmdline.findParser(args as String[]) == closure

        where:
            args                || closure
            ['mkdir','--help']  || cmdline.help
            ['cp','-h']         || cmdline.help
            ['mv','--parents','ftp://from.here/file','ftp://to.here/file','--help','--version'] || Cmdline.help
    }

    def "mkdir should accept -p, --parents , -m and --mode"() {
        given:
            Closure parser = cmdline.findParser(['mkdir'] as String[])

        expect:
            Mkdir cmd= parser(opts as String[]) as Mkdir
            cmd?.intermediates == ints
            cmd?.mode == mode

        where:
            opts                     || ints  | mode
            ['--parents']            || true  | null
            ['-p']                   || true  | null
            []                       || false | null
            ['--mode=0777']          || false | 511i
            ['-m','0555']            || false | 365i
    }

    def "mkdir --mode should accept chmod-style octal values"() {
        given:
            Closure parser = cmdline.findParser(['mkdir'] as String[])

        expect:
            parser(['--mode=099'] as String[]) == null
            parser(['-m','199'] as String[]) == null
            parser(['--mode=7'] as String[]) == null
    }

    def "mkdir usage should be invoked by --help"() {
        given:
            def parser = cmdline_U.parse(['mkdir','--help'] as String[])

        expect:
            parser == null
            usage =~ /-p,--parents\s+Make parent directories as needed/
    }

    def "mkdir with two uris should update set them on Mkdir object"() {
        given:
            Mkdir cmd = cmdline.parse(['mkdir','./local/dir','ftp://some.server/pub/dir'] as String[]) as Mkdir

        expect:
            cmd != null
            cmd.uris.size() == 2
            cmd.uris[0].toString().startsWith('file://')
            cmd.uris[1].toString() == 'ftp://some.server/pub/dir'
    }

    def "cat with two uris should update set them on Cat object"() {
        given:
        Cat cmd = cmdline.parse(['cat','./local/file','ftp://some.server/pub/file'] as String[]) as Cat

        expect:
        cmd != null
        cmd.uris.size() == 2
        cmd.uris[0].toString().startsWith('file://')
        cmd.uris[1].toString() == 'ftp://some.server/pub/file'
    }

    def "cat should accept GNU cat options"() {
        given:
        Closure parser = cmdline.findParser(['cat'] as String[])

        expect:
            Cat cmd= parser(opts as String[]) as Cat
            cmd?.numberNonEmptyLines        == nel
            cmd?.showEndOfLines             == seol
            cmd?.numberLines                == nl
            cmd?.suppressRepeatedEmptyLines == srel
            cmd?.showTabs                   == st
            cmd?.showNonPrinting            == snp

        where:
            opts           || nel   | seol  | nl    | srel  | st    | snp
            []             || false | false | false | false | false | false
            ['-u']         || false | false | false | false | false | false
            ['-E']         || false | true  | false | false | false | false
            ['--show-ends']|| false | true  | false | false | false | false
            ['-n']         || false | false | true  | false | false | false
            ['--number']   || false | false | true  | false | false | false
            ['-b']         || true  | false | false | false | false | false
            ['--number-nonblank']||true|false|false | false | false | false
            ['-s']         || false | false | false | true  | false | false
            ['--squeeze-blank']||false|false| false | true  | false | false
            ['-T']         || false | false | false | false | true  | false
            ['--show-tabs']|| false | false | false | false | true  | false
            ['-v']         || false | false | false | false | false | true
            ['--show-nonprinting']|| false | false | false | false | false  | true
            ['-A']         || false | true  | false | false | true  | true
            ['--show-all'] || false | true  | false | false | true  | true
            ['-e']         || false | true  | false | false | false | true
            ['-t']         || false | false | false | false | true  | true

    }


//    ﻿SYNOPSIS
//    mv [OPTION]... [-T] SOURCE DEST
//    mv [OPTION]... SOURCE... DIRECTORY
//    mv [OPTION]... -t DIRECTORY SOURCE...
//
//    DESCRIPTION
//    Rename SOURCE to DEST, or move SOURCE(s) to DIRECTORY.
//
//    Mandatory arguments to long options are mandatory for short options too.
//
//    --backup[=CONTROL]
//    make a backup of each existing destination file
//
//    -b     like --backup but does not accept an argument
//
//    -f, --force
//    do not prompt before overwriting
//
//    -i, --interactive
//    prompt before overwrite
//
//    -n, --no-clobber
//    do not overwrite an existing file
//
//    If you specify more than one of -i, -f, -n, only the final one takes effect.
//
//    --strip-trailing-slashes
//    remove any trailing slashes from each SOURCE argument
//
//    -S, --suffix=SUFFIX
//    override the usual backup suffix
//
//    ﻿      -t, --target-directory=DIRECTORY
//    move all SOURCE arguments into DIRECTORY
//
//    -T, --no-target-directory
//    treat DEST as a normal file
//
//    -u, --update
//    move  only  when the SOURCE file is newer than the destination file or
//    when the destination file is missing
//
//    -v, --verbose
//    explain what is being done
//
//    --help display this help and exit
//
//    --version
//    output version information and exit
//
//    The backup suffix is '~', unless set with --suffix  or  SIMPLE_BACKUP_SUFFIX.
//    The version control method may be selected via the --backup option or through
//    the VERSION_CONTROL environment variable.  Here are the values:
//
//    none, off
//    never make backups (even if --backup is given)
//
//    numbered, t
//    make numbered backups
//    ﻿       existing, nil
//    numbered if numbered backups exist, simple otherwise
//
//    simple, never
//    always make simple backups

//    ﻿SYNOPSIS
//    cp [OPTION]... [-T] SOURCE DEST
//    cp [OPTION]... SOURCE... DIRECTORY
//    cp [OPTION]... -t DIRECTORY SOURCE...
//
//    DESCRIPTION
//    Copy SOURCE to DEST, or multiple SOURCE(s) to DIRECTORY.
//
//    Mandatory arguments to long options are mandatory for short options too.
//
//    -a, --archive
//    same as -dR --preserve=all
//
//    --attributes-only
//    don't copy the file data, just the attributes
//
//    --backup[=CONTROL]
//    make a backup of each existing destination file
//
//    -b     like --backup but does not accept an argument
//
//    --copy-contents
//    copy contents of special files when recursive
//
//    -d     same as --no-dereference --preserve=links
//
//    ﻿       -f, --force
//    if  an  existing  destination file cannot be opened, remove it and try
//    again (redundant if the -n option is used)
//
//    -i, --interactive
//    prompt before overwrite (overrides a previous -n option)
//
//    -H     follow command-line symbolic links in SOURCE
//
//    -l, --link
//    hard link files instead of copying
//
//    -L, --dereference
//    always follow symbolic links in SOURCE
//
//    -n, --no-clobber
//    do not overwrite an existing file (overrides a previous -i option)
//
//    -P, --no-dereference
//    never follow symbolic links in SOURCE
//
//    -p     same as --preserve=mode,ownership,timestamps
//
//    --preserve[=ATTR_LIST]
//    preserve  the  specified  attributes  (default:   mode,ownership,time‐
//    stamps), if possible additional attributes: context, links, xattr, all
//
//    --no-preserve=ATTR_LIST
//    don't preserve the specified attributes
//
//    --parents
//    use full source file name under DIRECTORY
//    ﻿       -R, -r, --recursive
//    copy directories recursively
//
//    --reflink[=WHEN]
//    control clone/CoW copies. See below
//
//    --remove-destination
//    remove  each  existing  destination  file before attempting to open it
//    (contrast with --force)
//
//    --sparse=WHEN
//    control creation of sparse files. See below
//
//    --strip-trailing-slashes
//    remove any trailing slashes from each SOURCE argument
//
//    -s, --symbolic-link
//    make symbolic links instead of copying
//
//    -S, --suffix=SUFFIX
//    override the usual backup suffix
//
//    -t, --target-directory=DIRECTORY
//    copy all SOURCE arguments into DIRECTORY
//
//    -T, --no-target-directory
//    treat DEST as a normal file
//
//    -u, --update
//    copy only when the SOURCE file is newer than the destination  file  or
//    when the destination file is missing
//
//    -v, --verbose
//    explain what is being done
//    ﻿       -x, --one-file-system
//    stay on this file system
//
//    --help display this help and exit
//
//    --version
//    output version information and exit
//
//    By  default,  sparse  SOURCE  files are detected by a crude heuristic and the
//    corresponding DEST file is  made  sparse  as  well.   That  is  the  behavior
//    selected  by  --sparse=auto.  Specify --sparse=always to create a sparse DEST
//    file whenever the SOURCE file contains a long enough sequence of zero  bytes.
//    Use --sparse=never to inhibit creation of sparse files.
//
//    When  --reflink[=always]  is specified, perform a lightweight copy, where the
//    data blocks are copied only when modified.  If this is not possible the  copy
//    fails, or if --reflink=auto is specified, fall back to a standard copy.
//
//    The  backup  suffix is '~', unless set with --suffix or SIMPLE_BACKUP_SUFFIX.
//    The version control method may be selected via the --backup option or through
//    the VERSION_CONTROL environment variable.  Here are the values:
//
//    none, off
//    never make backups (even if --backup is given)
//
//    numbered, t
//    make numbered backups
//
//    existing, nil
//    numbered if numbered backups exist, simple otherwise
//
//    simple, never
//    always make simple backups
//
//    ﻿       As  a  special  case,  cp  makes a backup of SOURCE when the force and backup
//    options are given and SOURCE and DEST are the same name for an existing, reg‐
//    ular file.
//

}