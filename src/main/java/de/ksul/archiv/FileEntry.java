package de.ksul.archiv;

/**
 * Die Klasse realisiert einen internen Speicher für eingelesene Dokumente
 */
public class FileEntry {

    /**
     * Konstruktor
     * @param name              der Name der Datei
     * @param data              die originalen Daten der Datei
     */
	public FileEntry(String name, byte[] data) {
		super();
		this.name = name;
		this.data = data;
	}

    /**
     * Konstruktor
     * @param name              der Name der Datei
     * @param data              die originalen Daten der Datei
     * @param extractedData     die extrahierten Daten der Datei
     */
    public FileEntry(String name, byte[] data, String extractedData) {
        super();
        this.name = name;
        this.data = data;
        this.extractedData = extractedData;
    }

    /**
     * liefert den Namen der Datei
     * @return   der Name der Datei
     */
	public String getName() {
		return name;
	}

    /**
     * liefert die originalen Daten der Datei
     * @return   die originalen Daten der Datei
     */
	public byte[] getData() {
		return data;
	}

    /**
     * liefert die extrahierten Daten der Datei.
     * dies ist bei PDF Dateien der Text als String
     * @return  die extrahierten Daten
     */
    public String getExtractedData(){
        return extractedData;
    }

    /**
     * setzt die extrahierten Daten der Datei.
     * dies ist bei PDF Dateien der Text als String
     * @param extractedData  die extrahierten Daten
     */
    public void setExtractedData(String extractedData) {
        this.extractedData = extractedData;
    }

    // der Name der Datei
	String name;

    // die orginal Daten der Datei
	byte[] data;


    // die extrahierten Daten als String
    String extractedData;

}
