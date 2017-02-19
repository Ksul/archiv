package de.schulte.archiv;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.InputStream;

public class PDFConnector {
	
	// Extract text from PDF Document
	public String pdftoText(InputStream f) {
		String parsedText;
		PDDocument pdf = null;
		try {
			pdf = PDDocument.load((f));
			PDFTextStripper stripper = new PDFTextStripper();
			parsedText = stripper.getText(pdf);
		} catch (Throwable e) {
			System.out.println("Unable to open PDF Parser.");
			System.out.println(e.getMessage());
			e.printStackTrace();
			return "Unable to parse PDF Document. " + e.getMessage();
		} finally {
			if (pdf != null) {
				try {
					pdf.close();
				} catch (Throwable e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
				if (f != null) {
					try {
						f.close();
					} catch (Throwable e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
		return parsedText;
	}

}
