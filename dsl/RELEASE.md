Changelog
=========

0.6 - Roadmap
-------------
+ Groovy VFS now has it own ```FileSystemManager``` class with more control for loading plugins.
+ Passing ```defaultProvider``` to ```VFS()``` is a NOP.
+ Supports loading of SMB & MIME if the VFS Sandbox JAR is on the classpath and ```scanForVfsProviderXml : true```
is passed to ```VFS()```
+ DSL now has an ```extend``` section for easier addition of plugins
+ [Allow option not to create intermediate paths when copying](https://github.com/ysb33r/groovy-vfs/issues/2)
+ [Using a closure as a filter when copying](https://github.com/ysb33r/groovy-vfs/issues/4)
+ [Delete functionality](https://github.com/ysb33r/groovy-vfs/issues/9)
+ [Allow cookies to be passed to http/https schemas](https://github.com/ysb33r/groovy-vfs/issues/10)
+ [Create complex URIs with a closure](https://github.com/ysb33r/groovy-vfs/issues/11)
+ [Allow option not to auto-create intermediates when create a directory](https://github.com/ysb33r/groovy-vfs/issues/18)
+ [Fixed NPE when use Apache VFS FileSelector as copy filter](https://github.com/ysb33r/groovy-vfs/issues/19)
+ Six pre-defined filters have been added for copy operations:
```exclude_self, select_all, direct_children_only, only_files, only_folders, self_and_direct_children```
+ Added ```isFile```, ```isFolder```, ```mtime``` and ```exists``` keywords to VFS DSL.

0.5
---
+ Removed standalone use of InputStream when applying ```cat``.
+ [Fix use of mavenRepo](https://github.com/ysb33r/groovy-vfs/issues/12)
+ [Fix MissingMethodException when uri was a FileObject and extra properties were provided](https://github.com/ysb33r/groovy-vfs/issues/16)
+ [Fix issue with connection not closing when using cat method](https://github.com/ysb33r/groovy-vfs/issues/15)
+ Source Jar will be created as part of build

0.4
---
+ [Fixed Bug #6](https://github.com/ysb33r/groovy-vfs/issues/6) - Obtain logger instance in use by Groovy VFS
+ [Fixed Bug #8](https://github.com/ysb33r/groovy-vfs/issues/8) - Overwrite fails when using nested schemas
+ [CodeNarc checks added to source code](https://github.com/ysb33r/groovy-vfs/issues/7)
+ Upgraded to Gradle 1.11

0.3
---
+ [Move files and folders between sites](https://github.com/ysb33r/groovy-vfs/issues/3)
+ Copy operations support using a closure for overwrites
+ Builds with JDK6

0.2
---
+ DSL for setting options
+ Replaced ```vfs << {}``` with ```vfs {}```

0.1.1
-----
+ Published in Maven format instead of Ivy (using gradle-bintray plugin)

0.01
----
+ Initial release
+ Support for 'cp', 'ls', 'cat'