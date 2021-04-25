package projeto

import java.io.File

fun fileGenerator(o: String) { //gera um ficheiro json com a string inserida
    val fileName = "my json.json"

    var file = File(fileName)

    file.writeText(o)
}

fun jsonGenerator(o: Any): String { //gera o texto json do objeto inserido

    if(o is List<*> || o is Map<*,*>) {
        val oj = JsonObject(o)
        var ja = JsonArray(o, oj)
        ja.createJson()
        return ja.createJson()
    }

    val mj = MyJSON()
    val oj = JsonObject(o)
    oj.accept(mj)
    return mj.textoJson
}
