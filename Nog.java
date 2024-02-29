package NogBuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

class BuildArtifact {
    public File file;
    public BuildArtifact(File file) {
        this.file = file;
    }
}

class Module {
    String name;
    ArrayList<BuildArtifact> units = new ArrayList<BuildArtifact>();
    public Module(String name) {
        this.name = name;
    }

    public void addFile(String filePath) throws IOException {
        File file = new File(filePath);
        Nog.copyFile(filePath, Nog.cacheDir);
        addArtifact(filePath);
    }

    public void addArtifact(String filePath) {
        File file = new File(filePath);
        BuildArtifact artifact = new BuildArtifact(file);
        units.add(artifact);
    }

    public void build() throws IOException {
        ArrayList<String> command = new ArrayList<String>();
        command.add("javac");
        command.add("-d");
        command.add(Nog.cacheDir);

        for (BuildArtifact unit : units) {
            command.add(unit.file.getPath());
        }

        Nog.runCommand(command.toArray(new String[0]));
    }

    public void makeJar(String jarName, String entry) throws IOException {
        String[] command = new String[] {
            "jar", "cvfe", Nog.cacheDir + File.separator + jarName,
            name + "." + entry,
            "-C", Nog.cacheDir, name
        };

        Nog.runCommand(command);
    }

    public void run(String className) throws IOException {
        Nog.runCommand(new String[] {
            "java",
            "-cp", Nog.cacheDir,
            name + "." + className
        });
    }
}

public class Nog {

    public static void buildAll() throws IOException {
        File cache = new File(cacheDir);
        if (!cache.exists()) cache.mkdir();

        ArrayList<String> command = new ArrayList<String>();
        command.add("javac");
        command.add("-d");
        command.add(cacheDir);

        for (Module module : modules) {
            module.build();
        }

        runCommand(command.toArray(new String[0]));
    }

    public static void setPackage(String name) {
        packageName = name;
    }

    public static Module addModule(String name) {
        Module module = new Module(name);
        modules.add(module);
        return module;
    }

    public static String cachedPath(String path) {
        return cacheDir + File.separator + path;
    }

    public static void copyFile(String source, String destination) throws IOException {
        String[] command = isWindows()
            ? new String[] { "xcopy.exe", source, destination, "/y" }
            : new String[] { "cp", source, destination };
        runCommand(command);
    }

    public static boolean isWindows() {
        return System.getProperty("os.name")
            .toLowerCase()
            .startsWith("windows");
    }

    private static String packageName = "";

    public static String projectDir = new File(".").getPath();
    public static String cacheDir = new File("nog-cache").getPath();
    public static String outDir = new File("nog-out").getPath();

    private static ArrayList<Module> modules =
        new ArrayList<Module>(); 

    private static String ANSI_RESET = "\u001B[0m";
    private static String ANSI_GREEN = "\u001B[32m";
    private static String ANSI_GRAY = "\u001B[37m";
    private static String ANSI_RED = "\u001B[31m";

    private static Runtime runtime = Runtime.getRuntime();

    public static void runCommand(String[] args) throws IOException {
        System.out.println(ANSI_GREEN + "+ " + String.join(" ", args) + ANSI_RESET);

        String[] prefix = isWindows()
            ? new String[] {"cmd.exe", "/c"}
            : new String[] {"/bin/sh", "-c"};

        String[] command = new String[prefix.length + args.length];
        System.arraycopy(prefix, 0, command, 0, prefix.length);
        System.arraycopy(args, 0, command, prefix.length, args.length);

        Process process = runtime.exec(command);
        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            System.out.println("Process interrupted");
        }

        if (exitCode == 0) {
            String stdout = getOutput(process.getInputStream());
            if (stdout.length() > 0) {
                System.out.print(ANSI_GRAY);
                System.out.print(stdout);
                System.out.print(ANSI_RESET);
            }
        } else {
            String stderr = getOutput(process.getErrorStream());
            if (stderr.length() > 0) {
                System.err.print(ANSI_RED);
                System.err.print(stderr);
                System.err.print(ANSI_RESET);
            }
        }
    }

    private static String getOutput(InputStream stream) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }
}
