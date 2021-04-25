package projeto

//Filtra o tipo de valor recebido para saber que print fazer
class JsonVariable(valor: Any?, parent: JsonObject): Element(parent) { //representa uma variavel

    var objeto = parent
    var valorRecebido = valor
    var hasAnnotation = false //para o caso de haver uma anotação
    var recebeuObjeto = false //se recebeu objeto retira os [ ]

    override fun accept(v: Visitor) { //escreve o texto json da variavel
        v.visitJsonVariable(this) //this é a propria JsonVariable
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
        else if(valorRecebido == null) {
            return "" + valorRecebido
        }
        else  { //se valorRecebido for um objeto(valorRecebido) cria um JsonObject(valorRecebido) e cria o Json desse objeto
            recebeuObjeto = true

            val obj = JsonObject(valorRecebido as Any)

            val mj = MyJSON()
            obj.accept(mj)

            return "[\n" + mj.textoJson + "\n]"
        }

    }
}