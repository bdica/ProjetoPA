package projeto

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

@Target(AnnotationTarget.PROPERTY)
annotation class HideJson //omite o texto json da variavel

@Target(AnnotationTarget.PROPERTY) //altera o nome da variavel no texto json
annotation class RenameProperty(val name: String)

interface Visitor { //interface com funções genéricas para serem implementadas
    fun visitJsonObject(node: JsonObject): Boolean
    fun visitJsonVariable(node: JsonVariable): Boolean
    fun visitJsonArray(node: JsonArray): Boolean
    fun endVisitJsonObject(node: JsonObject)
}

abstract class Element(value: Any) {
    abstract fun accept(v: Visitor)
}

class JsonObject(o: Any) : Element(o) { //representa um objeto com um map de variaveis (children)

    var objetoRecebido = o

    val clazz: KClass<Any> = o::class as KClass<Any>

    var children = mutableMapOf<String, Element>() //lista de variaveis do objeto

    fun readObject() { //coloca as variaveis do objeto recebido na lista children (associando-as a uma JsonVariable)

        clazz.declaredMemberProperties.forEach {

            var nomeVariavel = it.name
            val valorVariavel = it.call(objetoRecebido)

            if(valorVariavel !is List<*> && valorVariavel !is Map<*, *>) {
                val variavel = JsonVariable(valorVariavel, this)

                val hasAnnotation = it.hasAnnotation<HideJson>() //verifica se a variavel recebida tem anotação

                if(hasAnnotation == true) { //se variavel recebida tem anotação, informa a JsonVariable criada
                    variavel.hasAnnotation = true
                }

                val hasRenameAnnotation = it.hasAnnotation<RenameProperty>()

                if(hasRenameAnnotation == true) {
                    val table = it.annotations.find { it is RenameProperty } as? RenameProperty
                    val annotationName = "" + table!!.name //nome da variavel a remover
                    nomeVariavel = annotationName
                }

                variavel.converterValorEmJson()

                children.put(nomeVariavel, variavel)
            }
            else if(valorVariavel is List<*> || valorVariavel is Map<*,*>) { //se objeto recebido tiver uma variavel lista nao cria JsonVariable mas sim JsonArray
                val variavelArray = JsonArray(valorVariavel, this)

                val hasAnnotation = it.hasAnnotation<HideJson>() //verifica se a variavel recebida tem anotação

                if(hasAnnotation == true) { //se variavel recebida tem anotação, informa a JsonVariable criada
                    variavelArray.hasAnnotation = true
                }

                val hasRenameAnnotation = it.hasAnnotation<RenameProperty>()

                if(hasRenameAnnotation == true) {
                    val table = it.annotations.find { it is RenameProperty } as? RenameProperty
                    val annotationName = "" + table!!.name //nome da variavel a remover
                    nomeVariavel = annotationName
                }

                variavelArray.createJson()

                children.put(nomeVariavel, variavelArray)
            }
        }
    }

    override fun accept(v: Visitor) { //itera os elementos da lista children
        if (v.visitJsonObject(this)) {
            children.forEach {
                it.value.accept(v)
            }
        }
        v.endVisitJsonObject(this)
    }
}

class JsonArray(o: Any, parent: JsonObject) : Element(o) { //representa uma lista ou um map com uma map de variaveis (children)

    var objeto = parent
    var valorRecebido = o
    var hasAnnotation = false //para o caso de haver uma anotação

    var children = mutableMapOf<String, Any>() //lista de variaveis do array (texto json, valor)

    fun createJson(): String { //vai a cada variavel da lista, obtem o seu json e coloca no Map (json da variavel, variavel)

        var valorRecebidoJson = "[\n"

        if(valorRecebido is List<*>) {
            (valorRecebido as List<*>).forEach {

                val variavel = JsonVariable(it, objeto)
                variavel.converterValorEmJson()

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

    //vai a cada variavel da lista, cria um objeto com a variavel e obtem a string dos objetos
    override fun accept(v: Visitor) { //itera os elementos da lista
        v.visitJsonArray(this)
    }
}

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