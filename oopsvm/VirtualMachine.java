package oopsvm;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Die Klasse implementiert eine virtuelle Maschine für einen einfachen
 * Satz von Maschinenbefehlen. Alle Befehle haben zwei Parameter, so
 * dass sie immer aus drei Maschinenworten bestehen. Die virtuelle Maschine
 * hat einen Registersatz (R0 ... Rn), wobei das Register R0 der Instruktionszeiger
 * ist, d.h. R0 zeigt immer auf die nächste auszuführenden Instruktion.
 * Verlässt R0 den gültigen Bereich des Hauptspeichers, ist das Programm beendet.
 */
public class VirtualMachine {
    /** Die Menge aller Instruktionen zur schnellen Abbildung von Index auf Instruktion. */
    public static final Instruction[] INSTRUCTIONS = Instruction.values();
    
    /** Der Hauptspeicher. Er enthält das Programm und alle Daten. */
    private int[] memory;
    
    /** Der Registersatz. */
    private int[] registers;
    
    /** Sollen die ausgeführten Instruktionen angezeigt werden? */
    private boolean showInstructions;
    
    /** Soll der Speicherinhalt nach jeder ausgeführten Instruktion angezeigt werden? */
    private boolean showMemory;
    
    /** Soll der Registersatz nach jeder ausgeführten Instruktion angezeigt werden? */
    private boolean showRegisters;
    
    /** Sind wir im Einzelschrittmodus? */
    private boolean singleStep;
    
    /** Alle Objekte, die über Verändernungen im Speicher informiert werden wollen. */
    private ArrayList<MemoryListener> memoryListeners = new ArrayList<MemoryListener>();
    
    /** Alle Objekte, die über Verändernungen im Registersatz informiert werden wollen. */
    private ArrayList<RegistersListener> registersListeners = new ArrayList<RegistersListener>();
    
    /** Alle Objekte, die über Verändernungen informiert werden wollen, die vollständiges Neuzeichnen erfordern. */
    private ArrayList<RefreshListener> refreshListeners = new ArrayList<RefreshListener>();
    
    private HashSet<Integer> breakPoints = new HashSet<Integer>();

    private void executeInstruction() throws Exception {
        try {
            int address = registers[0];
            int word = memory[registers[0]++];
            Instruction instruction;
            try {
                instruction = INSTRUCTIONS[word >> 8 & 0xff];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new Exception("Illegale Instruktion: " + String.format("%04x", word >> 8 & 0xff) + 
                        " an Adresse " + String.format("%04x", address));
            }
            int param1 = word >> 4 & 0x0f;
            int param2 = word & 0x0f;
            boolean registerChanged = true;
            switch(instruction) {
            case MRI:
                param2 = memory[registers[0]++];
                registers[param1] = param2;
                break;
            case MRR:
                registers[param1] = registers[param2];
                break;
            case MRM:
                if (registers[param2] < 0 || registers[param2] >= memory.length) {
                    throw new Exception("Zugriff auf nicht existierende Speicherstelle " 
                            + registers[param2] + " an Adresse " + String.format("%04x", address));
                }
                registers[param1] = memory[registers[param2]];
                break;
            case MMR:
                if (registers[param1] < 0 || registers[param1] >= memory.length) {
                    throw new Exception("Zugriff auf nicht existierende Speicherstelle " 
                            + registers[param1] + " an Adresse " + String.format("%04x", address));
                }
                memory[registers[param1]] = registers[param2];
                if (singleStep) {
                    for (MemoryListener listener : memoryListeners) {
                        listener.memoryChanged(registers[param1]);
                    }
                }
                registerChanged = false;
                break;
            case ADD:
                registers[param1] += registers[param2];
                break;
            case SUB:
                registers[param1] -= registers[param2];
                break;
            case MUL:
                registers[param1] *= registers[param2];
                break;
            case DIV:
                registers[param1] /= registers[param2];
                break;
            case MOD:
                registers[param1] %= registers[param2];
                break;
            case AND:
                registers[param1] &= registers[param2];
                break;
            case OR:
                registers[param1] |= registers[param2];
                break;
            case XOR:
                registers[param1] ^= registers[param2];
                break;
            case ISZ:
                registers[param1] = registers[param2] == 0 ? 1 : 0;
                break;
            case ISP:
                registers[param1] = registers[param2] > 0 ? 1 : 0;
                break;
            case ISN:
                registers[param1] = registers[param2] < 0 ? 1 : 0;
                break;
            case JPC:
                param2 = memory[registers[0]++];
                if (registers[param1] != 0) {
                    registers[0] = param2;
                }
                registerChanged = false;
                break;
            case SYS:
                switch (param1) {
                case 0:
                    --registers[0]; // Nur für Visualisierung während des Wartens auf Eingabe
                    if (!singleStep && System.in.available() == 0) {
                        for (RefreshListener listener : refreshListeners) {
                            listener.refresh();
                        }
                    }
                    registers[param2] = System.in.read();
                    ++registers[0]; // Wieder zurücksetzen
                    break;
                case 1:
                    System.out.print((char) registers[param2]);
                    registerChanged = false;
                    break;
                default:
                    throw new Exception("Illegaler Systemaufruf: " + param1);
                }
            }

            if (showInstructions) {
                System.out.format("%04x  %s%n", address, instruction.toString(param1, param2));
            }
            
            if (singleStep) {
                for (RegistersListener listener : registersListeners) {
                    listener.registerChanged(0);
                    if (registerChanged) {
                        if (instruction == Instruction.SYS) {
                            if (param2 != 0) {
                                listener.registerChanged(param2);
                            }
                        } else if (param1 != 0) {
                            listener.registerChanged(param1);
                        }
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new Exception("Zugriff auf nicht existierendes Register " + e.getMessage() + " an Adresse " + (registers[0] - 3));
        }
    }
    
    /**
     * Die Methode gibt den Hauptspeicher aus, wenn {@link #showMemory showMemory}
     * aktiviert ist.
     */
    private void printMemory() {
        if (showMemory) {
            String text = "";
            for (int m : memory)
                text += m + " ";
            System.out.println(text);
        }
    }
  
    /**
     * Die Methode gibt den Registersatz aus, wenn {@link #showRegisters showRegisters}
     * aktiviert ist.
     */
    private void printRegisters() {
        if (showRegisters) {
            String text = "";
            for (int i = 0; i < registers.length; ++i)
                text += "R" + i + "=" + registers[i] + " ";
            System.out.println(text);
        }
    }

    /**
     * Konstruiert eine virtuelle Maschine.
     * @param memory Der Hauptspeicher. Er enthält das Programm und alle Daten.
     * @param registers Der Registersatz.
     * @param showInstructions Sollen die ausgeführten Instruktionen angezeigt werden?
     * @param showMemory Soll der Speicherinhalt nach jeder ausgeführten Instruktion angezeigt werden?
     * @param showRegisters Soll der Registersatz nach jeder ausgeführten Instruktion angezeigt werden?
     */
    public VirtualMachine(int[] memory, int[] registers, 
            boolean showInstructions, boolean showMemory, boolean showRegisters) {
        this.memory = memory;
        this.registers = registers;
        this.showInstructions = showInstructions;
        this.showMemory = showMemory;
        this.showRegisters = showRegisters;
        singleStep = true;
    }
    
    /**
     * Die Methode führt das Programm im Hauptspeicher aus.
     * @param untilAddress Führt das Programm bis zum Erreichen dieser Adresse aus.
     *         Ist der Wert eine gültige Speicheradresse, wird aber auch nach jedem
     *         Sprung angehalten.
     * @param untilJump Hält nach jedem Sprung an.
     * @param untilReturn Hält die Ausführung an, wenn ein Sprung passiert und der 
     *         Stapelzeiger R2 kleiner ist als zuvor.
     * @param stepOver Methodenaufrufe auf einen Schlag ausführen.
     * @throws Exception Ein Fehler ist aufgetreten (Instruktion, Speicherstelle,
     *         Register oder Systemaufruf ungültig).
     */
    public void run(int untilAddress, boolean untilJump, boolean untilReturn, boolean stepOver) throws Exception {
        singleStep = false;
        
        if (registers[0] < 0 || registers[0] >= memory.length) {
            throw new Exception(String.format("Register R0 zeigt auf eine Adresse außerhalb des Speichers (%04x).", registers[0]));
        }

        int startR2 = registers[2];
        while (registers[0] >= 0 && registers[0] < memory.length) {
            int oldR0 = registers[0];
            executeInstruction();
            printMemory();
            printRegisters();
            if (registers[0] == untilAddress ||
                    hasBreakPoint(registers[0]) || 
                    ((untilJump || untilReturn && registers[2] < startR2 || stepOver && registers[2] <= startR2) &&
                            registers[0] != oldR0 + 1 && registers[0] != oldR0 + 2)) {
                break;
            }
        }
        
        singleStep = true;
        for (RefreshListener listener : refreshListeners) {
            listener.refresh();
        }
    }
    
    /**
     * Die Methode führt eine einzelne Instruktion sie aus.
     * @throws Exception Ein Fehler ist aufgetreten (Instruktion, Speicherstelle
     *         oder Systemaufruf ungültig).
     */
    public void step() throws Exception {
        if (registers[0] < 0 || registers[0] >= memory.length) {
            throw new Exception(String.format("Register R0 zeigt auf eine Adresse außerhalb des Speichers (%04x).", registers[0]));
        } else {
            executeInstruction();
        }
    }
    
    /**
     * Fügt ein Objekt hinzu, das über Veränderungen im Speicher informiert werden möchte.
     * @param listener Das Objekt, das informiert wird.
     */
    public void addMemoryListener(MemoryListener listener) {
        memoryListeners.add(listener);
    }
    
    /**
     * Fügt ein Objekt hinzu, das über Veränderungen im Registersatz informiert werden möchte.
     * @param listener Das Objekt, das informiert wird.
     */
    public void addRegistersListener(RegistersListener listener) {
        registersListeners.add(listener);
    }

    /**
     * Fügt ein Objekt hinzu, das über Veränderungen informiert werden möchte, die ein
     * vollständiges Neuzeichnen erfordern.
     * @param listener Das Objekt, das informiert wird.
     */
    public void addRefreshListener(RefreshListener listener) {
        refreshListeners.add(listener);
    }
    
    /**
     * Fügt einen Haltepunkt hinzu und informiert alle Beobachter des Speichers.
     * @param address Die Adresse, an der das Programm angehalten werden soll.
     */
    public void addBreakPoint(int address) {
        breakPoints.add(address);
        for (MemoryListener listener : memoryListeners) {
            listener.memoryChanged(address);
        }
    }
    
    /**
     * Entfernt einen Haltepunkt und informiert alle Beobachter des Speichers.
     * @param address Die Adresse, an der das Programm nun nicht mehr angehalten werden soll.
     */
    public void removeBreakPoint(int address) {
        breakPoints.remove(address);
        for (MemoryListener listener : memoryListeners) {
            listener.memoryChanged(address);
        }
   }
    
    /**
     * Liefert zurück, ob für eine bestimmte Adresse ein Haltepunkt gesetzt wurde.
     * @param address Die Adresse, nach der gefragt wird.
     * @return Wurde für die angegebene Adresse ein Haltepunkt gesetzt?
     */
    public boolean hasBreakPoint(int address) {
        return breakPoints.contains(address);
    }
 }
