package projeto

/**
 * Interface Visitor com funções genéricas
 */
interface Visitor { //interface com funções genéricas para serem implementadas
    fun visitJsonObject(node: JsonObject): Boolean
    fun visitJsonVariable(node: JsonVariable): Boolean
    fun visitJsonArray(node: JsonArray): Boolean
    fun endVisitJsonObject(node: JsonObject)
    fun endVisitJsonVariable(node: JsonVariable)
    fun endVisitJsonArray(node: JsonArray)
}

/**
 * Classe que implementa a interface Visitor
 * Realiza as visitas aos elementos json
 */
class MyJSON: Visitor {

    var textoJson = "{" //texto final

    override fun visitJsonObject(oj: JsonObject): Boolean {

        oj.readObject() //ao visitar um JsonObject, le as suas variaveis e coloca-as na lista children

        if(oj.hasAnnotation == false) { //se nao tiver de omitir json devido a alguma anotação, escreve o json

            if(oj.nomeChave != "") { //caso objeto venha de um map
                textoJson += "{\n" + "\"" + oj.nomeChave + "\": "
            }
            else {
                if(oj.recebeuNull == true && oj.objetoRecebido == 0) { //caso do valor recebido no JsonObject ser null
                    if(oj.nome != "") {
                        textoJson += "\"" + oj.nome + "\": " + ""
                    }
                    else {
                        textoJson += ""
                    }
                }
                else {
                    if(oj.nome != "") {
                        textoJson += "\"" + oj.nome + "\": " + "{\n"
                    }
                    else {
                        textoJson += "{\n"
                    }
                }
            }
        }

        return true
    }

    override fun endVisitJsonObject(node: JsonObject) {
        if(node.hasAnnotation == false) {
            textoJson = textoJson.substring(0, textoJson.length - 2)

            if (node.recebeuNull == true && node.objetoRecebido == 0) {
                textoJson += ",\n"
            } else {
                textoJson += "\n},\n"
            }
        }
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
                if(vj.objeto.recebeuNull == true && vj.objeto.objetoRecebido == 0) { //caso o JsonObject receba um null
                    keyEncontrada = ""
                }
                else {
                    keyEncontrada = "number"
                }
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

            if(vj.fromArray == true) {
                textoJson += vj.converterValorEmJson() +",\n"
            }
            else {
                var recebido = "\"" + keyEncontrada + "\": " + vj.converterValorEmJson() +",\n"
                recebido = recebido.replace("\"valor\": ", "")
                recebido = recebido.replace("\n}", "")

                if(vj.objeto.recebeuNull == true && vj.objeto.objetoRecebido == 0) { //caso o JsonObject receba um null
                    var recebido = "\"" + keyEncontrada + "\": " + "null" +",\n"
                    recebido = recebido.replace("\"\": ", "")
                    textoJson += recebido
                }
                else {
                    textoJson += recebido
                }

            }
        }

        return true
    }

    override fun endVisitJsonVariable(node: JsonVariable) {
        //
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
            textoJson += "\"" + keyEncontrada + "\": " + "[ \n"
            ja.createJson()
        }

        return true
    }

    override fun endVisitJsonArray(node: JsonArray) {
        if(node.hasAnnotation == false) {
            textoJson = textoJson.substring(0, textoJson.length - 2)
            textoJson += "\n],\n"
        }
    }

}

