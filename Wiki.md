# Gerador de JSON

Biblioteca para para produzir dados no formato JSON.<br/>
Esta biblioteca permite criar JSON a partir de objetos, podendo o processo ser adaptado por via de anotações nas classes desses objetos.

# Tutorial

## 1. Modelo de Dados

Para utilizar o modelo de dados é necessário instanciar a classe ***JsonGenerator*** e utilizar uma das suas funções de acordo com o pretendido.

- Se o pretendido for derivar JSON a partir de um objeto deve-se usar a função ***jsonGenerator(o: Any?)*** da instância criada. É possível derivar JSON a partir de:
    - Objeto
    - String
    - Int
    - Double
    - Enum
    - Boolean
    - Null
    - Listas
    - Maps
  
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

Como forma de adaptar a instanciação, existem duas anotações disponíveis para serem utilizadas nas classes dos objetos recebidos.

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

## 2. Visualizador

Para utilizar o visualizador JSON é necessário instanciar a classe ***JsonTreeSkeleton*** através da classe ***JsonInjector***.
A classe ***JsonInjector*** representa uma framework de injeção de dependência e neste caso recebe um pedido para criar um objeto da classe ***JsonTreeSkeleton***.<br/>
Após isto tem de se invocar a função ***open(o: Any?)*** da classe ***JsonTreeSkeleton***, que recebe como parâmetro o objeto pretendido para a criação de JSON.

```kt
    val w = JsonInjector.create(JsonTreeSkeleton::class)
    w.open(objeto)
```

### Plugins

É possível extender o visualizador através de plugins para personalizar a visualização e acrescentar ações.<br/>
Para criar um plugin é necessário modificar o ficheiro di.properties que se encontra na diretoria [src](https://github.com/bdica/ProjetoPA/tree/master/src) do projeto.


