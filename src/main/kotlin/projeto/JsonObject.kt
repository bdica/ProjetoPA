package projeto

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

/**
 * Classe que representa um objeto
 *
 * @param o objeto para gerar o json
 */
class JsonObject(o: Any) : Element(o) { //representa um objeto com um map de variaveis (children)

    var objetoRecebido = o

    val clazz: KClass<Any> = o::class as KClass<Any>

    var children = mutableMapOf<String, Element>() //lista de variaveis do objeto

    fun readObject() { //coloca as variaveis do objeto recebido na lista children (associando-as a uma JsonVariable)

        if(objetoRecebido is Int || objetoRecebido is Double || objetoRecebido is Enum<*>  || objetoRecebido is Boolean || objetoRecebido is String) { //se receber tipos primitivos
            val variavel = JsonVariable(objetoRecebido, this)

            val clazz: KClass<Any> = objetoRecebido::class as KClass<Any>
            var nomeObjeto = clazz.simpleName + ""

            variavel.converterValorEmJson()
            children.put(nomeObjeto, variavel)
        }

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