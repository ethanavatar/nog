package NogBuild;

class Build {
    public static void main(String[] args) {
        System.out.println("[" + String.join(" ", args) + "]");
        try {
            Nog.setPackage("Main");

            Nog.addFile("src/Hello.java");
            Nog.addFile("src/Program.java");

            Nog.build();

            if (args[0].equals("run")) {
                Nog.runClass("Program");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
