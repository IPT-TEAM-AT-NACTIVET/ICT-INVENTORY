package tz.go.nactvet.ict_inventory_management.dto;

import java.util.ArrayList;
import java.util.List;

public class CsvImportResult {

    private int imported;
    private int failed;
    private final List<RowError> errors = new ArrayList<>();

    public int getImported() {
        return imported;
    }

    public void setImported(int imported) {
        this.imported = imported;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public List<RowError> getErrors() {
        return errors;
    }

    public void addError(int row, String message) {
        this.errors.add(new RowError(row, message));
        this.failed++;
    }

    public static class RowError {
        private final int row;
        private final String message;

        public RowError(int row, String message) {
            this.row = row;
            this.message = message;
        }

        public int getRow() {
            return row;
        }

        public String getMessage() {
            return message;
        }
    }
}
