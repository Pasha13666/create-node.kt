import java.io.File
import java.net.URL
import kotlin.system.exitProcess

const val VERSION = "0.1.1"
val LOCAL_MODULES_RE = "^[ \\t]*([a-zA-Z_][a-zA-Z0-9_-]*)[ \\t]*(==|<=|>=|<|>)[ \\t]*([0-9]+(?:\\.[0-9]+)*|\\*)[ \\t]*$".toRegex()
val GLOBAL_MODULES_RE = "^[ \\t]*([a-zA-Z_][a-zA-Z0-9_-]*)[ \\t]+((?:[0-9]+(?:\\.[0-9]+)*)(?:[ \\t]+(?:[0-9]+(?:\\.[0-9]+)*))*)[ \\t]*$".toRegex()
var server = "https://raw.githubusercontent.com/Pasha13666/create-node.kt/repo/"
val modules = mutableMapOf<String, String>()

private fun usage(status: Int){
    print("""Usage: create-node.sh [OPTION...] [DIRECTORY] [MODULE...]
Where OPTIONS is:
    -h, --help      Show this help.
    -v, --version   Print version and exit.
    --server=URL    Use URL as base server url to download files.
DIRECTORY is path to new node, default is current dir.
MODULE is name of module to install. New modules will be added to end of
  `%DIRECTORY%/modules.list`.
""")
    exitProcess(status)
}

private fun parseArgs(args: Array<String>) {
    var ks = 0
    loop@ for ((k, i) in args.withIndex())
        when (i) {
            "-h", "--help" -> usage(0)
            "-v", "--version" -> {
                println("create-node.sh v$VERSION")
                exitProcess(0)
            }
            else -> {
                if (i.startsWith("--server="))
                    server = i.substring(9)
                else {
                    ks = k
                    break@loop
                }
            }
        }

    if (ks in args.indices){
        if (args[ks][0] == '-')
            usage(1)
        File(args[ks]).mkdirs()
        System.setProperty("user.dir", args[ks++]) // cd args[ks]
    }

    for (i in ks..args.lastIndex)
        if (args[ks][0] == '-')
            usage(1)
        else modules[args[i]] = "==*"
}

fun main(args: Array<String>){
    parseArgs(args)

    File("modules.list").absoluteFile.apply {
        if (!exists())
            createNewFile()

        forEachLine(Charsets.UTF_8){
            if (it.isEmpty() || it[0] == '#')
                return@forEachLine

            val r = LOCAL_MODULES_RE.find(it) ?: return@forEachLine
            if (r.groupValues[1] !in modules || modules[r.groupValues[1]] == "==*")
                modules[r.groupValues[1]] = r.groupValues[2] + r.groupValues[3]
        }
    }

    val modinfo = mutableMapOf<String, MutableList<List<String>>>()
    URL("$server/modules.txt").openStream().bufferedReader(Charsets.UTF_8).forEachLine {
        if (it.isEmpty() || it[0] == '#')
            return@forEachLine

        val r = GLOBAL_MODULES_RE.find(it) ?: return@forEachLine
        modinfo[r.groupValues[1]] = r.groupValues[2].split(' ', '\t').map { it.split('.') }.toMutableList()
    }

    val files = mutableMapOf<String, String>()
    modules.forEach { name, version ->
        if (name !in modinfo) {
            println("Invalid module: $name ($version)!")
            exitProcess(2)
        }

        val v = if (version != "==*") compareVersion(version, modinfo[name]!!)
        else modinfo[name]?.maxWith(Comparator(::vercmp))?.joinToString(".")
        if (v == null) {
            println("Module $name has no version $version!")
            exitProcess(2)
        }

        URL("$server/modules/$name/$v/module.txt").openStream().bufferedReader(Charsets.UTF_8)
                .forEachLine {
            val (file, uri) = it.split("[ \\t]+".toRegex(), limit = 2)
            files[file] = if (uri[0] == '@') uri.substring(1) else "$server/modules/$name/$v/$uri"
        }
    }

    files.forEach { file, uri ->
        println("Downloading $uri...")
        val f = File(file).absoluteFile!!
        if (f.exists())
            return@forEach
        f.parentFile.mkdirs()
        URL(uri).openStream().copyTo(f.outputStream())
    }

    println("Successfully created node.kt!")
}

private fun vercmp(o1: List<String>, o2: List<String>) =
    (0..minOf(o1.lastIndex, o2.lastIndex)).map { o1[it].compareTo(o2[it]) }.firstOrNull { it != 0 }
            ?: o1.size.compareTo(o2.size)

private fun compareVersion(version: String, versions: List<List<String>>): String? {
    if (version[1] == '=') version.substring(2).let {
        if (it.split('.') in versions) return it
    }
    val ve = version.substring(if (version[1] == '=') 2 else 1).split('.')
    return when (version[0]){
        '<' -> versions.firstOrNull { vercmp(it, ve) < 0 }
        '>' -> versions.firstOrNull { vercmp(it, ve) > 0 }
        // Bad regexp???
        else -> throw RuntimeException("Invalid module version.")
    }?.joinToString(".")
}
