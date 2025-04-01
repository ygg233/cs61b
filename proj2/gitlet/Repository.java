package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** The object direcotry that stores the blob objects and commit objects. */
    public static final File OBJETS_DIR = join(GITLET_DIR, "objects");

    /** The blobs directory that stores the blob objects. */
    public static final File BLOB_DIR = join(OBJETS_DIR, "blobs");

    /** The commits directory that stores the blob objects. */
    public static final File COMMIT_DIR = join(OBJETS_DIR, "commits");

    /** The branch directory that store all the created branch name */
    public static final File BRANCH_DIR = join(GITLET_DIR, "branch");

    /** The staging area directory that store the files are added by 'add' command. */
    public static final File STAGING_DIR = join(GITLET_DIR, "staging");

    /** The HEAD pointer that points to the current working commit. */
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    /* TODO: fill in the rest of this class. */

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        GITLET_DIR.mkdir();
        OBJETS_DIR.mkdir();
        BLOB_DIR.mkdir();
        COMMIT_DIR.mkdir();
        BRANCH_DIR.mkdir();

        Commit initialCommit = new Commit();
        initialCommit.saveCommit();

        Branch masterBranch = new Branch("master", initialCommit);
        masterBranch.saveBranch();
    }
}
