package NogBuild;

import java.util.ArrayList;
import java.util.Arrays;

class Build {
    public static void main(String[] args) throws Exception {
        bootstrapBuild(args);

        Nog.setPackage("Main");

        Module main = Nog.addModule("Main");

        main.addFile("src\\Hello.java");
        main.addFile("src\\Program.java");

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
        if (modifiedFiles.size() == 0) {
            System.out.println("No build files changed.");
            return; 
        }

        System.out.println("Build files changed:");
        for (String f: modifiedFiles) {
            System.out.println("- " + f);
        }

        // This is a failsafe to prevent the build system from becoming
        // a fork bomb when modified files arent detected properly
        if (args.length > 0 && Arrays.asList(args).contains("bootstrapped")) {
            System.out.println(
                Nog.ANSI_RED
                + "Recursion limit reached. Aborting bootstrap..."
                + Nog.ANSI_RESET);

            return;
        }


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
