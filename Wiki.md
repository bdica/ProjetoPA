# Gerador de JSON

Biblioteca para para produzir dados no formato JSON.<br/>
Esta biblioteca permite criar JSON a partir de objetos, podendo o processo ser adaptado por via de anotações nas classes desses objetos..

# Tutorial

## 1. Modelo de Dados

Para utilizar o modelo de dados é necessário criar instanciar a classe ***JsonGenerator*** e utilizar uma das suas funções de acordo com o pretendido.

- Se o pretendido for derivar JSON a partir de um objeto deve-se usar a função ***jsonGenerator(o: Any?)*** da instância criada. É possível derivar JSON a partir de:
  <br/> >> Objeto, String, Int, Double, Enum, Boolean, Null, Listas ou Maps.
  
```kt
    var jg = JsonGenerator()
    println(jg.jsonGenerator(objeto))
  ```

- Se o pretendido for criar um ficheiro JSON a partir de um objeto deve-se usar a função ***fileGenerator(o: String)*** da instância criada, que recebe como argumento o texto JSON criado com a função anterior.
  
```kt
    var jg = JsonGenerator()
    jg.fileGenerator(jg.jsonGenerator(objeto))
  ```

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


