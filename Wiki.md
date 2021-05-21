# Gerador de JSON

Biblioteca para para produzir dados no formato JSON.<br/>
Esta biblioteca permite criar JSON a partir de objetos, podendo o processo ser adaptado por via de anotações nas classes desses objetos..

# Tutorial

## 1. Modelo de Dados

...

### Annotações

@HideJson:<br/>
Esta anotação permite omitir uma propriedade de um objeto

```kt
    @HideJson
    val numero: Int
```

@RenameProperty:<br/>
Esta anotação recebe uma string como parâmetro e altera o nome de uma propriedade de um objeto para a string recebida

```kt
    @RenameProperty("retirado")
    val reformado: Boolean
```

## 2. Plugins para o Visualizador

...


