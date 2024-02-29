package NogBuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

class Cmd {
    public static boolean isWindows() {
        return System.getProperty("os.name")
            .toLowerCase()
            .startsWith("windows");
    }

    private static void runCommand(String[] args) throws Exception {
        System.out.println("+ " + String.join(" ", args));

        String[] prefix = isWindows()
            ? new String[] {"cmd.exe", "/c"}
            : new String[] {"/bin/sh", "-c"};

        String[] command = new String[prefix.length + args.length];
        System.arraycopy(prefix, 0, command, 0, prefix.length);
        System.arraycopy(args, 0, command, prefix.length, args.length);

        Process process = runtime.exec(command);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Process exited with " + exitCode);
        }

        try (BufferedReader stdout = new BufferedReader(
            new InputStreamReader(process.getInputStream())
        )) {
            System.out.print(readOutput(process, stdout));
        }

        try (BufferedReader stderr = new BufferedReader(
            new InputStreamReader(process.getErrorStream())
        )) {
            System.err.print(readOutput(process, stderr));
        }
    }


    private static Runtime rt = Runtime.getRuntime();

    private static String[] getSystemPrefix() {
        if (isWindows()) {
            return new String[] {"cmd", "/c"};
        } else {
            return new String[] {"/bin/sh", "-c"};
        }
    }
}

class BuildArtifact {
    public File file;
    boolean compileOnly;
    public BuildArtifact(File file, boolean compileOnly) {
        this.file = file;
        this.compileOnly = compileOnly;
    }
}

public class Nog {
    /**
     * Add a file to be compiled and packaged in the final build
     */
    public static void addFile(String filePath) {
        addArtifact(filePath, false);
    }

    /**
     * Add a file as a dependency but do not include it in the final build
     */
    public static void compileOnly(String filePath) {
        addArtifact(filePath, true);
    }

    /**
     * Compile and package the project
     */
    public static void build() throws IOException {
        clearCache();

        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }

        ArrayList<String> command = new ArrayList<String>();
        command.add("javac");
        command.add("-d");
        command.add(cacheDir.getPath());

        for (BuildArtifact unit: units) {
            copyToCache(unit.file);
            command.add(unit.file.getPath());
        }

        Cmd.runCommand(command.toArray(new String[0]));
    }

    public static void bootstrap(String nogFilePath, String buildFilePath, String outputJarName, String entry) throws IOException {
        File nogFile = new File(nogFilePath);
        File buildFile = new File(buildFilePath);

        copyToCache(nogFile);
        copyToCache(buildFile);

        Cmd.runCommand(new String[] {
            "javac",
            nogFile.getPath(),
            buildFile.getPath(),
            "-d",
            cacheDir.getPath(),
        });

        Cmd.runCommand(new String[] {
            "jar",
            "cfe",
            outputJarName,
            entry,
            "-C",
            cacheDir.getPath(),
            "NogBuild"
        });

        if (Cmd.isWindows()) {
            command = new String[] {
                "xcopy",
                new File(outputJarName).getPath(),
                projectDir.getPath(),
                "/s",
                "/y"
            };
        } else {
            command = new String[] {
                "cp",
                new File(outputJarName).getPath(),
                projectDir.getPath()
            };
        }

        Cmd.runCommand(command);
    }

    public static void runClass(String className) throws IOException {
        Cmd.runCommand(new String[] {
            "java",
            "-cp",
            cacheDir.getPath(),
            packageName + "." + className
        });
    }

    public static void setPackage(String name) {
        packageName = name;
    }

    private static String packageName = "";

    private static File projectDir = new File(".");
    private static File cacheDir = new File("nog-cache");
    private static File outDir = new File("nog-out");

    private static ArrayList<BuildArtifact> units =
        new ArrayList<BuildArtifact>(); 


    private static void addArtifact(String filePath, boolean compileOnly) throws IllegalArgumentException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException(
                "File does not exist: " + filePath);
        }

        if (!file.isFile()) {
            throw new IllegalArgumentException(
                "Not a file: " + filePath);
        }

        if (!file.canRead()) {
            throw new IllegalArgumentException(
                "Cannot read file: " + filePath);
        }

        if (!file.getName().endsWith(".java")) {
            throw new IllegalArgumentException(
                "Not a Java file: " + filePath);
        }

        BuildArtifact artifact = new BuildArtifact(file, compileOnly);
        units.add(artifact);
    }

    private static void copyToCache(File file) throws IOException {
        if (Cmd.isWindows()) {
            Cmd.runCommand(new String[] {
                "xcopy",
                file.getPath(),
                cacheDir.getPath(),
                "/s",
                "/y"
            });
        } else {
            Cmd.runCommand(new String[] {
                "cp",
                file.getPath(),
                cacheDir.getPath()
            });
        }
    }

    private static void clearCache() throws IOException {
        if (Cmd.isWindows()) {
            Cmd.runCommand(new String[] {
                "rmdir",
                "/s",
                "/q",
                cacheDir.getPath()
            });
        } else {
            Cmd.runCommand(new String[] {
                "rm",
                "-rf",
                cacheDir.getPath()
            });
        }
    }
}
