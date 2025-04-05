package gitlet;

import java.util.*;

public class MergeResult {
    private Map<String, String> mergedFiles = new HashMap<>();
    private Set<String> removedFiles = new HashSet<>();
    private boolean hasConflict = false;
    private List<String> conflictFiles = new ArrayList<>();

    public boolean hasConflict() {
        return hasConflict;
    }

    public Map<String, String> getMergedFiles() {
        return mergedFiles;
    }

    public List<String> getConflictFiles() {
        return conflictFiles;
    }

    public Set<String> getRemovedFiles() {
        return removedFiles;
    }

    public void addMergedFile(String fileName, String blobId) {
        mergedFiles.put(fileName, blobId);
    }

    public void removeFiles(String fileName) {
        removedFiles.add(fileName);
    }

    void markConflict(String fileName) {
        hasConflict = true;
        conflictFiles.add(fileName);
    }
}
