package projeto

import org.eclipse.swt.widgets.Tree
import org.eclipse.swt.widgets.TreeItem
import java.io.File

class JsonGenerator() {
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
    fun jsonGenerator(o: Any?): String {

        if(o is List<*> || o is Map<*,*>) {
            val ja = JsonArray(o, null)
            ja.naoTemJsonObject = true
            return "[\n" + ja.obterJsonGerado()
        }

        if(o is String) {
            val mj = MyJSON()
            val oj = JsonObject(o)
            oj.accept(mj)
            return mj.textoJson.substringBefore(",") + "\n}"
        }

        if(o == null) {
            val mj = MyJSON()
            val oj = JsonObject(0)
            oj.recebeuNull = true
            oj.objetoRecebido = 0
            oj.accept(mj)
            return mj.textoJson.substring(0, mj.textoJson.length - 2).substring(1)
        }

        val mj = MyJSON()
        val oj = JsonObject(o)
        oj.accept(mj)
        return mj.textoJson.substring(0, mj.textoJson.length - 2).substring(1)
    }

}