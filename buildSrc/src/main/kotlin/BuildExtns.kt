import org.gradle.api.Project
import term.bold
import term.cyan
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Kotlin build script extension functions.
 *
 * @author Suresh
 */
val GradleSnapShotURL = "gradle.snap.url".sysProp

/**
 * Returns the system property value of given string.
 */
val String.sysProp: String get() = System.getProperty(this, "")

fun getGskURL(version: String, type: org.gradle.api.tasks.wrapper.Wrapper.DistributionType = org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL) = "$GradleSnapShotURL/gradle-script-kotlin-$version-${type.name.toLowerCase()}.zip"

val buildDateTime by lazy { ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a z")) }

/**
 * Returns [GithubRepo] config for the current project.
 *
 * The regex basically supports the following kinds of Github repo urls,
 *  - git://github.com/some-user/my-repo.git/
 *  - git@github.com:some-user/my-repo.git
 *  - https://github.com/some-user/my-repo.git
 *  - https://github.com/some-user/my-repo
 *
 * @see The regex representation - https://goo.gl/f95P06
 */
val githubRepo by lazy {
    val repoUrlRegex = "(git|ssh|https?)(?:@|://)([\\w.-]+)[:/]([\\w.-]+)/([\\w-]+)(?:\\.git)?".toRegex()
    val repoUrl = "github.repo.url".sysProp
    repoUrlRegex.find(repoUrl, 0)?.groups?.let {
        GithubRepo(proto = it[1]!!.value,
                baseUrl = it[2]!!.value,
                user = it[3]!!.value,
                repo = it[4]!!.value)
    } ?: throw IllegalArgumentException("Invalid Github repo url: $repoUrl")
}

/**
 * Prints the project header.
 */
fun Project.printHeader(version: Any?) {
    val header = """|===============================
                    |Building Install Certs v$version
                    |===============================
                 """.trimMargin()
    println(header.bold.cyan)
    println()
}

/**
 * Gets the value of the specified environment variable. If it's not set (null),
 * it will prompt the user to enter value on the system console. For password
 * input masking, run gradle with "--no-deamon" option.
 *
 * @param envVar system environment variable.
 * @param mask [true] if the env value echoing is disabled on console.
 */
fun Project.getEnv(envVar: String, mask: Boolean = true): String {
    var env = System.getenv(envVar)
    if (env == null) {
        val con = System.console()
        val msg = "> Please enter ${envVar.bold}: "
        env = when (con) {
        // daemon mode.
            null -> {
                println(msg)
                readLine() ?: ""
            }
        //--no-daemon
            else -> when (mask) {
                true -> String(con.readPassword(msg) ?: "".toCharArray())
                else -> con.readLine(msg) ?: ""
            }
        }
    }
    return env
}

/**
 * Represents a  Github repository.
 */
data class GithubRepo(val proto: String,
                      val baseUrl: String,
                      val user: String,
                      val repo: String,
                      val branch: String = "master",
                      val url: String = "https://$baseUrl/$user/$repo") {

    /**
     * Returns the github release url for the specific [tag].
     */
    fun releaseUrl(tag: String = "latest") = when (tag) {
        "latest" -> "$url/releases/latest"
        else -> "$url/releases/tag/$tag"
    }

    /**
     * Returns README.md url for the [branch].
     */
    fun readmeUrl(branch: String = this.branch) = "$url/blob/$branch/README.md"

    /**
     *  Returns CHANGELOG.md url for the specified [branch] and [tag].
     */
    fun changelogUrl(branch: String = this.branch, tag: String? = null): String {
        val suffix = if (tag != null) "#${tag.replace(".", "")}" else ""
        return "$url/blob/$branch/CHANGELOG.md$suffix"
    }
}

/**
 * Dokka output format.
 */
enum class DokkaFormat(val type: String, val desc: String) {
    Html("html", "HTML Doc"),
    KotlinWeb("kotlin-website", "Kotlin Website"),
    Markdown("markdown", "Markdown(md) doc"),
    Gfm("gfm", "GitHub-Flavored Markdown"),
    Jekyll("jekyll", "Markdown adapted for Jekyll sites"),
    JavaDoc("javadoc", "Javadoc format")
}