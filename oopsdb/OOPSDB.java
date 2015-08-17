package oopsdb;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import oopsvm.Assembler;
import oopsvm.RefreshListener;
import oopsvm.VirtualMachine;

/**
 * Die Klasse implementiert das Hauptfenster des OOPS-Debuggers.
 * Eine Instanz muss erzeugt werden, um das Programm zu starten.
 */
class OOPSDB implements ActionListener, RefreshListener {
    private static final String runLabel = "> Programm >";
    private static final String executeMethodLabel = "> Methode >";
    private static final String executeCallLabel = "> Aufruf >";
    private static final String executeLineLabel = "> Zeile >";
    private static final String executeInstructionLabel = "> Instruktion >";
    private final JFrame frame;
    private int[] memory;
    private int[] registers;
    private VirtualMachine vm;
    private SourceModel sourceModel;
    
    /**
     * Der Konstruktor erzeugt das Hauptfenster mit Menüleiste.
     * Im Hauptfenster befindet sich im Hauptbereich die Tabelle
     * in einem Bereich mit Rollbalken. Oben ist das Suchfeld mit 
     * rechts davon liegendem Button "Suchen" platziert.
     * Zudem werden das Datenmodell für die Studierenden erzeugt
     * und die Sortierroutine gesetzt.
     */
    OOPSDB(String assemblerFileName, String sourceFileName) {
        frame = new JFrame("OOPSDB");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        try {
            Assembler assembler = new Assembler(false, false);
            memory = assembler.assemble(assemblerFileName);
            registers = new int[8];
            vm = new VirtualMachine(memory, registers, false, false, false);
            MemoryModel memoryModel = new MemoryModel(memory, registers);
            MemoryPane memoryPane = new MemoryPane(memoryModel, memory, registers, 
                    assembler.getLabelAddress("_stack"),
                    assembler.getLabelAddress("_heap"));
            frame.add(memoryPane, sourceFileName == null ? BorderLayout.CENTER : BorderLayout.EAST);
            vm.addMemoryListener(memoryModel);
            vm.addRegistersListener(memoryModel);
            vm.addRefreshListener(memoryModel);
            
            RegistersModel registersModel = new RegistersModel(registers, memoryPane);
            frame.add(new RegistersPane(registersModel), BorderLayout.SOUTH);
            vm.addRegistersListener(registersModel);
            vm.addRefreshListener(registersModel);
            
            AssemblerModel assemblerModel = new AssemblerModel(memory, 
                    assembler.getInstructionAddresses(), registers);
            AssemblerPane assemblerPane = new AssemblerPane(assemblerModel, assembler.getInstructionAddresses(), vm);
            frame.add(assemblerPane, BorderLayout.WEST);
            vm.addMemoryListener(assemblerModel);
            vm.addRegistersListener(assemblerModel);
            vm.addRefreshListener(assemblerModel);
            
            if (sourceFileName != null) {
                sourceModel = new SourceModel(sourceFileName, 
                        assembler.getLineAddresses(), registers, memory.length, assemblerPane);
                SourcePane sourcePane = new SourcePane(sourceModel, assembler.getLineAddresses(), vm);
                frame.add(sourcePane, BorderLayout.CENTER);
                assemblerModel.setSourcePane(sourcePane);
                vm.addMemoryListener(sourceModel);
                vm.addRegistersListener(sourceModel);
                vm.addRefreshListener(sourceModel);
            }

            vm.addRefreshListener(this);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(), "Fehler", JOptionPane.INFORMATION_MESSAGE);
            System.exit(1);
        }

        frame.add(createToolBar(), BorderLayout.NORTH);
        frame.pack();
        frame.setVisible(true);
    }
    
    /**
     * Erzeugen der Symbolleiste.
     * @return Die Symbolleiste.
     */
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton button = new JButton(runLabel);
        button.addActionListener(this);
        toolBar.add(button);
        if (sourceModel != null) {
            button = new JButton(executeMethodLabel);
            button.addActionListener(this);
            toolBar.add(button);
            button = new JButton(executeCallLabel);
            button.addActionListener(this);
            toolBar.add(button);
            button = new JButton(executeLineLabel);
            button.addActionListener(this);
            toolBar.add(button);
        }
        button = new JButton(executeInstructionLabel);
        button.addActionListener(this);
        toolBar.add(button);
        return toolBar;
    }

    /**
     * Reaktionen auf Auswahl von Menüpunkten, Drücken des Suchen-Buttons
     * und Drücken der Eingabetaste im Textfeld.
     * @param event Das ausgelöste Ereignis.
     */
    public void actionPerformed(ActionEvent event) {
        try {
            int address;
            int line;
            switch(event.getActionCommand()) {
            case runLabel:
                vm.run(-1, false, false, false);
                break;
            case executeMethodLabel:
                vm.run(-1, false, true, false);
                break;
            case executeCallLabel:
                address = registers[0];
                line = sourceModel.getLineFromAddress(address);
                if (line >= 0) {
                    while (sourceModel.getLineFromAddress(++address) == line);
                }
                vm.run(address, false, false, true);
                break;
            case executeLineLabel:
                address = registers[0];
                line = sourceModel.getLineFromAddress(address);
                if (line >= 0) {
                    while (sourceModel.getLineFromAddress(++address) == line);
                }
                vm.run(address, true, false, false);
                break;
            case executeInstructionLabel:
                vm.step();
                break;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(), "Fehler", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Erzwingt ein Neuzeichenen.
     * Wird unmittelbar vor dem READ-Befehl benötigt, der das Programm blockiert.
     */
    public void refresh() {
        if (registers[0] >= 0 && registers[0] < memory.length && (memory[registers[0]] & 0xfff0) == 0x1000) {
            frame.paint(frame.getGraphics());
        }
    }
    
    /**
     * Die Hauptmethode der virtuellen Maschine. 
     * Sie wertet die Kommandozeilen-Optionen aus und bietet eine Hilfe an, falls diese falsch sind.
     * Sind sie gültig, wird der Assembler benutzt, um den übergebenen Quelltext in ein
     * Maschinenprogramm zu übersetzen. Dieses wird dann von der virtuellen Maschine ausgeführt.
     * @param args Die Kommandozeilenargumente. Diese sind im Quelltext der Methode 
     * {@link #usage usage} nachzulesen.
     */
    public static void main(String[] args) {
        String assemblerFileName = null;
        String sourceFileName = null;

        for (String arg : args) {
            if (arg.equals("-h")) {
                usage();
            } else if (arg.length() > 0 && arg.charAt(0) == '-') {
                System.out.println("Unbekannte Option " + arg);
                usage();
            } else if (sourceFileName != null) {
                System.out.println("Nur zwei Dateinamen erlaubt");
                usage();
            } else {
                sourceFileName = assemblerFileName;
                assemblerFileName = arg;
            }
        }
            
        if (assemblerFileName == null) {
            System.out.println("Kein Dateiname angegeben");
            usage();
        }
        
        new OOPSDB(assemblerFileName, sourceFileName);
    }
    
    /**
     * Die Methode gibt eine Hilfe auf der Konsole aus und beendet das Programm.
     */
    private static void usage() {
        System.out.println("java -jar OOPSDB.jar [-h] [<oops-dateiname>] <asm-dateiname>");
        System.out.println("    -h  Zeige diese Hilfe");
        System.exit(2);
    }
}
