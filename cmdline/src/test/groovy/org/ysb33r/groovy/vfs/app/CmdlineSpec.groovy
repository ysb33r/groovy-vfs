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

import org.junit.Ignore
import spock.lang.*

class CmdlineSpec extends Specification {

    @Shared String u1='ftp://uri/1'
    @Shared String u2='ftp://uri/2'
    @Shared StringWriter errors
    @Shared StringWriter usage
    @Shared Cmdline cmdline
    @Shared Cmdline cmdline_E
    @Shared Cmdline cmdline_U

    void setup() {
        errors = new StringWriter()
        usage = new StringWriter()
        cmdline = new Cmdline(name: 'TEST')
        cmdline_E = new Cmdline(name: 'TEST_E', errorWriter: new PrintWriter(errors))
        cmdline_U = new Cmdline(name: 'TEST_U', usageWriter: new PrintWriter(usage))
    }

    def "A non-scheme URI should convert to a file: URI"() {
        given:
            def root = new org.ysb33r.groovy.dsl.vfs.URI( new File('/'))
        expect:
            cmdline.buildURIs( ['/a/file'] )[0].toString() == root.toString() + 'a/file'
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
            usage =~ /\[3\] Runs a script of commands as in \[5\] \(drop leading TEST_U\)/
            cmd instanceof Integer
    }

    def "-h on it's own should display basic help page"() {
        given:
            def cmd = cmdline_U.parse(['-h'] as String[]) as Integer
            cmdline_U.usageWriter.flush()

        expect:
            cmd instanceof Integer
            usage =~ /\[2\] TEST_U --version/
            usage =~ /\[5\] TEST_U COMMAND \[COMMAND-OPTIONS\] URIs\.{3}/
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
        // We don't test for the version number itself as this requires the jar to be loaded
        usage =~ /TEST_U version /
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
            ['--parents',u1,u2]      || true  | null
            ['-p',u1,u2]             || true  | null
            [u1,u2]                  || false | null
            ['--mode=0777',u1,u2]    || false | 511i
            ['-m','0555',u1,u2]      || false | 365i
    }

    def "mkdir --mode should accept chmod-style octal values"() {
        given:
            Closure parser = cmdline.findParser(['mkdir'] as String[])

        expect:
            parser(['--mode=099',u1,u2] as String[]) == null
            parser(['-m','199',u1,u2] as String[]) == null
            parser(['--mode=7',u1,u2] as String[]) == null
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

    def "mkdir should not set interactive mode"() {
        given:
            Mkdir cmd = cmdline.parse(['mkdir','./local/file'] as String[]) as Mkdir

        expect:
            cmd.isInteractive() == false
    }

    def "cat should not set interactive mode"() {
        given:
            Cat cmd = cmdline.parse(['cat','./local/file'] as String[]) as Cat

        expect:
            cmd.isInteractive() == false
    }

    def "cat with two uris should set them on Cat object"() {
        given:
        Cat cmd = cmdline.parse(['cat','./local/file','ftp://some.server/pub/file'] as String[]) as Cat

        expect:
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
            opts                 || nel   | seol  | nl    | srel  | st    | snp
            [u1,u2]              || false | false | false | false | false | false
            ['-u',u1,u2]         || false | false | false | false | false | false
            ['-E',u1,u2]         || false | true  | false | false | false | false
            ['--show-ends',u1,u2]|| false | true  | false | false | false | false
            ['-n',u1,u2]         || false | false | true  | false | false | false
            ['--number',u1,u2]   || false | false | true  | false | false | false
            ['-b',u1,u2]         || true  | false | false | false | false | false
            ['--number-nonblank',u1,u2]||true|false|false | false | false | false
            ['-s',u1,u2]         || false | false | false | true  | false | false
            ['--squeeze-blank',u1,u2]||false|false| false | true  | false | false
            ['-T',u1,u2]         || false | false | false | false | true  | false
            ['--show-tabs',u1,u2]|| false | false | false | false | true  | false
            ['-v',u1,u2]         || false | false | false | false | false | true
            ['--show-nonprinting',u1,u2]|| false | false | false | false | false  | true
            ['-A',u1,u2]         || false | true  | false | false | true  | true
            ['--show-all',u1,u2] || false | true  | false | false | true  | true
            ['-e',u1,u2]         || false | true  | false | false | false | true
            ['-t',u1,u2]         || false | false | false | false | true  | true

    }

    def "mv options -i, -f, -n is last option wins"() {
        given:
            Closure parser = cmdline.findParser(['mv'] as String[])

        expect:
            Mv cmd= parser(opts as String[]) as Mv
            result == ((cmd.overwrite instanceof Closure) ? null : cmd.overwrite)
            interactive == cmd.isInteractive()

        where:
            opts                   || result | interactive
            ['-i','-f','-n',u1,u2] || false  | false
            ['-f','-i','-n',u1,u2] || false  | false
            ['-i','-n','-f',u1,u2] || true   | false
            ['-n','-i','-f',u1,u2] || true   | false
            ['-f','-n','-i',u1,u2] || null   | true
            ['-n','-f','-i',u1,u2] || null   | true
    }

    def "If -T is used with mv, then only two URIs are allowed"() {
        given:
            Closure parser = cmdline_E.findParser(['mv'] as String[])
            def result= parser(['-T','./local/file','ftp://some.server/pub/file',
                               'sftp://third/broken'] as String[])

        expect:
            result == null
            errors =~ /Only two URIs are allowed with -T/
    }

    def "If -T is used with mv and two URIs are provided, destination is a file"() {
        given:
            Closure parser = cmdline.findParser(['mv'] as String[])

        when:
            Mv cmd= parser(['-T','./local/file','ftp://some.server/pub/file'] as String[]) as Mv

        then:
            cmd.targetIsFile == true
    }

    def "mv should accept GNU mv options"() {
        given:
            Closure parser = cmdline.findParser(['mv'] as String[])

        expect:
            Mv cmd= parser(opts as String[]) as Mv
            cmd?.update                      == upd
            cmd?.verbose                     == ver
//        def backupStrategy
        where:
            opts                               || upd   | ver
            [u1,u2]                            || false | false
            ['-u',u1,u2]                       || true  | false
            ['--update',u1,u2]                 || true  | false
            ['-v',u1,u2]                       || false | true
            ['--verbose',u1,u2]                || false | true
    }

    def "mv --parents"() {
        given:
            Closure parser = cmdline.findParser(['mv'] as String[])

        expect:
            Mv cmd= parser(opts as String[]) as Mv
            cmd?.intermediates                      == result

        where:
            opts                               || result
            [u1,u2]                            || false
            ['--parents',u1,u2]                || true
    }

    def "mv --strip-trailing-slashes should update only src uris"() {
        given:
        Closure parser = cmdline.findParser(['mv'] as String[])

        when:
        Mv cmd1= parser(['--strip-trailing-slashes','ftp://uri/1/','ftp://uri/2/'] as String[]) as Mv

        then:
            cmd1.sources[0].toString() == 'ftp://uri/1'
            cmd1.destination.toString() == 'ftp://uri/2/'
    }

    def "When using -t with mv, the directory should be added to the end of the URI list"() {
        given:
            Closure parser = cmdline.findParser(['mv'] as String[])

        when:
            Mv cmd1= parser(['-t',u1,u2] as String[]) as Mv
            Mv cmd2= parser(["--target-directory=${u2}",u1] as String[]) as Mv

        then:
            cmd1.sources[0].toString() == u2
            cmd1.destination.toString() == u1
            cmd2.sources[0].toString() == u1
            cmd2.destination.toString() == u2

    }

    @Ignore
    def "mv with -b or --backup affects backupStrategy"() {
        // -b
        // --backup=<>
        // cmd.backupStrategy
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
   }


    def "cp options -i, -f, -n is last option wins"() {
        given:
            Closure parser = cmdline.findParser(['cp'] as String[])

        expect:
            Cp cmd= parser(opts as String[]) as Cp
            result == ((cmd.overwrite instanceof Closure) ? null : cmd.overwrite)
            interactive == cmd.isInteractive()

        where:
            opts                   || result | interactive
            ['-i','-f','-n',u1,u2] || false  | false
            ['-f','-i','-n',u1,u2] || false  | false
            ['-i','-n','-f',u1,u2] || true   | false
            ['-n','-i','-f',u1,u2] || true   | false
            ['-f','-n','-i',u1,u2] || null   | true
            ['-n','-f','-i',u1,u2] || null   | true
    }

    def "If -T is used with cp, then only two URIs are allowed"() {
        given:
            Closure parser = cmdline_E.findParser(['cp'] as String[])
            def result= parser(['-T','./local/file','ftp://some.server/pub/file',
                                'sftp://third/broken'] as String[])

        expect:
            result == null
            errors =~ /Only two URIs are allowed with -T/
    }

    def "If -T is used with cp and two URIs are provided, destination is a file"() {
        given:
            Closure parser = cmdline.findParser(['cp'] as String[])

        when:
            Cp cmd= parser(['-T','./local/file','ftp://some.server/pub/file'] as String[]) as Cp

        then:
            cmd.targetIsFile == true
    }

    def "cp should accept GNU cp options"() {
        given:
            Closure parser = cmdline.findParser(['cp'] as String[])

        expect:
            Cp cmd= parser(opts as String[]) as Cp
            cmd?.update                      == upd
            cmd?.verbose                     == ver
            cmd?.recursive                   == rec
            //  def backupStrategy
            //  --remove-destination (remove each existing destination file before attempting to open it)

        where:
            opts                               || upd   | ver   | rec
            [u1,u2]                            || false | false | false
            ['-u',u1,u2]                       || true  | false | false
            ['--update',u1,u2]                 || true  | false | false
            ['-v',u1,u2]                       || false | true  | false
            ['--verbose',u1,u2]                || false | true  | false
            ['-R',u1,u2]                       || false | false | true
            ['-r',u1,u2]                       || false | false | true
            ['--recursive',u1,u2]              || false | false | true
    }

    def "cp --strip-trailing-slashes should update only src uris"() {
        given:
            Closure parser = cmdline.findParser(['cp'] as String[])

        when:
            Cp cmd1= parser(['--strip-trailing-slashes','ftp://uri/1/','ftp://uri/2/'] as String[]) as Cp

        then:
            cmd1.sources[0].toString() == 'ftp://uri/1'
            cmd1.destination.toString() == 'ftp://uri/2/'
    }

    def "When using -t with cp, the directory should be added to the end of the URI list"() {
        given:
            Closure parser = cmdline.findParser(['cp'] as String[])

        when:
            Cp cmd1= parser(['-t',u1,u2] as String[]) as Cp
            Cp cmd2= parser(["--target-directory=${u2}",u1] as String[]) as Cp

        then:
            cmd1.sources[0].toString() == u2
            cmd1.destination.toString() == u1
            cmd2.sources[0].toString() == u1
            cmd2.destination.toString() == u2
    }

    @Ignore
    def "cp with -b or --backup affects backupStrategy"() {
        // -b
        // --backup=<>
        // cmd.backupStrategy
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
    }



}