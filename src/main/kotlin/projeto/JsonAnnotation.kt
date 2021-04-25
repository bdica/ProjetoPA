package projeto

/**
 * Esta anotação permite omitir uma propriedade de um objeto
 */
@Target(AnnotationTarget.PROPERTY)
annotation class HideJson //omite o texto json da variavel

/**
 * Esta anotação permite alterar o nome de uma propriedade de um objeto
 *
 * @param name novo nome para a propriedade
 */
@Target(AnnotationTarget.PROPERTY) //altera o nome da variavel no texto json
annotation class RenameProperty(val name: String)