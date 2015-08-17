package oopsvm;

public interface RegistersListener {
    /**
     * Der Inhalt eines Registers hat sich verändert.
     * @param register Die Nummer des Registers.
     */
    void registerChanged(int index);
}
