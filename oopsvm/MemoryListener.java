package oopsvm;

public interface MemoryListener {
    /**
     * Der Inhalt einer Speicherzelle hat sich verändert.
     * @param address Die Adresse der Speicherzelle.
     */
    void memoryChanged(int address);
}
