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

    public void saveBranch() {
        File branch = Utils.join(Repository.BRANCH_DIR, branchName);
        Utils.writeObject(branch, this);
        try {
            branch.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
