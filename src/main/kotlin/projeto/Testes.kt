package projeto

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Target(AnnotationTarget.FUNCTION)
annotation class Before()

fun countAllInts(o: Any): Int { //obter numero de Ints no objeto recebido
    var sum = 0

    val mj = MyJSON()
    val oj = JsonObject(o)
    oj.accept(mj)

    oj.children.forEach {
        if(it.value is JsonVariable) {
            if((it.value as JsonVariable).valorRecebido is Int) {
                sum++
            }
        }
    }

    return sum
}

fun getAllStrings(o: Any): String { //obter todas as variaveis que são do tipo String
    var json = ""

    val mj = MyJSON()
    val oj = JsonObject(o)
    oj.accept(mj)

    oj.children.forEach {
        if(it.value is JsonVariable) {
            if((it.value as JsonVariable).valorRecebido is String) {
                json += it.key
            }
        }
    }

    return json
}

fun printAllStrings(o: Any): String { //obter todos os valores das variaveis que são do tipo String
    var json = ""

    val mj = MyJSON()
    val oj = JsonObject(o)
    oj.accept(mj)

    oj.children.forEach {
        if(it.value is JsonVariable) {
            if((it.value as JsonVariable).valorRecebido is String) {
                json += (it.value as JsonVariable).converterValorEmJson()
            }
        }
    }

    return json
}

fun findVariable(): Boolean { //verifica se o JsonObject coloca as variaveis do objeto recebido no map children durante a reflexão

    data class Pessoa(var nome: String)
    var o = Pessoa("Bruno")

    val mj = MyJSON()
    val oj = JsonObject(o)
    oj.accept(mj)

    oj.children.forEach {
        if (it.key == "nome") {
            return true
        }
    }

    return false
}

fun generateJson(o: Any): Boolean { //verificar se criou json do objeto

    var jsonEsperado = """
        {
        "catalogo": [
        {
        "categoria": "Carro Usado",
        "nome": "Ford Focus",
        "preco": 30000,
        "vendido": false
        },
        {
        "categoria": "Carro Novo",
        "nome": "Ford Fiesta",
        "preco": 20000,
        "vendido": true
        }
        ],
        "id": 1,
        "nome": "Ford"
        }
    """.trimIndent()

    if(jsonEsperado == jsonGenerator(o)) {
        return true
    }

    return false
}

data class Produto(
    @HideJson
    val id: Int,
    val nome: String,
    val categoria: String,
    val preco: Int,
    val vendido: Boolean
)

data class Marca(
    val id: Int,
    val nome: String,
    val catalogo: List<Produto>
)

class Testes() {

    var variavelJogador = setUp()
    var variavelMarca = setUpMarca()

    @Before
    fun setUp(): Jogador {
        val p1 = Patrocinio("Nike", 2050)
        val p2 = Patrocinio("NOS", 2030)
        val p3 = Patrocinio("Macron", 2021)
        val patrocinios = mutableListOf(p2,p3)

        val c1 = Clube("Sporting CP", "Portugal", patrocinios)
        val c2 = Clube("Manchester United", "Inglaterra", null)
        val c3 = Clube("Real Madrid", "Espanha", null)
        val clubes = mutableListOf(c1,c2,c3)

        val trofeus = mutableMapOf<String, Any>()
        trofeus.put("Champions League", 5)
        trofeus.put("La Liga", 2)
        trofeus.put("Taça de França", "não tem")

        var variavelJogador = Jogador("Cristiano Ronaldo", 36, 7, Posicao.PL, false, p1, clubes, trofeus)

        return variavelJogador
    }

    @Before
    fun setUpMarca(): Marca {
        val p1 = Produto(1, "Ford Focus", "Carro Usado", 30000, false)
        val p2 = Produto(2, "Ford Fiesta", "Carro Novo", 20000, true)
        var produtos = mutableListOf(p1,p2)
        var m1 = Marca(1, "Ford", produtos)

        return m1
    }

    @Test
    fun contarInteiros() {
        assertEquals(2, countAllInts(variavelJogador),  "O número de Ints é diferente do inserido")
    }

    @Test
    fun procurarStrings() {
        assertEquals("nome", getAllStrings(variavelJogador),  "A String é diferente da inserida")
    }

    @Test
    fun imprimirStrings() {
        assertEquals("\"Cristiano Ronaldo\"", printAllStrings(variavelJogador),  "A String é diferente da inserida")
    }

    @Test
    fun procurarVariavel() {
        assertTrue(findVariable(), "Não encontrou a anotação")
    }

    @Test
    fun verificarJson() {
        assertTrue(generateJson(variavelMarca), "Não gerou o json correto")
    }

}

