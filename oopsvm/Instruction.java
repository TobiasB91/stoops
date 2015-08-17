package oopsvm;

/**
 * Dieser Aufzählungstyp definiert alle Instruktionen der virtuellen Maschine.
 */
public enum Instruction {
    /** 
     * MRI reg, num. 
     * Diese Instruktion speichert die Zahl num im Register reg.
     */
    MRI,

    /**
     * MRR reg1, reg2.
     * Diese Instruktion speichert den Inhalt von Register <i>reg1</i> im Register <i>reg2</i>. 
     */
    MRR,

    /** 
     * MRM reg1, (reg2). 
     * Diese Instruktion speichert den Inhalt der Speicherstelle, auf die Register <i>reg2</i>
     * zeigt, im Register <i>reg1</i>.
     */
    MRM,

    /** 
     * MMR (reg1), reg2. 
     * Diese Instruktion speichert den Inhalt von Register <i>reg2</i> in der Speicherstelle, 
     * auf die Register <i>reg1</i> zeigt.
     */
    MMR,
    
    /**
     * ADD reg1, reg2.
     * Diese Instruktion addiert den Inhalt von Register <i>reg2</i> zum Register <i>reg1</i>. 
     */
    ADD,
    
    /**
     * SUB reg1, reg2.
     * Diese Instruktion subtrahiert den Inhalt von Register <i>reg2</i> vom Register <i>reg1</i>. 
     */
    SUB,
    
    /**
     * MUL reg1, reg2.
     * Diese Instruktion multipliziert den Inhalt von Register <i>reg2</i> zum Register <i>reg1</i>. 
     */
    MUL,
    
    /**
     * DIV reg1, reg2.
     * Diese Instruktion dividiert das Register <i>reg1</i> durch den Inhalt von Register <i>reg2</i>. 
     */
    DIV,

    /**
     * MOD reg1, reg2.
     * Diese Instruktion speichert den Divisionsrest von Register <i>reg1</i> durch Register <i>reg2</i>
     * in Register <i>reg1</i>. 
     */
    MOD,

    /**
     * AND reg1, reg2.
     * Diese Instruktion und-verknüpft den Inhalt von Register <i>reg2</i> in das Register <i>reg1</i>. 
     */
    AND,

    /**
     * OR reg1, reg2.
     * Diese Instruktion oder-verknüpft den Inhalt von Register <i>reg2</i> in das Register <i>reg1</i>. 
     */
    OR,

    /**
     * XOR reg1, reg2.
     * Diese Instruktion exklusiv-oder-verknüpft den Inhalt von Register <i>reg2</i> in das Register <i>reg1</i>. 
     */
    XOR,

    /**
     * ISZ reg1, reg2.
     * Diese Instruktion setzt das Register <i>reg1</i> auf eins, wenn der Inhalt des Registers <i>reg2</i>
     * null ist, ansonsten auf null.
     */
    ISZ,

    /**
     * ISP reg1, reg2.
     * Diese Instruktion setzt das Register <i>reg1</i> auf eins, wenn der Inhalt des Registers <i>reg2</i>
     * größer als null ist, ansonsten auf null.
     */
    ISP,

    /**
     * ISP reg1, reg2.
     * Diese Instruktion setzt das Register <i>reg1</i> auf eins, wenn der Inhalt des Registers <i>reg2</i>
     * kleiner als null ist, ansonsten auf null.
     */
    ISN,

    /**
     * JPC reg1, addr.
     * Diese Instruktion schreibt <i>addr</i> in den Instruktionszeiger (Register R0), wenn der Inhalt des 
     * Registers <i>reg1</i> ungleich null ist.
     */
    JPC,

    /**
     * SYS num1, num2.
     * Diese Instruktion ruft eine Systemfunktion auf. <i>num1</i> ist dabei die Nummer der Funktion,
     * <i>num2</i> ein funktionsabhängiger Parameter. Momentan sind nur zwei Funktionen definiert:
     * <ul>
     *   <li> 0: Es wird ein Zeichen von der Konsole eingelesen. Das Zeichen wird in dem Register 
     *        mit der Nummer <i>num2</i> abgelegt. Das Ende des Eingabestroms wird durch das
     *        Zeichen -1 symbolisiert.</li>
     *   <li> 1: Es wird ein Zeichen auf der Konsole ausgegeben. Das Zeichen wird aus dem Register 
     *        mit der Nummer <i>num2</i> gelesen.</li>
     * </ul>
     */
    SYS;
    
    /**
     * Liefert eine textuelle Beschreibung der Instruktion mit ihren Parametern.
     * @param param1 Der erste Parameter. Ist meistens die Nummer eines Registers.
     * @param param2 Der zweite Parameter. Ist meistens die Nummer eines Registers, manchmal aber
     *         auch ein Wert.
     * @return Die Instruktion als Text. 
     */
    public String toString(int param1, int param2) {
        switch (this) {
        case MRI:
        case JPC:
            return toString() + " R" + param1 + ", " + String.format("%04x", param2 & 0xffff);
        case MRM:
            return toString() + " R" + param1 + ", (R" + param2 + ")";
        case MMR:
            return toString() + " (R" + param1 + "), R" + param2;
        case SYS:
            return toString() + " " + param1 + ", " + param2;
        default:
            return toString() + " R" + param1 + ", R" + param2;
        }
    }
}
