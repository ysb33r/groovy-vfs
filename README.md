A DSL for Groovy to wrap around the Apache VFS libraries.

If you like it, then tweet about it using ```#groovyvfs``` as the hashtag.

Groovy Code
===========
```groovy

@Grapes([
    @GrabResolver( name='grysb33r', root='http://dl.bintray.com/ysb33r/grysb33r' ),
	@Grab( 'org.ysb33r.groovy:groovy-vfs:0.4' ),
	@Grab( 'commons-net:commons-net:3.+' ), // If you want to use ftp 
    @Grab( 'commons-httpclient:commons-httpclient:3.1'), // If you want http/https
    @Grab( 'com.jcraft:jsch:0.1.48' ) // If you want sftp
])
import org.ysb33r.groovy.dsl.vfs.VFS

def vfs = new VFS()
 
// Simple copy operation
vfs.cp 'ftp://foo.example/myfile', 'sftp://bar.example/yourfile'
 
// Utilising the DSL
vfs {
   
    // Copy file from one site to anther using two different protocols
    cp 'http://first.example/myfile', 'sftp://second.example/yourfile'
 
    // Not implemented yet - move file between two sites using different protocols
    mv 'sftp://second.example/yourfile', 'ftp://third.example/theirfile'
 
    // Lists all files on a remote site
    ls ('http://first.example') {
        println it.name
    }
  
    // Streams the output
    cat ('http://first.example/myfile') { strm->
        println strm.text
    }
 
    // Create a new folder on a remote site
    mkdir 'sftp://second.example/my/new/folder'
    
    // Change default options via property Map
    options 'vfs.ftp.passiveMode' : true
 
    // Change default options DSL style
    options {
        ftp {
            passiveMode true
        }
    }
 
    // Use options on a per URL basis
    cp 'ftp://first.example/myfile?vfs.ftp.passiveMode=1', 'sftp://second.example/yourfile?vfs.sftp.compression=zlib'
    
    // Download a compressed archive and unpack to local directory
    cp 'tbz2:ftp:/first.example/myFiles.tar.bz2", new File( '../unpack-here' ), recursive:true
     
}
```


Gradle plugin (EXPERIMENTAL)
=============

It is now possible to use this in Gradle as an extension to the project class.
The interface is very experimental and may change without much warning in future
releases of this plugin.

```groovy

buildscript {
    repositories {
        mavenCentral()
        mavenRepo(url: 'http://repository.codehaus.org')
        mavenRepo(url: 'http://dl.bintray.com/ysb33r/grysb33r')
      }
      dependencies {
        classpath 'org.ysb33r.gradle:vfs-gradle-plugin:0.5.1'
        classpath 'commons-net:commons-net:3.+'  // If you want to use ftp 
        classpath 'commons-httpclient:commons-httpclient:3.1' // If you want http/https
        classpath 'com.jcraft:jsch:0.1.48'  // If you want sftp
      }
}
apply plugin : 'org.ysb33r.vfs'

// Create a VFS task
task copyReadme << { 
  vfs {
    cp 'https://raw.github.com/ysb33r/groovy-vfs/master/README.md', new File("${buildDir}/tmp/README.md")
  }
}

// it is also possible to update global options for vfs
vfs {
  options {
    http {
      maxTotalConnections 4
    }
  }
}


```

If you want to see what VFS is going run gradle with --debug

Documentation
=============

See https://github.com/ysb33r/groovy-vfs/wiki for more detailed documentation.

Credits
=======

It is seldom that these kind of libraries happen in isolation. It is therefore prudent 
that I acknowledge the inputs of others in the creation of groovy-vfs

+ Luke Daley (https://gist.github.com/alkemist/7943781) for helping to use Ratpack as a Mock HTTP Server in unit tests.
+ Will_lp (https://gist.github.com/will-lp/5785180) & Jim White (https://gist.github.com/jimwhite/5784982) 
offered great suggestions when I got stuck with the config DSL.
+ Jez Higgins, Rob Fletcher, Giovanni Asproni, Balachandran Sivakumar, Burkhard Kloss & Tim Barker who helped shape the design decision
to auto-create intermediates during a move operation.
