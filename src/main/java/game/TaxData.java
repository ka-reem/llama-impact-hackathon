package game;

import java.util.*;

public class TaxData {
    private Map<String, String> answers = new HashMap<>();
    private List<String> fieldOrder = Arrays.asList(
        "topmostSubform[0].Page1[0].f1_02[0]",  // Name
        "topmostSubform[0].Page1[0].f1_04[0]",  // SSN
        "topmostSubform[0].Page1[0].f1_08[0]",  // Address
        "topmostSubform[0].Page1[0].c1_1[0]",   // Filing Status
        "topmostSubform[0].Page1[0].Line1[0].f1_12[0]"  // Income
    );
    private Set<String> requiredFields = new HashSet<>(Arrays.asList(
        "f1_02", // Name
        "f1_04", // SSN
        "f1_08", // Address
        "c1_01", // Filing Status
        "f1_12"  // Income
    ));
    private double income;
    private boolean isComplete;

    public void addAnswer(String fieldName, String value) {
        answers.put(fieldName, value);
        if (fieldName.equals("f1_12")) {
            try {
                income = Double.parseDouble(value.replaceAll("[^0-9.]", ""));
            } catch (NumberFormatException e) {
                income = 0;
            }
        }
        checkCompletion();
    }

    public Map<String, String> getAllAnswers() {
        return answers;
    }

    public void setIncome(double income) {
        this.income = income;
        answers.put("f1_12", String.valueOf(income));
        checkCompletion();
    }

    public double getIncome() {
        return income;
    }

    private void checkCompletion() {
        isComplete = income > 0 && answers.keySet().containsAll(requiredFields);
    }

    public boolean isComplete() {
        return isComplete;
    }

    public String getNextRequiredField() {
        for (String field : fieldOrder) {
            if (!answers.containsKey(field)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (answers.containsKey("topmostSubform[0].Page1[0].f1_02[0]")) 
            sb.append("Name: ").append(answers.get("topmostSubform[0].Page1[0].f1_02[0]")).append(", ");
        if (answers.containsKey("topmostSubform[0].Page1[0].f1_04[0]")) {
            String ssn = answers.get("topmostSubform[0].Page1[0].f1_04[0]");
            sb.append("SSN: ").append(maskSSN(ssn)).append(", ");
        }
        if (answers.containsKey("topmostSubform[0].Page1[0].f1_08[0]"))
            sb.append("Address: ").append(answers.get("topmostSubform[0].Page1[0].f1_08[0]")).append(", ");
        if (answers.containsKey("topmostSubform[0].Page1[0].c1_1[0]"))
            sb.append("Filing Status: ").append(answers.get("topmostSubform[0].Page1[0].c1_1[0]")).append(", ");
        if (answers.containsKey("topmostSubform[0].Page1[0].Line1[0].f1_12[0]"))
            sb.append("Income: $").append(answers.get("topmostSubform[0].Page1[0].Line1[0].f1_12[0]"));
        return sb.toString();
    }

    private String maskSSN(String ssn) {
        if (ssn == null || ssn.length() < 4) return "***-**-****";
        return "***-**-" + ssn.substring(Math.max(0, ssn.length() - 4));
    }

    public String getFormattedSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("=== Tax Information ===\n");
        Map<String, String> currentAnswers = getAllAnswers();
        for (Map.Entry<String, String> entry : currentAnswers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.contains("f1_02")) summary.append("Name: ").append(value).append("\n");
            else if (key.contains("f1_04")) summary.append("SSN: ").append(maskSSN(value)).append("\n");
            else if (key.contains("f1_08")) summary.append("Address: ").append(value).append("\n");
            else if (key.contains("c1_1")) summary.append("Filing Status: ").append(value).append("\n");
            else if (key.contains("f1_12")) summary.append("Income: $").append(value).append("\n");
        }
        summary.append("=====================");
        return summary.toString();
    }
}