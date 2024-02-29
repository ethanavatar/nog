package NogBuild;

import java.io.File;

class Build {
    public static void main(String[] args) throws Exception {
        Nog.setPackage("Main");

        Module main = Nog.addModule("Main");

        main.addFile("src\\Hello.java");
        main.addFile("src\\Program.java");

        main.build();

        System.out.println("Bootstrapping...");
        Module build = Nog.addModule("NogBuild");

        build.addFile("Nog.java");
        build.addFile("Build.java");

        build.build();
        build.makeJar("Build.jar", "Build");
        Nog.copyFile(Nog.cachedPath("Build.jar"), Nog.projectDir);
    }
}
