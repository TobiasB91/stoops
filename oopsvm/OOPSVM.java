package oopsvm;

/**
 * Diese Klasse stellt die Hauptmethode der virtuellen Maschine 
 * für OOPS bereit. Sie wertet die Kommandozeilen-Optionen aus
 * und bietet eine Hilfe an, falls diese falsch sind.
 */
class OOPSVM {
    /**
     * Die Hauptmethode der virtuellen Maschine. 
     * Sie wertet die Kommandozeilen-Optionen aus und bietet eine Hilfe an, falls diese falsch sind.
     * Sind sie gültig, wird der Assembler benutzt, um den übergebenen Quelltext in ein
     * Maschinenprogramm zu übersetzen. Dieses wird dann von der virtuellen Maschine ausgeführt.
     * @param args Die Kommandozeilenargumente. Diese sind im Quelltext der Methode 
     * {@link #usage usage} nachzulesen.
     */
    public static void main(String[] args) {
        String fileName = null;
        boolean showInstructions = false;
        boolean showMemory = false;
        boolean showRegisters = false;
        boolean showFirst = false;
        boolean showSecond = false;
        boolean execution = true;

        for (String arg : args) {
            if (arg.equals("-i")) {
                showInstructions = true;
            } else if (arg.equals("-m")) {
                showMemory = true;
            } else if (arg.equals("-r")) {
                showRegisters = true;
            } else if (arg.equals("-1")) {
                showFirst = true;
            } else if (arg.equals("-2")) {
                showSecond = true;
            } else if (arg.equals("-c")) {
                execution = false;
            } else if (arg.equals("-h")) {
                usage();
            } else if (arg.length() > 0 && arg.charAt(0) == '-') {
                System.out.println("Unbekannte Option " + arg);
                usage();
            } else if (fileName != null) {
                System.out.println("Nur ein Dateiname erlaubt: " + fileName + " vs. " + arg);
                usage();
            } else {
                fileName = arg;
            }
        }
            
        if (fileName == null) {
            System.out.println("Kein Dateiname angegeben");
            usage();
        }
        
        try {
            VirtualMachine vm = new VirtualMachine(
                    new Assembler(showFirst, showSecond).assemble(fileName), new int[8],
                    showInstructions, showMemory, showRegisters);
            if (execution) {
                vm.run(-1, false, false, false);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Die Methode gibt eine Hilfe auf der Konsole aus und beendet das Programm.
     */
    private static void usage() {
        System.out.println("java -jar OOPSVM.jar [-1] [-2] [-c] [-h] [-i] [-m] [-r] <dateiname>");
        System.out.println("    -1  Ausgabe beim ersten Assemblierungslauf");
        System.out.println("    -2  Ausgabe beim zweiten Assemblierungslauf");
        System.out.println("    -c  Programm wird nur uebersetzt, aber nicht ausgefuehrt");
        System.out.println("    -h  Zeige diese Hilfe");
        System.out.println("    -i  Zeige Instruktionen bei der Ausfuehrung");
        System.out.println("    -m  Zeige Speicher bei der Ausfuehrung");
        System.out.println("    -r  Zeige Registersatz bei der Ausfuehrung");
        System.exit(2);
    }
}
