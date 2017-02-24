package de.ksul.archiv;

public class ArchivException extends Exception {

    public ArchivException(String fehler) {
        super(fehler);
    }

    public ArchivException(String fehler, Exception e) {
        super(fehler, e);
    }
}
