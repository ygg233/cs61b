package gitlet;

import jdk.jshell.execution.Util;

import java.io.File;
import java.io.Serializable;
import java.util.*;

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

    public static Branch getCurrentBranch() {
        String currentBranchName = readContentsAsString(HEAD);
        return readBranch(currentBranchName);
    }

    public static Branch readBranch(String branchName) {
        File branchFile = join(BRANCH_DIR, branchName);
        return readObject(branchFile, Branch.class);
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
        Map<String, String> addedStagingFiles = stagingArea.getAddedFiles();
        Set<String> removedStagingFiles = stagingArea.getRemovedFiles();

        Blob blob = new Blob(fileToAdd);
        String blobSha1Ref = blob.getSha1Ref();

        if (trackedFiles != null &&
                trackedFiles.containsKey(fileName) &&
                trackedFiles.get(fileName).equals(blobSha1Ref)) {
            if (addedStagingFiles.containsKey(fileName)) {
                addedStagingFiles.remove(fileName);
            }

            if (removedStagingFiles.contains(fileName)){
                removedStagingFiles.remove(fileName);
            }
        } else {
            addedStagingFiles.put(fileName, blobSha1Ref);
            blob.save();
        }

        stagingArea.save();
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
        stagingArea.save();
    }

    public static void remove(String fileName) {
        // git rm --cached
        StagingArea stagingArea = getStagingArea();
        Map<String, String> stagingAddedFiles = stagingArea.getAddedFiles();
        Set<String> removedStagingFiles = stagingArea.getRemovedFiles();

        Commit currentCommit = getCurrentCommit();
        Map<String, String> trackedFiles = currentCommit.getFilesRef();

        boolean isStaged = stagingAddedFiles.containsKey(fileName);
        boolean isTracked = trackedFiles.containsKey(fileName);

        if (!isStaged && !isTracked) {
            Utils.message("No reason to remove the file.");
            System.exit(0);
        }

        if (isStaged) {
            stagingAddedFiles.remove(fileName);
        }

        if (isTracked) {
            removedStagingFiles.add((fileName));
            File fileToRemove = join(CWD, fileName);
            fileToRemove.delete();
        }

        stagingArea.save();
    }

    public static void log() {
        Commit currentCommit = getCurrentCommit();

        while (currentCommit != null) {
            printCommitInfo(currentCommit);
            String parentId = currentCommit.getDefaultParentCommit();
            currentCommit = parentId == null ? null : readCommit(parentId);
        }
    }

    public static void globalLog() {
        List<String> commitSha1Ids = plainFilenamesIn(COMMIT_DIR);
        for (String sha1Id: commitSha1Ids) {
            Commit commit = readCommit(sha1Id);
            printCommitInfo(commit);
        }
    }

    public static void find(String msg) {
        List<String> commitSha1Ids = plainFilenamesIn(COMMIT_DIR);
        int matchedCommitCount = 0;
        for (String sha1Id: commitSha1Ids) {
            Commit commit = readCommit(sha1Id);
            if (commit.getMessage().equals(msg)) {
                System.out.println(commit.getSha1Ref());
                matchedCommitCount++;
            }
        }
        if (matchedCommitCount == 0) {
            message("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static Commit readCommit(String sha1Id) {
        File commitFile = join(COMMIT_DIR, sha1Id);
        return readObject(commitFile, Commit.class);
    }

    public static void printCommitInfo(Commit commit) {
        String sha1Id = commit.getSha1Ref();
        Date date = commit.getTimestamp();
        String commitMsg = commit.getMessage();

        System.out.println("===");
        System.out.println("commit " + sha1Id);
        System.out.println("Date: " + convertDateInFormat(date));
        System.out.println(commitMsg);
        System.out.println();
    }

    public static void status() {
        System.out.println("=== Branches ===");
        String currentBranch = getCurrentBranch().getBranchName();
        List<String> branches = plainFilenamesIn(BRANCH_DIR);
        Collections.sort(branches);
        for (String branch: branches) {
            if (currentBranch.equals(branch)) {
                System.out.print('*');
            }
            System.out.println(branch);
        }
        System.out.println();

        StagingArea stagingArea = getStagingArea();
        Map<String, String> stagedAddedFiles = stagingArea.getAddedFiles();
        System.out.println("=== Staged Files ===");
        List<String> stagedAddedList = new ArrayList<>(stagedAddedFiles.keySet());
        Collections.sort(stagedAddedList);
        for (String addedFile: stagedAddedList) {
            System.out.println(addedFile);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        Set<String> stagedRemovedFiles = stagingArea.getRemovedFiles();
        List<String> stagedRemovedList = new ArrayList<>(stagedRemovedFiles);
        Collections.sort(stagedRemovedList);
        for (String removedFile: stagedRemovedList) {
            System.out.println(removedFile);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        Commit currentCommit = getCurrentCommit();
        Set<String> modifiedNotStaged = new TreeSet<>();
        Map<String, String> trackedFiles = currentCommit.getFilesRef();

        // tracked files are modified or deleted and have not been staged yet
        for (String trackedFile: trackedFiles.keySet()) {
            File workingDirectorFile = join(CWD, trackedFile);
            if (workingDirectorFile.exists()) {
                String workingBlobId = sha1(readContents(workingDirectorFile));
                String trackedBlobId = trackedFiles.get(trackedFile);
                if (!workingBlobId.equals(trackedBlobId) && !stagedAddedFiles.containsKey(trackedFile)) {
                    modifiedNotStaged.add(trackedFile + " (modified)");
                }
            } else {
                if (!stagedRemovedFiles.contains(trackedFile)) {
                    modifiedNotStaged.add(trackedFile + " (deleted)");
                }
            }
        }

        // staged files are modified or deleted
        for (String stagedAddedFile: stagedAddedFiles.keySet()) {
            File workingDirectorFile = join(CWD, stagedAddedFile);
            if (workingDirectorFile.exists()) {
                String workingBlobSha1Id = sha1(readContents(workingDirectorFile));
                String stagedAddedBlobSha1Id = stagedAddedFiles.get(stagedAddedFile);
                if (!workingBlobSha1Id.equals(stagedAddedBlobSha1Id)) {
                    modifiedNotStaged.add(stagedAddedFile + " (modified)");
                }
            } else {
                modifiedNotStaged.add(stagedAddedFile + " (deleted)");
            }
        }

        modifiedNotStaged.forEach(System.out::println);
        System.out.println();

        System.out.println("=== Untracked Files ===");
        List<String> workingFiles = plainFilenamesIn(CWD);
        Set<String> untracked = new TreeSet<>();
        for (String workingFile: workingFiles) {
            File file = join(CWD, workingFile);
            if (file.isDirectory()) {
                continue;
            }
            if (!trackedFiles.containsKey(workingFile) && !stagedAddedFiles.containsKey(workingFile) && !stagedRemovedFiles.contains(workingFile)) {
                untracked.add(workingFile);
            }
        }
        untracked.forEach(System.out::println);
        System.out.println();
    }

    public static void checkoutFile(String commitSha1Id, String fileName) {
        if (commitSha1Id == null) {
            commitSha1Id = getCurrentCommit().getSha1Ref();
        } else {
            commitSha1Id = findFullCommitIdByPrefix(commitSha1Id);
            if (commitSha1Id == null) {
                message("No commit with that id exists.");
                System.exit(0);
            }
        }

        Commit targetCommit = readCommit(commitSha1Id);

        if (!targetCommit.getFilesRef().containsKey(fileName)) {
            message("File does not exist in that commit.");
            System.exit(0);
        }

        StagingArea stagingArea = getStagingArea();
        Map<String, String> stagingAddedFiles = stagingArea.getAddedFiles();
        Set<String> stagingRemovedFiles = stagingArea.getRemovedFiles();

        stagingAddedFiles.remove(fileName);
        stagingRemovedFiles.remove(fileName);
        stagingArea.save();

        restoreFile(fileName, targetCommit.getFilesRef().get(fileName));
    }

    public static void restoreFile(String fileName, String blobId) {
        File blobFile = join(BLOB_DIR, blobId);
        File targetFile = join(CWD, fileName);
        writeContents(targetFile, readContents(blobFile));
    }

    public static String findFullCommitIdByPrefix(String commitIdIdPrefix) {
        List<String> commits = plainFilenamesIn(COMMIT_DIR);
        List<String> matches = new ArrayList<>();
        for (String commit: commits) {
            if (commit.startsWith(commitIdIdPrefix)) {
                matches.add(commit);
            }
            if (matches.size() > 1) {
                message("Ambiguous commit ID prefix.");
                System.exit(0);
            }
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    public static void checkoutBranch(String targetBranchName) {
        List<String> branches = plainFilenamesIn(BRANCH_DIR);
        if (!branches.contains(targetBranchName)) {
            message("No such branch exists.");
            System.exit(0);
        }

        Branch currentBranch = getCurrentBranch();
        if (currentBranch.getBranchName().equals(targetBranchName)) {
            message("No need to checkout the current branch.");
            System.exit(0);
        }

        Branch targetBranch = readBranch(targetBranchName);
        Commit targetCommit = targetBranch.getCommit();

        checkUntrackedFilesOverwrite(targetCommit);

        // clear staging area
        StagingArea stagingArea = getStagingArea();
        stagingArea.clear();
        stagingArea.save();

        overwriteWorkingDirectory(targetCommit);
        deleteUntrackedInTarget(targetCommit);

        // update HEAD
        // previously update HEAD deletingUntrackedInTarget, which will modify the current commit
        // then cause the deleteUntrackedInTarget does not work
        writeContents(HEAD, targetBranchName);
    }

    public static void checkUntrackedFilesOverwrite(Commit targetCommit) {
        List<String> cwdFiles = plainFilenamesIn(CWD);
        Map<String, String> targetTracked = targetCommit.getFilesRef();
        Map<String, String> currentTracked = getCurrentCommit().getFilesRef();

        for (String file: cwdFiles) {
            if (join(CWD, file).isDirectory()) {
                continue;
            }

            boolean isTrackedInCurrentCommit = currentTracked.containsKey(file);
            boolean isStaged = getStagingArea().getAddedFiles().containsKey(file);
            boolean isTrackedInTargetCommit = targetTracked.containsKey(file);

            if (isTrackedInTargetCommit && !isStaged && !isTrackedInCurrentCommit) {
                message("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    public static void overwriteWorkingDirectory(Commit targetCommit) {
        Map<String, String> targetTracked = targetCommit.getFilesRef();
        for (Map.Entry<String, String> entry: targetTracked.entrySet()) {
            String fileName = entry.getKey();
            String blobId = entry.getValue();
            restoreFile(fileName, blobId);
        }
    }

    public static void deleteUntrackedInTarget(Commit targetCommit) {
        Commit currentCommit = getCurrentCommit();
        Set<String> currentTracked = currentCommit.getFilesRef().keySet();
        Map<String, String> targetTracked = targetCommit.getFilesRef();

        for (String fileName: currentTracked) {
            if (!targetTracked.containsKey(fileName)) {
                File fileToDelete = join(CWD, fileName);
                if (fileToDelete.exists()) { // file could be staged to be removed
                    fileToDelete.delete();
                }
            }
        }
    }

    public static void createNewBranch(String newBranchName) {
        if (plainFilenamesIn(BRANCH_DIR).contains(newBranchName)) {
            message("A branch with that name already exists.");
            System.exit(0);
        }
        Commit currentCommit = getCurrentCommit();
        Branch newBranch = new Branch(newBranchName, currentCommit);
        newBranch.save();
    }

    public static void removeBranch(String targetBranch) {
        if (!plainFilenamesIn(BRANCH_DIR).contains(targetBranch)) {
            message("A branch with that name does not exist.");
            System.exit(0);
        }

        Branch currentBranch = getCurrentBranch();
        if (currentBranch.getBranchName().equals(targetBranch)) {
            message("Cannot remove the current branch.");
            System.exit(0);
        }

        File branchFile = join(BRANCH_DIR, targetBranch);
        branchFile.delete();
    }

    public static void reset(String commitIdPrefix) {
        String commitId = findFullCommitIdByPrefix(commitIdPrefix);
        if (commitId == null) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        }

        Commit targetCommit = readCommit(commitId);

        checkUntrackedFilesOverwrite(targetCommit);

        StagingArea stagingArea = getStagingArea();
        stagingArea.clear();
        stagingArea.save();

        overwriteWorkingDirectory(targetCommit);

        Branch currentBranch = getCurrentBranch();
        currentBranch.updateCommit(targetCommit);
        currentBranch.save();
    }
}
