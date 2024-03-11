# Nog

A Java build system that requires no external programs or third party dependecies-- just Java.

Greatly inspired by [Zig's build system](https://ziglang.org/learn/build-system/) as well as the project [tsoding/nobuild](https://github.com/tsoding/nobuild) (and a strong dislike for Java build systems eg. Gradle).


It is very incomplete in its current state. Using it is not recommended.

## Usage

Place the file `Nog.java` in the root of your project. This is the library that contains the whole build system. You can interact with the build system by creating a class in the same directory with the package name `NogBuild`

Heres an example of how you could use it to build a simple multi-file project:


---

File: [`Build.java`](./Build.java)

```java
package NogBuild;

import java.util.ArrayList;

class Build {
    public static void main(String[] args) throws Exception {
        bootstrapBuild(args);

        Nog.setPackage("Main");
        Module main = Nog.addModule("Main");

        main.addFile("src", "Hello.java"); // Provides the hello function
        main.addFile("src", "Program.java"); // Uses the hello function

        main.build();

        if (args.length > 0 && args[0].equals("run")) {
            main.run("Program");
        }
    }

    public static void bootstrapBuild(String[] args) throws Exception {
        Module build = Nog.addModule("NogBuild");

        build.addFile("Nog.java");
        build.addFile("Build.java");

        ArrayList<String> modifiedFiles = build.getModifiedUnits();
        if (modifiedFiles.size() == 0) return;
        if (args.length > 0 && args[0].equals("bootstrapped")) return;

        build.build();
        build.makeJar("Build.jar", "Build");

        Nog.runJar(
            Nog.cachedPath("Build.jar"),
            new String[] { "bootstrapped" });

        Nog.copyFile(
            Nog.cachedPath("Build.jar"),
            Nog.projectPath("Build.jar"));

        System.exit(0);
    }
}
```

In order to build the program, first, you need to compile both `Nog.java` and `Build.java` into a jar file:

```bash
# Compile both files into .class files
$ javac Nog.java Build.java -d nog-cache

# Create a jar file with compiled .class files
$ jar cvfe nog-cache/Build.jar NogBuild.Build -C nog-cache NogBuild
```
Using the above example, this jar compilation step only needs to be run once, because the bootstrap phase will make a new jar file if `Build.java` has been modified.

Now you can build the program by running the jar file:

```bash
# Build the project
$ java -jar Build.jar
```

This specific build process handles a `run` argument, so it can be passed into the jar file to run the program:

```bash
# Run the project
$ java -jar Build.jar run
```

