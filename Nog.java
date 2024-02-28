package NogBuild;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

class Cmd {
    public static String runCommand(String[] args) throws IOException {
        Process proc = rt.exec(getFullCommand(args));
        InputStreamReader reader = new InputStreamReader(proc.getInputStream());
        char[] buffer = new char[4096];
        StringBuilder output = new StringBuilder();
        int n;
        while ((n = reader.read(buffer)) != -1) {
            output.append(buffer, 0, n);
        }
        return output.toString();
    }

    public static boolean isWindows() {
        return System.getProperty("os.name")
            .toLowerCase()
            .startsWith("windows");
    }

    private static Runtime rt = Runtime.getRuntime();

    private static String[] getSystemPrefix() {
        if (isWindows()) {
            return new String[] {"cmd", "/c"};
        } else {
            return new String[] {"/bin/sh", "-c"};
        }
    }

    private static String[] getFullCommand(String[] args) {
        String[] cmd = getSystemPrefix();
        String[] command = new String[cmd.length + args.length];
        System.arraycopy(cmd, 0, command, 0, cmd.length);
        System.arraycopy(args, 0, command, cmd.length, args.length);
        return command;
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
        command.add(cacheDir.getAbsolutePath());

        for (BuildArtifact unit: units) {
            copyToCache(unit.file);
            command.add(unit.file.getAbsolutePath());
        }

        System.out.println("+ " + String.join(" ", command));

        String output = Cmd.runCommand(command.toArray(new String[0]));
        System.out.print(output);
    }

    public static void runClass(String className) throws IOException {
        String[] command = new String[] {
            "java",
            "-cp",
            cacheDir.getAbsolutePath(),
            packageName + "." + className
        };
        System.out.println("+ " + String.join(" ", command));
        String output = Cmd.runCommand(command);
        System.out.print(output);
    }

    public static void setPackage(String name) {
        packageName = name;
    }

    private static String packageName = "";

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
            String[] command = new String[] {
                "xcopy",
                file.getAbsolutePath(),
                cacheDir.getAbsolutePath(),
                "/s",
                "/y"
            };
            System.out.println("+ " + String.join(" ", command));
            Cmd.runCommand(command);
        } else {
            String[] command = new String[] {
                "cp",
                file.getAbsolutePath(),
                cacheDir.getAbsolutePath()
            };
            System.out.println("+ " + String.join(" ", command));
            Cmd.runCommand(command);
        }
    }

    private static void clearCache() throws IOException {
        if (Cmd.isWindows()) {
            String[] command = new String[] {
                "rmdir",
                "/s",
                "/q",
                cacheDir.getAbsolutePath()
            };
            System.out.println("+ " + String.join(" ", command));
            Cmd.runCommand(command);
        } else {
            String[] command = new String[] {
                "rm",
                "-rf",
                cacheDir.getAbsolutePath()
            };
            System.out.println("+ " + String.join(" ", command));
            Cmd.runCommand(command);
        }
    }
}
