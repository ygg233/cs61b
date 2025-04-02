package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StagingArea implements Serializable {
    private Map<String, String> addedFiles = new HashMap<>();
    private Set<String> removedFiles = new HashSet<>();

    public Map<String, String> getAddedFiles() {
        return addedFiles;
    }

    public Set<String> getRemovedFiles() {
        return removedFiles;
    }

    public void clear() {
        addedFiles.clear();
        removedFiles.clear();
    }
}
