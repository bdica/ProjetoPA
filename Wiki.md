# Gerador de JSON

Biblioteca para para produzir dados no formato JSON.
Esta biblioteca permite criar JSON a partir de objetos, podendo o processo ser adaptado por via de anotações nas classes desses objetos..

## Tutorial

...

## Annotações

@HideJson: Esta anotação permite omitir uma propriedade de um objeto

```kt
    @HideJson
    val numero: Int
```

@RenameProperty: Esta anotação recebe uma string como parâmetro e altera o nome de uma propriedade de um objeto para a string recebida

```kt
    @RenameProperty("retirado")
    val reformado: Boolean
```
