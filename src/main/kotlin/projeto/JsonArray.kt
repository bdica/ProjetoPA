package projeto

/**
 * Classe que representa um array
 * Filtra o tipo de valor recebido (list ou map) para saber que json criar
 *
 * @param valor variavel do objeto recebido
 * @param parent representa o elemento pai associado a esta classe
 */
class JsonArray(o: Any, parent: JsonObject?) : JsonElement(o) { //representa uma lista ou um map com uma map de variaveis (children)

    var objeto = parent
    var valorRecebido = o
    var hasAnnotation = false //para o caso de haver uma anotação

    var children = mutableMapOf<String, Any>() //lista de variaveis do array (texto json, valor)

    var elementsList = mutableListOf<JsonElement>()
    var nomeObjeto = ""

    fun createJson() { //vai a cada variavel da lista, obtem o seu json e coloca no Map (json da variavel, variavel)

        if(valorRecebido is List<*>) {
            (valorRecebido as List<*>).forEach {

                val oj = JsonObject(0) //no caso de não haver JsonObject (input não ser um objeto e ser apenas uma lista)
                var variavel = JsonVariable(it, oj)
                variavel.converterValorEmJson()

                if(objeto != null) { //se a lista vem de um JsonObject
                    variavel = JsonVariable(it, objeto as JsonObject)
                    variavel.converterValorEmJson()
                    variavel.fromArray = true
                    elementsList.add(variavel)
                }
            }
        }
        else if(valorRecebido is Map<*,*>) {
            (valorRecebido as Map<*, *>).forEach {

                var chave = it.key
                var valor = it.value
                data class Objeto(var valor: Any?)
                var o = Objeto(valor)

                if(objeto != null) { //se o map vem de um JsonObject
                    var variavel = JsonVariable(o, objeto as JsonObject)
                    variavel.fromArray = true

                    variavel.nomeChave = chave.toString()
                    nomeObjeto = variavel.nomeChave

                    variavel.converterValorEmJson()
                    elementsList.add(variavel)
                }
                else {
                    val oj = JsonObject(0)
                    var variavel = JsonVariable(o, oj)
                    variavel.fromArray = true

                    variavel.nomeChave = chave.toString()
                    nomeObjeto = variavel.nomeChave

                    variavel.converterValorEmJson()
                    elementsList.add(variavel)
                }
            }
        }
    }

    fun jsonTotal(): String { //vai a cada variavel da lista, obtem o seu json e coloca no Map (json da variavel, variavel)
        var valorRecebidoJson = "[\n"

        if(valorRecebido is List<*>) {
            (valorRecebido as List<*>).forEach {

                val oj = JsonObject(0) //no caso de receber objeto null
                var variavel = JsonVariable(it, oj)
                variavel.converterValorEmJson()

                if(objeto != null) {
                    variavel = JsonVariable(it, objeto as JsonObject)
                    variavel.converterValorEmJson()
                }

                var texto = variavel.converterValorEmJson()

                if(variavel.recebeuObjeto == true) { //remove os [ ] dos objetos recebidos
                    texto = texto.substring(2) //remove [
                    texto = texto.substring(0, texto.length - 2) //remove ]
                }

                children.put(texto, it as Any)
            }

            children.forEach {
                var textoVariavel = it.key
                valorRecebidoJson += textoVariavel + ",\n"
            }

            valorRecebidoJson = valorRecebidoJson.substring(0, valorRecebidoJson.length - 2)
            valorRecebidoJson += "\n]"
        }
        else if(valorRecebido is Map<*,*>) {
            var texto = ""

            (valorRecebido as Map<*, *>).forEach {

                if(it.value is Int || it.value is Boolean || it.value == null) {
                    texto += "{\n" + "\"" + it.key + "\": " + it.value + "\n},\n"
                }
                else if(it.value is Enum<*>) {
                    texto += "{\n" + "\"" + it.key + "\": " + "\"" + it.value.toString() + "\"" + "\n},\n"
                }
                else { //se for String
                    texto += "{\n" + "\"" + it.key + "\": " + "\"" + it.value + "\"" + "\n},\n"
                }
            }

            valorRecebidoJson += texto
            valorRecebidoJson = valorRecebidoJson.substring(0, valorRecebidoJson.length - 2)
            valorRecebidoJson += "\n]"
        }

        return valorRecebidoJson
    }

    override fun accept(v: Visitor) {
        if(valorRecebido is Map<*,*>) {
            if(v.visitJsonArray(this)) {
                elementsList.subList(elementsList.size/2, elementsList.size).clear() //remove duplicados da lista
                elementsList.forEach {
                    if(it is JsonVariable) {
                        nomeObjeto = it.nomeChave
                    }
                    it.accept(v)
                }
            }
            v.endVisitJsonArray(this)
        }
        else {
            if(v.visitJsonArray(this)) {
                elementsList.subList(elementsList.size/2, elementsList.size).clear() //remove duplicados da lista
                elementsList.forEach {
                    it.accept(v)
                }
            }
            v.endVisitJsonArray(this)
        }
    }
}