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

    var elementsList = mutableListOf<JsonElement>()
    var nomeObjeto = ""

    var naoTemJsonObject = false

    fun createJson() { //vai a cada variavel da lista, obtem o seu json e coloca no Map (json da variavel, variavel)

        if(valorRecebido is List<*>) {
            (valorRecebido as List<*>).forEach {

                if(objeto == null) {
                    if ((it !is List<*> && it !is Map<*, *>) && (it is Int || it is Double || it is Enum<*> || it is Boolean || it is String)) {
                        val oj = JsonObject(0) //no caso de não haver JsonObject (input não ser um objeto e ser apenas uma lista)
                        var variavel = JsonVariable(it, oj)
                        if(hasAnnotation == true) {
                            variavel.hasAnnotation = true
                        }
                        variavel.converterValorEmJson()
                        variavel.fromArray = true
                        elementsList.add(variavel)
                    }
                    if(it !is Int && it !is Double && it !is Enum<*>  && it !is Boolean && it !is String) {
                        var objeto = JsonObject(it as Any)
                        if(hasAnnotation == true) {
                            objeto.hasAnnotation = true
                        }
                        elementsList.add(objeto)
                    }
                }

                if(objeto != null) { //se a lista vem de um JsonObject
                    if((it !is List<*> && it !is Map<*, *>) && (it is Int || it is Double || it is Enum<*>  || it is Boolean || it is String)) {
                        var variavel = JsonVariable(it, objeto as JsonObject)
                        if(hasAnnotation == true) {
                            variavel.hasAnnotation = true
                        }
                        variavel.converterValorEmJson()
                        variavel.fromArray = true
                        elementsList.add(variavel)
                    }
                    if(it !is Int && it !is Double && it !is Enum<*>  && it !is Boolean && it !is String) {
                        var objeto = JsonObject(it as Any)
                        if(hasAnnotation == true) {
                            objeto.hasAnnotation = true
                        }
                        elementsList.add(objeto)
                    }
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
                    var variavel = JsonObject(o)

                    if(hasAnnotation == true) {
                        variavel.hasAnnotation = true
                    }

                    variavel.nomeChave = chave.toString()
                    nomeObjeto = variavel.nomeChave

                    elementsList.add(variavel)
                }
                else {
                    var variavel = JsonObject(o)

                    if(hasAnnotation == true) {
                        variavel.hasAnnotation = true
                    }

                    variavel.nomeChave = chave.toString()
                    nomeObjeto = variavel.nomeChave

                    elementsList.add(variavel)
                }
            }
        }
    }

    fun obterJsonGerado(): String {
        var texto = ""

        if(hasAnnotation == false) {
            if (naoTemJsonObject == true) { //se objeto recebido do utilizador for apenas uma lista
                var jo = JsonObject(0)
                var ja = JsonArray(valorRecebido, jo)
                ja.createJson()
                texto += ja.obterJsonGerado()
                return texto
            }

            if (valorRecebido !is Map<*, *>) {
                elementsList.forEach {
                    if (it is JsonObject) {
                        texto += it.obterJsonGerado() + ",\n"
                    }
                }
                return texto.dropLast(2) + "\n]"
            } else {
                (valorRecebido as Map<*, *>).forEach {
                    texto += "{\n" + "\"" + it.key + "\": " + it.value + "\n},\n"
                }
                return texto.dropLast(2) + "\n]"
            }
        }
        else {
            return texto
        }
    }

    override fun accept(v: Visitor) {
        if(valorRecebido is Map<*,*>) {
            if(v.visitJsonArray(this)) {
                elementsList.subList(elementsList.size/2, elementsList.size).clear() //remove duplicados da lista
                elementsList.forEach {
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