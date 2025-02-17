import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import java.util.Collections;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for switching between repositories and commits for testing GPT's ability to improve student code.
 */
public class JGitRepoHandler {
    private Repository repository;
    private Git git;

    public JGitRepoHandler(String repoPath) throws IOException {
        openRepository(repoPath);
    }

    public void openRepository(String repoPath) throws IOException {
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repositoryBuilder.setGitDir(new File(repoPath + "/.git"));
        repository = repositoryBuilder.build();
        git = new Git(repository);
    }

    public void switchToCommit(String commitHash) throws IOException, GitAPIException {
        // Remove untracked files
        git.clean().setCleanDirectories(true).setForce(true).setIgnore(false).call();

        // Discard any changes in the working directory
        git.reset().setMode(ResetCommand.ResetType.HARD).call();

        ObjectId commitObjectId = repository.resolve(commitHash);
        // Switch to the target commit
        CheckoutCommand checkoutCommand = git.checkout();
        checkoutCommand.setName(commitObjectId.getName()).call();
    }

    public void close() {
        git.close();
        repository.close();
    }

}
