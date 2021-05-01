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
        var ja = JsonArray(o, null)
        ja.createJson()
        return ja.createJson()
    }

    if(o is String) {
        val mj = MyJSON()
        val oj = JsonObject(o)
        oj.accept(mj)
        return mj.textoJson.substringBefore(",") + "\n}"
    }

    val mj = MyJSON()
    val oj = JsonObject(o)
    oj.accept(mj)
    return mj.textoJson
}
