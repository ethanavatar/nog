# Nog

A Java build system that requires no external programs-- just Java.

Inspired by [Zig's build system](https://ziglang.org/learn/build-system/) as well as the project [tsoding/nobuild](https://github.com/tsoding/nobuild), and a strong dislike for Gradle and other Java build systems.


It is very incomplete in its current state. Using it is not recommended. I also cannot promise that I will ever finish it because I don't like Java nearly enough to care.

## Usage

Place the file `Nog.java` in the root of your project. This is the library that contains the whole build system. You can interact with the build system by creating a class in the same directory with the package name `NogBuild`

Heres an example of how you could use it to build a simple multi-file project:


---
```java
// File: Build.java

package NogBuild;

public class Build {
    public static void main(String[] args) throws Exception {
        Nog.setPackage("com.example");

        Nog.addFile("Main.java");
        Nog.addFile("Other.java");

        Nog.build();

        if (args[0].equals("run")) {
            Nog.runClass("Main");
        }
    }
}
```

In order to build the project, first, you need to compile both `Nog.java` and `Build.java` into a jar file:

```bash
# Compile both files into .class files in the current directory
$ javac Nog.java Build.java -d .

# Create a jar file with classes in the current directory 
$ jar cvfe build.jar NogBuild.Build -C . .
```
This jar compilation step needs to be run every time `Build.java` is modified.

Now you can build and run the project with the following commands:

```bash
# Build the project
$ java -jar build.jar
```

This specific build process has a `run` argument, so it can be passed into the jar file to run the project:

```bash
# Run the project
$ java -jar build.jar run
```

As it stands, this still only produces `.class` files. Packaging into `.jar` files is something I want to implement in the future.
