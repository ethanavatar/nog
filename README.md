# Nog

A Java build system that requires no external programs or third party dependecies-- just Java.

Greatly inspired by [Zig's build system](https://ziglang.org/learn/build-system/) as well as the project [tsoding/nobuild](https://github.com/tsoding/nobuild) (and a strong dislike for Java build systems eg. Gradle).


It is very incomplete in its current state. Using it is not recommended. I also cannot promise that I will ever finish it because I don't like Java very much.

## Usage

Place the file `Nog.java` in the root of your project. This is the library that contains the whole build system. You can interact with the build system by creating a class in the same directory with the package name `NogBuild`

Heres an example of how you could use it to build a simple multi-file project:


---

File: [`Build.java`](./Build.java)

```java
package NogBuild;

class Build {
    public static void main(String[] args) throws Exception {
        Nog.setPackage("Main");

        Module main = Nog.addModule("Main");

        main.addFile("src\\Hello.java");
        main.addFile("src\\Program.java");

        main.build();

        if (args.length > 0 && args[0].equals("run")) {
            main.run("Program");
        }

        // --- Bootstrap ---
        Module build = Nog.addModule("NogBuild");

        build.addFile("Nog.java");
        build.addFile("Build.java");

        build.build();
        build.makeJar("Build.jar", "Build");
        Nog.copyFile(Nog.cachedPath("Build.jar"), Nog.projectPath("Build.jar"));
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

This specific build process has a `run` argument, so it can be passed into the jar file to run the program:

```bash
# Run the project
$ java -jar Build.jar run
```

