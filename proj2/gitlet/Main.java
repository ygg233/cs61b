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
                Utils.validateArgsLength(args, 1);
                Repository.init();
                break;
            case "add":
                Utils.validateArgsLength(args, 2);
                String fileToAdd = args[1];
                Repository.add(fileToAdd);
                break;
            case "commit":
                Utils.validateArgsLength(args, 2);
                String commitMessage = args[1];
                Repository.commit(commitMessage);
                break;
            case "rm":
                Utils.validateArgsLength(args, 2);
                String fileToRemove = args[1];
                Repository.remove(fileToRemove);
                break;
            case "log":
                Utils.validateArgsLength(args, 1);
                Repository.log();
                break;
            case "global-log":
                Utils.validateArgsLength(args, 1);
                Repository.globalLog();
                break;
            case "find":
                Utils.validateArgsLength(args, 2);
                String msgToFind = args[1];
                Repository.find(msgToFind);
                break;
            case "status":
                Utils.validateArgsLength(args, 1);
                Repository.status();
                break;
            case "checkout":
                Utils.validateCheckoutArgs(args);
                break;
            case "branch":
                Utils.validateArgsLength(args, 2);
                String newBranchName = args[1];
                Repository.createNewBranch(newBranchName);
                break;
            case "rm-branch":
                Utils.validateArgsLength(args, 2);
                String targetBranch = args[1];
                Repository.removeBranch(targetBranch);
                break;
            case "reset":
                Utils.validateArgsLength(args, 2);
                String commitId = args[1];
                Repository.reset(commitId);
                break;
            default:
                Utils.message("No command with that name exists.");
                System.exit(0);
        }
    }
}
