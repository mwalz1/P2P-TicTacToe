# Peer-To-Peer Tic Tac Toe
The 2020-2021 SWE4203 lab project!

## Compilation
You will need to [install Java](https://www3.ntu.edu.sg/home/ehchua/programming/howto/JDK_Howto.html) before you can compile/run this application. Once installed

```
$ javac src/*.java
```

If you are on a Unix based system, you can also you the make command.
```
$ make
```

## Running
Just run the `Main.java` file!
```
cd src
java Main
```

## Development
Ideally, you are on a Linux based system. In this situation, you can easily use the following command to start a hot reload server.
```
# -r because the child process is persistent and -s because we are passing in a shell command
$ ls src/**/*.java src/**/*.html src/**/*.js src/**/*.css | entr -rs 'make && make serve'
```

Note that you will likely have to install [entr](http://eradman.com/entrproject/) before you can run the following command. It can be easily downloaded and installed using the link above or installed using your system's package manager. The following subsections show the commands for a few operating systems.

### Ubuntu
```
$ sudo apt-get update -y
$ sudo apt-get install -y entr
```

### Mac OS
```
$ brew install entr
```
