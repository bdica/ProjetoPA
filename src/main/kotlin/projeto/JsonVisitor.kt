package projeto

interface Visitor { //interface com funções genéricas para serem implementadas
    fun visitJsonObject(node: JsonObject): Boolean
    fun visitJsonVariable(node: JsonVariable): Boolean
    fun visitJsonArray(node: JsonArray): Boolean
    fun endVisitJsonObject(node: JsonObject)
}

//Implementa o Visitor e diz o que fazem as funções do Visit (funções para escrever o json)
class MyJSON: Visitor {

    var textoJson = "{" //texto final

    override fun visitJsonObject(oj: JsonObject): Boolean {

        oj.readObject() //ao visitar um JsonObject, le as suas variaveis e coloca-as na lista children

        textoJson += "\n"

        return true
    }

    override fun endVisitJsonObject(node: JsonObject) {
        textoJson = textoJson.substring(0, textoJson.length - 2)
        textoJson += "\n}"
    }

    override fun visitJsonArray(ja: JsonArray): Boolean {

        var keyEncontrada = "" //para encontrar o nome da variavel e escrever no json

        ja.objeto.children.forEach { //para encontrar o nome da variavel e escrever no json
            var valor = ja //value do elemento da lista lido

            val mapIterator = ja.objeto.children.iterator()

            while (mapIterator.hasNext()) {
                val mapEntry = mapIterator.next()

                when (mapEntry.value) {
                    valor -> keyEncontrada = mapEntry.key //quando value = valor, iguala a key à keyEncontrada
                }
            }
        }

        if(ja.hasAnnotation == false) { //se nao tiver de omitir json devido a alguma anotação, escreve o json
            textoJson += "\"" + keyEncontrada + "\": " + ja.createJson() +",\n"
        }

        return true
    }

    override fun visitJsonVariable(vj: JsonVariable): Boolean { //escreve no JsonObject o texto json de acordo com as JsonVariable visitadas

        var keyEncontrada = "" //para encontrar o nome da variavel e escrever no json

        vj.objeto.children.forEach { //para encontrar o nome da variavel e escrever no json
            var valor = vj //value do elemento da lista lido

            val mapIterator = vj.objeto.children.iterator()

            while (mapIterator.hasNext()) {
                val mapEntry = mapIterator.next()

                when (mapEntry.value) {
                    valor -> keyEncontrada = mapEntry.key //quando value = valor, iguala a key à keyEncontrada
                }
            }
        }

        if(vj.hasAnnotation == false) { //se nao tiver de omitir json devido a alguma anotação, escreve o json
            textoJson += "\"" + keyEncontrada + "\": " + vj.converterValorEmJson() +",\n"
        }

        return true
    }

}

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