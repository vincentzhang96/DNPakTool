DNPakTool
========

A simple command line tool for listing, extracting, and searching Eyedentity/Dragon Nest resource pak files, as well 
as a Java library for reading and processing them.

##Usage (Command Line)
DNPakTool may be used in an interactive shell-like manner or through startup arguments. The commands and arguments are 
the same.

To run a command, 
```
java -jar DMPakTool-1.0.0.jar command args...
```

To launch in interactive mode, simply pass no arguments as such:
```
java -jar DNPakTool-1.0.0.jar
```
and you will be greeted by a prompt.

| Command | Arguments          | Description                                          |
|---------|--------------------|------------------------------------------------------|
| help    | (none)             | Prints all available commands and their descriptions |
| exit    | (none)             | Exits the program. Aliases: quit, stop               |
| ls      | `files...`         | Prints all the subfile paths within `files...`       |
| find    | `[-r] string file` | Finds all paths in the pak with filename containing the given `string`. `-r` treats `string` as a regex. |
| dump    | `[-ds] [-fr string] src...[*] dest` | Dumps all files in the `src...` paks into the `dest` directory. If a `src` path is terminated by `/*` then the program will attempt to dump all files ending in `.pak`. If `-d` is specified , then the output directory will be recursively emptied before dumping after a confirmation prompt. If `-s` is specified, then the `-d` deletion prompt will be suppressed, **and also implies `-d`.** If `-f` is specified, then only files that match will be dumped (see `find` for details). |

##Usage (Library)
Include DNPakTool-1.0.0.jar in your classpath, or install the library to your local maven repository 
via `mvn install` and adding to your pom.xml 
```xml
<dependency>
    <groupId>co.phoenixlab.dn</groupId>
    <artifactId>DNPakTool</artifactId>
    <version>1.0.0</version>
</dependency>
```
###Loading a pak file
Construct a new [`PakFileReader`]
(https://github.com/vincentzhang96/DNPakTool/blob/master/src/main/java/co/phoenixlab/dn/pak/PakFileReader.java) 
 by passing a valid `Path` to the Pak file and call `PakFileReader.load()`. You may then query the resultant 
 [`PakFile`](https://github.com/vincentzhang96/DNPakTool/blob/master/src/main/java/co/phoenixlab/dn/pak/PakFile.java) 
 for the subfiles. Currently, you are responsible for INFLATEing the data obtained from `PakFile.transferTo()`; see [here]
 (https://github.com/vincentzhang96/DNPakTool/blob/master/src/main/java/co/phoenixlab/dn/pak/DNPakTool.java#L447) 
 for an example on how to do so.

##To Do
- [x] Command line interactive mode
- [x] Command line pass-by-program-argument mode
- [ ] Document everything
- [ ] Support directory stripping (dump file(s) without creating parent directories)
- [ ] Provide a simple API for getting the contents of a given subfile

##Building
To build with Maven, `cd` into the project directory and run `mvn package`.

##Dependencies
- Java 8
- JUnit 4.11 (Testing)
- Mockito 2.04 (Testing)

##License

The MIT License (MIT)

Copyright (c) 2015 Vincent Zhang/PhoenixLAB

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
