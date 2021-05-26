package projeto

data class Jogador(
    val nome: String,
    val idade: Int,
    @HideJson
    val numero: Int,
    val posicao: Posicao, //enumerado
    @RenameProperty("retirado")
    val reformado: Boolean,
    val patrocinio: Patrocinio, //objeto
    val clubes: List<Clube>, //lista
    val trofeus: Map<String, Any> //map
)

enum class Posicao {
    GR, DE, DD, MC, MCE, MCD, MO, AE, AD, PL
}

data class Patrocinio(
    val nome: String,
    val validade: Int
)

data class Clube(
    val nome: String,
    val pais: String,
    val patrocinios: List<Patrocinio>? //lista ou null
)

fun main() {

    val p1 = Patrocinio("Nike", 2050)
    val p2 = Patrocinio("NOS", 2030)
    val p3 = Patrocinio("Macron", 2021)
    val patrocinios = mutableListOf(p2,p3)

    val c1 = Clube("Sporting CP", "Portugal", patrocinios)
    val c2 = Clube("Manchester United", "Inglaterra", null) //null
    val c3 = Clube("Real Madrid", "Espanha", null) //null
    val clubes = mutableListOf(c1,c2,c3)

    val trofeus = mutableMapOf<String, Any>()
    trofeus.put("Champions League", 5)
    trofeus.put("La Liga", 2)
    trofeus.put("Taça de França", "não tem")

    val variavelJogador = Jogador("Cristiano Ronaldo", 36, 7, Posicao.PL, false, p1, clubes, trofeus)

    //println(jg.jsonGenerator(patrocinios))
    //println(jg.jsonGenerator(trofeus))
    //println(jg.jsonGenerator("teste"))
    //println(jg.jsonGenerator(null))

    var jg = JsonGenerator()
    println(jg.jsonGenerator(variavelJogador))
    jg.fileGenerator(jg.jsonGenerator(variavelJogador))

}
