package NogBuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

class BuildArtifact {
    public File file;
    public File cachedFile;

    public BuildArtifact(File file) {
        this.file = file;
        this.cachedFile = new File(Nog.cachedPath(file.getName()));
    }

    public long lastModified() {
        return file.lastModified();
    }

    public boolean newerThanCached() {
        if (cachedFile.exists()) {
            return file.lastModified() > cachedFile.lastModified();
        }
        return true;
    }

    public void copyToCache() throws IOException {
        File cache = new File(Nog.cacheDir);
        if (!cache.exists()) cache.mkdir();
        Nog.copyFile(file.getPath(), cachedFile.getPath());
    }
}

class Module {
    String name;
    private int artifactsBuilt = 0;
    ArrayList<BuildArtifact> units = new ArrayList<BuildArtifact>();
    public Module(String name) {
        this.name = name;
    }

    public ArrayList<String> getModifiedUnits() {
        ArrayList<String> modified = new ArrayList<String>();
        for (BuildArtifact unit: units) {
            if (unit.newerThanCached()) modified.add(unit.file.getPath());
        }
        return modified;
    }

    public BuildArtifact addFile(String... pathSegments) throws IOException {
        File file = new File(String.join(File.separator, pathSegments));
        if (!file.exists()) {
            throw new IOException("File not found: " + file.getPath());
        }

        BuildArtifact artifact = new BuildArtifact(file);
        units.add(artifact);
        return artifact;
    }

    public void build() throws IOException {
        ArrayList<String> command = new ArrayList<String>();
        command.add("javac");
        command.add("-d");
        command.add(Nog.cacheDir);
        command.add("-cp");
        command.add(Nog.cacheDir);

        for (BuildArtifact unit: units) {
            if (!unit.newerThanCached()) {
                continue;
            }
            unit.copyToCache();
            command.add(unit.file.getPath());
            artifactsBuilt++;
        }

        if (artifactsBuilt == 0) {
            return;
        }

        Nog.runCommand(command.toArray(new String[0]));
    }

    public void makeJar(String jarName, String entry) throws IOException {
        if (artifactsBuilt == 0) {
            return;
        }

        String out = Nog.cacheDir + File.separator + jarName;

        String[] command = new String[] {
            "jar", "cvfe", out,
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

    public static String joinPath(String... parts) {
        return String.join(File.separator, parts);
    }

    public static void runJar(String jarName, String[] args) throws IOException {
        Nog.runCommand(new String[] {
            "java",
            "-jar", jarName,
            String.join(" ", args)
        });
    }

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

    public static String cachedPath(String... path) {
        return cacheDir + File.separator + String.join(File.separator, path);
    }

    public static String projectPath(String... path) {
        return projectDir + File.separator + String.join(File.separator, path);
    }

    public static void copyFile(String source, String destination) throws IOException {
        File sourceFile = new File(source);
        File destinationFile = new File(destination);

        boolean bothExist = sourceFile.exists() && destinationFile.exists();
        boolean destIsNewer = sourceFile.lastModified() <= destinationFile.lastModified();

        String destinationDir = destination;
        if (!destinationFile.isDirectory()) {
            destinationDir = destinationFile.getParent();
        }

        if (bothExist && destIsNewer) {
            return;
        }

        copyFileUnchecked(source, destinationDir);
    }

    private static void copyFileUnchecked(String source, String destination) throws IOException {
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

    public static String ANSI_RESET = "\u001B[0m";
    public static String ANSI_GREEN = "\u001B[32m";
    public static String ANSI_GRAY = "\u001B[37m";
    public static String ANSI_RED = "\u001B[31m";

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
