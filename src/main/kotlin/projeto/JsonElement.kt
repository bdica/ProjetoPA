package projeto

/**
 * Classe abstrata que representa um elemento json
 *
 * @param value valor recebido para a criação de json
 */
abstract class JsonElement(value: Any) {
    abstract fun accept(v: Visitor)
}