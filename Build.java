package NogBuild;

class Build {
    public static void main(String[] args) throws Exception {
        Nog.setPackage("Main");

        Nog.addFile("src/Hello.java");
        Nog.addFile("src/Program.java");

        Nog.build();

        if (args.length > 0 && args[0].equals("run")) {
            Nog.runClass("Program");
        }

        System.out.println("Boostraping Nog...");
        Nog.bootstrap(
            "Nog.java",
            "Build.java",
            "build.jar",
            "NogBuild.Build"
        );
    }
}
