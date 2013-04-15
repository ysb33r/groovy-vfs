I have been asked to produce the example code according to a small specification

  Take a number and give the equivalent number in british english words e.g.
  1 = one
  21 = twenty one
  105 = one hundred and five
  56945781 = fifty six million nine hundred and forty five thousand seven hundred and eighty one etc.. up to 999,999,999 without any external libraries (except testing libraries)

I don't believe 56945781's wording is actually correct English. However I have done the implementation 
according to that spec.

Instructions to build:
=====================
( For Windows just change path names to contain \ instead of / )
```
  git clone git://github.com/ysb33r/KroovyBan.git 
  cd KroovyBan
  git config core.sparsecheckout true
  echo SampleProjects/EnglishNumberFormatter/ > .git/info/sparse-checkout
  cd SampleProjects/EnglishNumberFormatter
  gradle build distZip
```

The above will build, test and package the library. The final artefact
will be written to 
```
  build/distributions/EnglishNumberFormatter-0.1.zip
```

Instructions to run a sample conversion:
=======================================  

The JAR has a simple main function to show the conversion in action. To use it
unpack the built zip and then run script with a number i.e

```
  cd EnglishNumberFormatter-0.1
  bin/EnglishNumberFormatter 56945781 
```
