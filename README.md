jarup
=====

`jarup`, the jar / zip updater tool.


`jarup` is a tool which can be used to easily update the content of a jar, war, ear or actually any zip file.

It is very convenient to use to update configuration files packaged inside a jar. It's a command line tool which can seamlessly be integrated in automated processes.

## Install
`jarup` is packaged as a small (<25kB) executable jar file, so you can simply [download it](https://rawgithub.com/xhanin/jarup/master/dist/jarup.jar) and use it with `java -jar jarup.jar` on any platform with java7 properly installed.

It is more convenient to use it with a shell/batch file named jarup. you can do `java -jar jarup.jar gen-script` to generate a script for your platform.

On Linux/MacOSX you can also use this simple one line install / update:
`curl -s https://rawgithub.com/xhanin/jarup/master/install.sh | sh`

## Usage

### do a search replace in any text file

You can perform search / replace in any text file:

simple search replace with default jarup encoding (UTF-8):

`jarup example.jar search-replace example1.xml TOKEN newvalue`

simple search replace with specific encoding:

`jarup example.jar search-replace example2.xml TOKEN newvalue --encoding=ISO-8859-1`

regular expression search / replace:

`jarup example.jar search-replace example1.xml /{{(.+)}}/$1/`

### set a property in a properties file [NOT IMPLEMENTED YET]

You can easily set a property in a standard java properties file.
The property is replaced if it exists, and set if it doesn't exist.
The file order is preserved.

*Examples:*

simple property update, with default properties file encoding (ISO 8859-1)

`jarup example.jar set-property example.properties property1=newvalue`

same, but with a file located in a directory inside the jar

`jarup example.jar set-property example/example.properties property1=newvalue`

same, but loading properties to set from a file

`jarup example.jar set-properties --from=my.properties  --into=example.properties`

same, but using UTF-8 encoding

`jarup example.jar set-property example2.properties property1=newvalue --encoding=UTF-8`


### cat a file content

You can cat (i.e. output the content of a file in console) any file. It's just more convenient than extracting the file to view it. You can also specify a destination in which case it extracts the file (you can use the `jar` tool for that too). Only the latter option is possible in batch mode.

`jarup example.jar cat example.properties`

`jarup example.jar cat example.properties --to=example.properties`

An alias for `cat` is `extract`.

### replace / add file content

Replace the content of a file or add a new one. This can be performed with `jar` command line utility too, it's just convenient to use.

`jarup example.jar replace --from=my.properties --to=example.properties`

An alias for 'replace' is 'add'.

### work with jars of jars

All commands can be used on files contained in jars contained in jars (or wars / ears / …):

`jarup example.war cat WEB-INF/lib/example.jar:/example.properties`

### batch commands

if you have multiple operations to perform on the target archive, it is highly recommended to batch commands.
If you have only a few commands you can chain them by separating them with a `+`:

`jarup example.jar set-property example.properties property1=newvalue + set-property example3.properties property3=newvalue`

You can also use a text file with line separated list of commands:

`jarup example.jar batch commands.jarup`

with `commands.jarup` being:
```
set-property example.properties property1=newvalue
set-property example3.properties property3=newvalue
```

In batch mode you can also exec arbitrary commands with `exec`. So if you want to use the powerful tools of choice you can use such a `commands.jarup` file:
```
extract example.properties --to=myexample.properties
exec sed -i .bak s/STRING_TO_REPLACE/STRING_TO_REPLACE_IT/g myexample.properties
replace --from=myexample.properties --to=example.properties
```

## How does it work

Implementation is very simple: it unzips your archive in a temporary directory (called a working copy), apply your command(s), and rebuild the archive if is has been updated.
That's why using batch of commands instead of doing multiple calls is recommended.

Note also that the implementation of commands are very simple and usually mount the manipulated file in memory. So if you want to perform a search and replace on a very large file you will need a corresponding amount of memory, or prefer using native commands with surrounded by extract / replace.

## Status

Development was started very recently, but all documented features are now implemented and have good test coverage.

If you want to add new commands it's pretty easy, have a look at [how existing commands are implemented](https://github.com/xhanin/jarup/tree/master/src/main/java/io/github/xhanin/jarup/commands).