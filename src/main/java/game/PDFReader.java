package game;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PDFReader {
    private File currentPDF;
    private Map<String, String> formFields;
    
    public PDFReader() {
        formFields = new HashMap<>();
    }

    public String readPDF(File file) throws IOException {
        currentPDF = file;
        try (PDDocument document = PDDocument.load(file)) {
            // Read form fields
            PDAcroForm form = document.getDocumentCatalog().getAcroForm();
            if (form != null) {
                for (PDField field : form.getFields()) {
                    formFields.put(field.getFullyQualifiedName(), field.getValueAsString());
                }
            }
            
            // Read text content
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public void fillAndSaveForm(Map<String, String> answers) throws IOException {
        if (currentPDF == null) return;
        
        try (PDDocument document = PDDocument.load(currentPDF)) {
            PDAcroForm form = document.getDocumentCatalog().getAcroForm();
            if (form != null) {
                form.setNeedAppearances(true); // Important for proper rendering
                
                // Fill each field
                for (Map.Entry<String, String> entry : answers.entrySet()) {
                    PDField field = form.getField(entry.getKey());
                    if (field != null) {
                        try {
                            field.setValue(entry.getValue());
                            System.out.println("Filled field: " + entry.getKey() + " with value: " + entry.getValue());
                        } catch (IOException e) {
                            System.err.println("Error filling field " + entry.getKey() + ": " + e.getMessage());
                        }
                    } else {
                        System.err.println("Field not found: " + entry.getKey());
                    }
                }
            }
            
            // Save with a timestamp to avoid overwrites
            String outputPath = "completed_1040_" + System.currentTimeMillis() + ".pdf";
            document.save(outputPath);
            System.out.println("Saved PDF to: " + outputPath);
        }
    }

    public Map<String, String> getFormFields() {
        return formFields;
    }

    public String getFieldValue(String fieldName) {
        return formFields.get(fieldName);
    }

    public void printAllFields() throws IOException {
        if (currentPDF == null) return;
        
        try (PDDocument document = PDDocument.load(currentPDF)) {
            PDAcroForm form = document.getDocumentCatalog().getAcroForm();
            if (form != null) {
                for (PDField field : form.getFields()) {
                    System.out.println("Field: " + field.getFullyQualifiedName() + 
                                     " Type: " + field.getClass().getSimpleName());
                }
            }
        }
    }
}