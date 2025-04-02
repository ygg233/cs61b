package gitlet;

import jdk.jshell.execution.Util;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository implements Serializable {
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
    public static final File STAGING = join(GITLET_DIR, "staging_area");

    /** The HEAD pointer that points to the current working commit. */
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    private static Map<String, String> blobs;

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
        initialCommit.save();

        Branch masterBranch = new Branch("master", initialCommit);
        masterBranch.save();

        writeContents(HEAD, "master");
    }

    public static StagingArea getStagingArea() {
        if (!STAGING.exists()) {
            return new StagingArea();
        } else {
            return readObject(STAGING, StagingArea.class);
        }
    }

    public static void saveStagingArea(StagingArea stagingArea) {
        writeObject(STAGING, stagingArea);
    }

    public static Branch getCurrentBranch() {
        String currentBranchName = readContentsAsString(HEAD);
        File branchFile = join(BRANCH_DIR, currentBranchName);
        Branch currentBranch = readObject(branchFile, Branch.class);
        return currentBranch;
    }

    public static Commit getCurrentCommit() {
        Branch currentBranch = getCurrentBranch();
        return currentBranch.getCommit();
    }

    public static void add(String fileName) {
        File fileToAdd = join(CWD, fileName);
        if (!fileToAdd.exists()) {
            Utils.message("File does not exist.");
            System.exit(0);
        }

        Commit currentCommit = getCurrentCommit();
        Map<String, String> trackedFiles = currentCommit.getFilesRef();

        StagingArea stagingArea = getStagingArea();
        Map<String, String> addedFiles = stagingArea.getAddedFiles();

        Blob blob = new Blob(fileToAdd);
        String blobSha1Ref = blob.getSha1Ref();

        if (trackedFiles != null &&
                trackedFiles.containsKey(fileName) &&
                trackedFiles.get(fileName).equals(blobSha1Ref)) {
            if (addedFiles != null) {
                addedFiles.remove(fileName);
            }

        } else {
            addedFiles.put(fileName, blobSha1Ref);
            blob.save();
        }

        saveStagingArea(stagingArea);
    }

    public static void commit(String commitMessage) {
        if (commitMessage.isEmpty()) {
            Utils.message("Please enter a commit message.");
            System.exit(0);
        }

        StagingArea stagingArea = getStagingArea();

        if (stagingArea.getRemovedFiles().isEmpty() && stagingArea.getAddedFiles().isEmpty()) {
            Utils.message("No changes added to the commit.");
            System.exit(0);
        }

        Commit parentCommit = getCurrentCommit();

        // make new commit
        Commit newCommit = new Commit(commitMessage, parentCommit, stagingArea);
        newCommit.save();

        // update the latest commit of the current branch
        Branch currentBranch = getCurrentBranch();
        currentBranch.updateCommit(newCommit);
        currentBranch.save();

        // clear the staging area
        stagingArea.clear();
        saveStagingArea(stagingArea);
    }
}
