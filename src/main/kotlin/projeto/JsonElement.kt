package projeto

abstract class Element(value: Any) {
    abstract fun accept(v: Visitor)
}