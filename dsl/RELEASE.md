Changelog
=========

0.5 - Roadmap
-------------
+ [Using a closure as a filter when copying](https://github.com/ysb33r/groovy-vfs/issues/4)
+ [Allow option not to create intermediate paths when copying](https://github.com/ysb33r/groovy-vfs/issues/2)

0.4 - Roadmap
-------------
+ [Fixed Bug #6](https://github.com/ysb33r/groovy-vfs/issues/6) - Obtain logger instance in use by Groovy VFS
+ [Fixed Bug #8](https://github.com/ysb33r/groovy-vfs/issues/8) - Overwrite fails when using nested schemas
+ [CodeNarc checks added to source code](https://github.com/ysb33r/groovy-vfs/issues/7)
+ Upgraded to Gradle 1.8

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