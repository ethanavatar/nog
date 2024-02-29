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
