import org.eclipse.jgit.api.*
import org.eclipse.jgit.internal.storage.file.*
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.revwalk.*
import org.eclipse.jgit.treewalk.*
import org.eclipse.jgit.treewalk.filter.*
import org.junit.*
import java.io.*

class GitTest {
    @Test
    fun xasd() {
        Git.wrap(FileRepository(File(".git"))).use { xx ->
            // find the HEAD
            val repository = xx.repository
            val lastCommitId = repository.resolve(Constants.HEAD)

            // a RevWalk allows to walk over commits based on some filtering that is defined
            RevWalk(repository).use { revWalk ->
                val commit = revWalk.parseCommit(lastCommitId)
                // and using commit's tree find the path
                val tree = commit.tree
                println("Having tree: $tree")

                // now try to find a specific file
                TreeWalk(repository).use { treeWalk ->
                    treeWalk.addTree(tree)
                    treeWalk.isRecursive = true
                    treeWalk.filter = PathSuffixFilter.create("kt")
                    check(treeWalk.next()) { "Did not find expected file 'README.md'" }

                    val objectId = treeWalk.getObjectId(0)
                    val loader = repository.open(objectId)

                    // and then one can the loader to read the file
                    loader.copyTo(System.out)
                }

                revWalk.dispose()
            }
        }
    }
}