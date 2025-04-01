package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.List;
import java.util.Map;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /** The timestamp of this Commit. */
    private String timestamp;

    /** The mapping of file name to blob reference
     * NOTICE: At beginning, I thought it can be added with multiple files but not! Read spec carefully!.
     * NOTICE2: OH NO! Surely that only one file can be 'add' one time, but not commit!
     * */
    private Map<String, String> filesRef;

    /** The reference to the default parent commit. */
    private String defaultParentCommit;

    /** The reference for the possible (for merges) second parent commit. */
    private String secondParentCommit;

    /** The unique reference to the object, which is computed by the SHA-1 algorithm. */
    private String sha1Ref;

    /* TODO: fill in the rest of this class. */

    public Commit() {
        this.message = "initial commit";
        this.timestamp = new Date(0).toString();
        this.sha1Ref = Utils.sha1(this.message, this.timestamp);
    }

    public Commit(String message, String timestamp) {
        
    }

    public void saveCommit() {
        File commitFile = Utils.join(Repository.COMMIT_DIR, this.sha1Ref);
        Utils.writeObject(commitFile, this);
        try {
            commitFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
