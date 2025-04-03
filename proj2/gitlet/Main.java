package gitlet;

import java.io.File;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            Utils.message("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                Repository.init();
                break;
            case "add":
                String fileToAdd = args[1];
                Repository.add(fileToAdd);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                String commitMessage = args[1];
                Repository.commit(commitMessage);
                break;
            case "rm":
                String fileToRemove = args[1];
                Repository.remove(fileToRemove);
                break;
            case "log":
                Repository.log();
                break;
            case "global-log":
                Repository.globalLog();
                break;
            case "find":
                String msgToFind = args[1];
                Repository.find(msgToFind);
                break;
            case "status":
                Repository.status();
                break;
            default:
                Utils.message("No command with that name exists.");
                System.exit(0);
        }

    }
}
