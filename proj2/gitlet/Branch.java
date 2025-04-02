package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Branch implements Serializable {
    private String branchName;
    private Commit commit;

    public Branch(String branchName, Commit commit) {
        this.branchName = branchName;
        this.commit = commit;
    }

    public void save() {
        File branchFile = Utils.join(Repository.BRANCH_DIR, branchName);
        Utils.writeObject(branchFile, this);

        if (!branchFile.exists()) {
            try {
                branchFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateCommit(Commit newCommit) {
        this.commit = newCommit;
    }

    public Commit getCommit() {
        return commit;
    }
}
