package projeto

/**
 * Classe que representa uma variável
 * Filtra o tipo de valor recebido para saber que json criar
 *
 * @param valor variavel do objeto recebido
 * @param parent representa o elemento pai associado a esta classe
 */
class JsonVariable(valor: Any?, parent: JsonObject): JsonElement(parent) { //representa uma variavel

    var objeto = parent
    var valorRecebido = valor
    var hasAnnotation = false //para o caso de haver uma anotação
    var fromArray = false //se veio de um array e for um objeto, nao coloca os [ ]

    override fun accept(v: Visitor) { //escreve o texto json da variavel
        v.visitJsonVariable(this) //this é a propria JsonVariable
        v.endVisitJsonVariable(this)
    }

    fun converterValorEmJson(): String {

        if(hasAnnotation == true) { //se objeto recebido contem a anotação, nao cria o texto json
            return ""
        }

        if(valorRecebido is String) {
            return "\"" + valorRecebido + "\""
        }
        else if(valorRecebido is Int || valorRecebido is Double) {
            return "" + valorRecebido
        }
        else if(valorRecebido is Boolean) {
            return "" + valorRecebido
        }
        else if(valorRecebido is Enum<*>) {
            return "\"" + valorRecebido.toString() + "\""
        }
        else { //if(valorRecebido == null)
            return "" + valorRecebido
        }

    }
}