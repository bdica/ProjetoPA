package projeto

/**
 * Interface Visitor com funções genéricas
 */
interface Visitor { //interface com funções genéricas para serem implementadas
    fun visitJsonObject(node: JsonObject): Boolean
    fun visitJsonVariable(node: JsonVariable): Boolean
    fun visitJsonArray(node: JsonArray): Boolean
    fun endVisitJsonObject(node: JsonObject)
}

/**
 * Classe que implementa a interface Visitor
 * Realiza as visitas aos elementos json
 */
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

        if(ja.objeto != null) {
            ja.objeto!!.children.forEach { //para encontrar o nome da variavel e escrever no json
                var valor = ja //value do elemento da lista lido

                val mapIterator = ja.objeto!!.children.iterator()

                while (mapIterator.hasNext()) {
                    val mapEntry = mapIterator.next()

                    when (mapEntry.value) {
                        valor -> keyEncontrada = mapEntry.key //quando value = valor, iguala a key à keyEncontrada
                    }
                }
            }
        }

        if(ja.hasAnnotation == false) { //se nao tiver de omitir json devido a alguma anotação, escreve o json
            textoJson += "\"" + keyEncontrada + "\": " + ja.createJson() +",\n"
        }

        return true
    }

    override fun visitJsonVariable(vj: JsonVariable): Boolean { //escreve o texto json de acordo com as JsonVariable visitadas

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

            if(vj.objeto.objetoRecebido is Int || vj.objeto.objetoRecebido is Double) { //no caso de receber um tipo primitivo
                keyEncontrada = "number"
            }
            else if(vj.objeto.objetoRecebido is Boolean) {
                keyEncontrada = "boolean"
            }
            else if(vj.objeto.objetoRecebido is String) {
                keyEncontrada = "string"
            }
            else if(vj.objeto.objetoRecebido is Enum<*>) {
                keyEncontrada = "string"
            }

            textoJson += "\"" + keyEncontrada + "\": " + vj.converterValorEmJson() +",\n"

        }

        return true
    }

}

