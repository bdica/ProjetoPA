package projeto.janela

class Folder(
    val name: String,
    val children: MutableList<Folder>?
)

fun main() {
    val f4 = Folder("Testes2.kt", null)
    val f3 = Folder("Testes1.kt", null)
    val f2 = Folder("testes", mutableListOf(f3, f4))
    val f1 = Folder("Main.kt", null)
    val f = Folder("src", mutableListOf(f1, f2))

    val w = JsonInjector.create(JsonTreeSkeleton::class)
    w.open(f)
}