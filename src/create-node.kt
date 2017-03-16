import java.io.File
import java.net.URL
import kotlin.system.exitProcess

const val VERSION = "0.0.1"
var server = "https://raw.githubusercontent.com/Pasha13666/create-node.sh/repo/"
var verbose = false
val files = mutableMapOf<String, String>()
val modules = mutableSetOf<String>()

fun usage(status: Int){
    print("""Usage: create-node.sh [ARGS]...
Where ARGS is:
    -?, -h, -help, --help       Show this help.
    -d=DIR, -directory=DIR      Create node in DIR, not in current directory.
    -S=URL, -server=URL         Use URL as base server url to download files.
    -m=MODULE, -module=MODULE   Use MODULE. This option can be repeated.
    -v, -verbose                Make more output.
    -V, -version                Print version and exit.
""")
    exitProcess(status)
}

fun download(file: String) = URL(server + file).openStream().buffered()

fun parseModule(k: String, v: String){
    when (k){
        "Library" -> files["lib/$v"] = "libraries/$v"
        "Component" -> files[v] = "files/$v"
        "Module" ->{
            val mod = v.split(':')
            if (mod[0] !in modules)
                return
            modules.remove(mod[0])
            if (verbose)
                println("Appending module ${mod[0]}, version=${mod[1]}, type=${mod[2]}")
            when (mod[2]){
                "Library" -> mod.listIterator(3).forEach { files["lib/$it"] = "modules/${mod[0]}/${mod[1]}/$it" }
                "Component" -> mod.listIterator(3).forEach { files[it] = "modules/${mod[0]}/${mod[1]}/$it" }
                "Directory" -> mod.listIterator(3).forEach { File(it).mkdirs() }
                "Group" -> modules.addAll(mod.subList(3, mod.size))
            }
        }
        "Directory" -> File(v).mkdirs()
    }
}

fun parseArgs(args: Array<String>){
    for(i in args){
        if (i[0] != '-')
            usage(1)
        var k = i
        var v = ""
        if ('=' in i) {
            val t = i.substring(2).split('=', limit = 2)
            k = t[0]
            v = t[1]
        }

        when (k){
            "m", "module" -> modules.add(v)
            "d", "directory" -> System.setProperty("user.dir", v)
            "S", "server-url" -> server = v
            "?", "h", "help", "-help" -> usage(0)
            "v", "verbose" -> verbose = true
            "V", "version" -> {
                println("create-node.sh v$VERSION")
                exitProcess(0)
            }
            else -> usage(1)
        }
    }
}

fun main(args: Array<String>){
    parseArgs(args)

    download("nodes.lst").reader(Charsets.UTF_8).forEachLine {
        if (':' !in it)
            return@forEachLine

        val (k, v) = it.split(':', limit = 2)
        parseModule(k, v)
    }

    if (modules.isNotEmpty()){
        println("Invalid module name: ${modules.first()}!")
        exitProcess(2)
    }

    files.forEach { uri, file ->
        if (verbose)
            println("Downloading $server$uri...")
        val f = File(file)
        if (f.exists())
            return@forEach
        f.parentFile.mkdirs()
        download(uri).copyTo(f.outputStream())
    }

    println("Successfully created node.kt!")
}
