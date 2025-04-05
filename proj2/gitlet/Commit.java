package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Yang
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /** The timestamp of this Commit. */
    private Date timestamp;

    /** The mapping of file name to blob reference */
    private Map<String, String> filesRef = new HashMap<>();

    /** The reference to the default parent commit. */
    private Commit defaultParentCommit;

    /** The reference for the possible (for merges) second parent commit. */
    private Commit secondParentCommit;

    /** The unique reference to the object, which is computed by the SHA-1 algorithm. */
    private String sha1Ref;

    public Commit() {
        this.message = "initial commit";
        this.timestamp = new Date(0);
        this.sha1Ref = Utils.sha1(this.message, this.timestamp.toString());
    }

    public Commit(String message, Commit parent, StagingArea stagingArea) {
        this.message = message;
        this.timestamp = new Date();
        this.defaultParentCommit = parent;

        this.filesRef.putAll(parent.getFilesRef());

        this.filesRef.putAll(stagingArea.getAddedFiles());

        for (String file: stagingArea.getRemovedFiles()) {
            this.filesRef.remove(file);
        }

        this.sha1Ref = Utils.sha1(
                this.message,
                this.timestamp.toString(),
                this.filesRef.toString(),
                this.defaultParentCommit.toString()
        );
    }

    public Map<String, String> getFilesRef() {
        return filesRef;
    }

    public String getSha1Ref() {
        return sha1Ref;
    }

    public Commit getDefaultParentCommit() {
        return defaultParentCommit;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void save() {
        File commitFile = Utils.join(Repository.COMMIT_DIR, this.sha1Ref);
        Utils.writeObject(commitFile, this);
        try {
            commitFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
