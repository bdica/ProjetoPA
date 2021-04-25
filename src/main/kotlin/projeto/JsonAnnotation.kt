package projeto

@Target(AnnotationTarget.PROPERTY)
annotation class HideJson //omite o texto json da variavel

@Target(AnnotationTarget.PROPERTY) //altera o nome da variavel no texto json
annotation class RenameProperty(val name: String)