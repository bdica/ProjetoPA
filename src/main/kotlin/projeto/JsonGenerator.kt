package projeto

import java.io.File

/**
 * Gera um ficheiro json com a string inserida
 *
 * @param o texto para criar o ficheiro
 */
fun fileGenerator(o: String) {
    val fileName = "JSON Gerado.json"

    var file = File(fileName)

    file.writeText(o)
}

/**
 * Gera o texto json do objeto inserido
 *
 * @param o objeto para gerar o json
 * @return texto do objeto em formato json
 */
fun jsonGenerator(o: Any): String {

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
