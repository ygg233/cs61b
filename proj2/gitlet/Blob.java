package gitlet;

import java.io.File;

public class Blob {
    private final String sha1Ref;
    private final byte[] content;

    public Blob(File file) {
        this.content = Utils.readContents(file);
        this.sha1Ref = Utils.sha1(content);
    }

    /** Save the content into a file if the file with same content does not exist */
    public void save() {
        File blobFile = Utils.join(Repository.BLOB_DIR, sha1Ref);
        if (!blobFile.exists()) {
            Utils.writeObject(blobFile, content);
        }
    }

    public String getSha1Ref() {
        return sha1Ref;
    }
}
