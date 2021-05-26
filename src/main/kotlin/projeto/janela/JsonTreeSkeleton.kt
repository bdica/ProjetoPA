package projeto.janela

import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import projeto.*

interface JsonFrameSetup {
    val title: String
    val layoutManager: GridLayout
    val width: Int
    val height: Int
    val folderIcon: String
    val fileIcon: String
}

interface JsonAction {
    val name: String
    val textBox: Boolean
    var input: String
    fun execute(window: JsonTreeSkeleton)
    fun undo(window: JsonTreeSkeleton)
}

class JsonTreeSkeleton() {

    @Inject
    private lateinit var setup: JsonFrameSetup

    @InjectAdd
    private lateinit var actions: MutableList<JsonAction>

    val shell: Shell = Shell(Display.getDefault())
    val tree: Tree
    var jsonGerado: String = ""
    
    var elementosMarcados = mutableListOf<TreeItem?>()
    var elementoSelecionado: TreeItem? = null

    var executedActions = mutableListOf<JsonAction>() //para o undo

    init {
        tree = Tree(shell, SWT.SINGLE or SWT.BORDER) //estrutura dos elementos

        //lado esquerdo da janela
        val labelElements = Label(shell, SWT.NONE)

        tree.addSelectionListener(object : SelectionAdapter() { //ao clicar num objeto tem de abrir o json
            override fun widgetSelected(e: SelectionEvent) {
                elementoSelecionado = tree.selection.first()

                //apresentação dos elementos no lado direito ao clicar nos objetos
                tree.getItem(0).data = jsonGerado //json completo do objeto
                println(tree.selection.first().text) //selecionado
                labelElements.text = "" + tree.selection.first().data //texto json na janela da direita

                labelElements.requestLayout()
            }
        })

        //analisar elementos com duplo clique
        tree.addListener(SWT.MouseDoubleClick) {
            read()
        }

        //parte da pesquisa
        val searchText = Text(shell, SWT.SINGLE or SWT.BORDER)

        val button = Button(shell, SWT.PUSH)
        button.text = "Search"
        button.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                val keyword = searchText.text
                if (elementosMarcados.isNotEmpty()) { //se tiver algum elemento pintado

                    elementosMarcados.forEach {
                        it!!.background = Color(Display.getCurrent(), 255, 255, 255)
                    }
                    elementosMarcados = mutableListOf<TreeItem?>()
                }
                if (keyword != "") {
                    val root: TreeItem = tree.getItem(0) //item está contido no primeiro nó (objeto principal)
                    pesquisa(root, keyword)
                    searchText.text = "" //volta a meter a caixa de texto vazia
                }
            }
        })

    }

    fun pesquisa(node: TreeItem, searchText: String) {
        node.items.forEach {
            if (it.text?.toString()?.toUpperCase()?.contains(searchText.toUpperCase()) == true) {
                it.background = Color(Display.getCurrent(), 0, 255, 0)
                elementosMarcados.add(it)
            }
        }
    }

    fun setActions() {
        actions.forEach { action ->

            var keywordText: Text? = null

            if (action.textBox == true) {
                keywordText = Text(shell, SWT.SINGLE or SWT.BORDER)
            }

            if(action.name != "") { //so cria botão se na ação houver texto para o botão
                val button = Button(shell, SWT.PUSH)
                button.text = action.name

                button.addSelectionListener(object : SelectionAdapter() {
                    override fun widgetSelected(e: SelectionEvent?) {
                        super.widgetSelected(e)
                        if (keywordText != null) {
                            action.input = keywordText.text
                            keywordText.text = "" //volta a meter a caixa de texto vazia
                        }
                        action.execute(this@JsonTreeSkeleton)
                    }
                })
            }
        }
        shell.pack()
    }

    fun open(o: Any?) {

        shell.setSize(setup.width, setup.height)
        shell.text = setup.title
        shell.layout = setup.layoutManager

        val visitor = JsonTraverser(tree, setup)

        if(o is List<*> || o is Map<*,*>) {
            val ja = JsonArray(o, null)
            ja.naoTemJsonObject = true
            jsonGerado = "[\n" + ja.obterJsonGerado()
        }

        if(o is String) {
            val oj = JsonObject(o)
            oj.accept(visitor)
            jsonGerado = visitor.textoJson.substringBefore(",") + "\n}"
        }

        if(o == null) {
            val oj = JsonObject(0)
            oj.recebeuNull = true
            oj.objetoRecebido = 0
            oj.accept(visitor)
            jsonGerado = visitor.textoJson.substring(0, visitor.textoJson.length - 2).substring(1)
        }

        val oj = JsonObject(o)
        oj.accept(visitor)
        jsonGerado = visitor.textoJson.substring(0, visitor.textoJson.length - 2).substring(1)

        setActions()

        tree.expandAll()
        shell.pack()
        shell.open()

        val display = Display.getDefault()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        display.dispose()
    }

    fun edit(name: String) {
        if (elementoSelecionado != null) {
            if(name != "") {
                elementoSelecionado!!.text = name //data
            }
        }
    }

    fun read() { //clicar duas vezes num elemento para abrir
        val shell = Shell(Display.getDefault())
        shell.setSize(250, 200)
        shell.text = "Analisar elemento"
        shell.layout = GridLayout(2,true)

        val tree = Tree(shell, SWT.SINGLE or SWT.BORDER)
        var node = TreeItem(tree, SWT.NONE)
        node.text = elementoSelecionado!!.text //lado esquerdo da janela

        val labelElements = Label(shell, SWT.NONE) //lado direito da janela
        labelElements.text = elementoSelecionado!!.data.toString()
        labelElements.requestLayout()

        tree.expandAll()
        shell.pack()
        shell.open()
    }

    fun undo() {
        if(executedActions.size > 0) { //tem de haver ações já feitas (guardadas em actions)
            executedActions.removeAt(executedActions.size - 1).undo(this)
        }
    }

    //Auxiliares para varrer a árvore
    fun Tree.expandAll() = traverse { it.expanded = true }

    fun Tree.traverse(visitor: (TreeItem) -> Unit) {
        fun TreeItem.traverse() {

            visitor(this)

            items.forEach {
                it.traverse()
            }
        }
        items.forEach { it.traverse() }
    }

}

class JsonTraverser(private val arvore: Tree, setup: JsonFrameSetup): Visitor {

    var currentDirectory: TreeItem? = null //estrutura grafica
    var textoJson = "{" //texto final

    var config = setup
    val iconePasta = Image(null, config.folderIcon)
    val iconeFicheiro = Image(null, config.fileIcon)

    override fun visitJsonObject(oj: JsonObject): Boolean {

        oj.readObject() //ao visitar um JsonObject, le as suas variaveis e coloca-as na lista children

        if(oj.hasAnnotation == false) { //se nao tiver de omitir json devido a alguma anotação, escreve o json

            val node: TreeItem = if (currentDirectory == null)
                TreeItem(arvore, SWT.NONE)
            else
                TreeItem(currentDirectory, SWT.NONE)

            if(oj.nome == "") {
                node.text = "(object)" //valor que é apresentado na janela que se vê da arvore
            }
            else {
                node.text = oj.nome
            }

            if(oj.nomeChave != "") { //caso objeto venha de um map
                textoJson += "{\n" + "\"" + oj.nomeChave + "\": "
            }
            else {
                if(oj.recebeuNull == true && oj.objetoRecebido == 0) { //caso do valor recebido no JsonObject ser null
                    if(oj.nome != "") {
                        textoJson += "\"" + oj.nome + "\": " + ""
                    }
                    else {
                        textoJson += ""
                    }
                }
                else {
                    if(oj.nome != "") {
                        textoJson += "\"" + oj.nome + "\": " + "{\n"
                    }
                    else {
                        textoJson += "{\n"
                    }
                }
            }

            if (oj.recebeuNull == true && oj.objetoRecebido == 0) {
                node.data = "null"
            }
            else {
                node.data = oj.obterJsonGerado()
            }

            if(oj.nomeChave != "") { //no caso de ser objeto de um map
                node.data = "{\n" + "\"" + oj.nomeChave + "\": " + oj.objetoRecebido.toString()
                    .substring( oj.objetoRecebido.toString().indexOf("=")+1).dropLast(1) + "\n}"
            }

            currentDirectory = node
            currentDirectory!!.setImage(0, iconePasta)

        }

        return true
    }

    override fun endVisitJsonObject(oj: JsonObject) {
        if(oj.hasAnnotation == false) {
            textoJson = textoJson.substring(0, textoJson.length - 2)

            if (oj.recebeuNull == true && oj.objetoRecebido == 0) {
                textoJson += ",\n"
            } else {
                textoJson += "\n},\n"
            }

            currentDirectory = currentDirectory!!.parentItem //depois de visitar um objeto, volta ao diretorio anterior
        }
    }

    override fun visitJsonVariable(vj: JsonVariable): Boolean {

        var keyEncontrada = ""

        vj.objeto.children.forEach {
            var valor = vj

            val mapIterator = vj.objeto.children.iterator()

            while (mapIterator.hasNext()) {
                val mapEntry = mapIterator.next()

                when (mapEntry.value) {
                    valor -> keyEncontrada = mapEntry.key
                }
            }
        }

        if(vj.hasAnnotation == false) {

            if(vj.objeto.objetoRecebido is Int || vj.objeto.objetoRecebido is Double) { //no caso de receber um tipo primitivo
                if(vj.objeto.recebeuNull == true && vj.objeto.objetoRecebido == 0) { //caso o JsonObject receba um null
                    keyEncontrada = ""
                }
                else {
                    keyEncontrada = "number"
                }
            }
            else if(vj.objeto.objetoRecebido is Boolean) {
                keyEncontrada = "boolean"
            }
            else if(vj.objeto.objetoRecebido is String) {
                keyEncontrada = "string"
            }
            else if(vj.objeto.objetoRecebido is Enum<*>) {
                keyEncontrada = "string"
            }

            if(vj.fromArray == true) {
                textoJson += vj.converterValorEmJson() +",\n"

                var node = TreeItem(currentDirectory, SWT.NONE)
                node.data = vj.converterValorEmJson() + "\n"
                node.setImage(0, iconeFicheiro)
                node.text = vj.converterValorEmJson() +"\n"
            }
            else {
                var recebido = "\"" + keyEncontrada + "\": " + vj.converterValorEmJson() +",\n"
                recebido = recebido.replace("\"valor\": ", "")
                recebido = recebido.replace("\n}", "")

                if(vj.objeto.recebeuNull == true && vj.objeto.objetoRecebido == 0) { //caso o JsonObject receba um null
                    var recebido = "\"" + keyEncontrada + "\": " + "null" +",\n"
                    recebido = recebido.replace("\"\": ", "")
                    textoJson += recebido
                }
                else {
                    textoJson += recebido

                    var node = TreeItem(currentDirectory, SWT.NONE)
                    node.data = vj.converterValorEmJson() + "\n"
                    node.setImage(0, iconeFicheiro)
                    node.text = keyEncontrada

                    if(keyEncontrada == "valor") { //no caso de ser objeto de um map
                        if(vj.objeto.nomeChave != "") {
                            node.text = vj.objeto.nomeChave
                        }
                    }

                }

            }

        }

        return true
    }

    override fun endVisitJsonVariable(node: JsonVariable) {
        //
    }

    override fun visitJsonArray(ja: JsonArray): Boolean {

        var keyEncontrada = "" //para encontrar o nome da variavel e escrever no json

        if(ja.objeto != null) {
            ja.objeto!!.children.forEach { //para encontrar o nome da variavel e escrever no json
                var valor = ja //value do elemento da lista lido

                val mapIterator = ja.objeto!!.children.iterator()

                while (mapIterator.hasNext()) {
                    val mapEntry = mapIterator.next()

                    when (mapEntry.value) {
                        valor -> keyEncontrada = mapEntry.key //quando value = valor, iguala a key à keyEncontrada
                    }
                }
            }
        }

        if(ja.hasAnnotation == false) { //se nao tiver de omitir json devido a alguma anotação, escreve o json

            textoJson += "\"" + keyEncontrada + "\": " + "[ \n"

            val node = TreeItem(currentDirectory, SWT.NONE)
            node.text = keyEncontrada
            node.data = "[\n" + ja.obterJsonGerado() //texto completo do array

            ja.createJson()

            currentDirectory = node
            currentDirectory!!.setImage(0, iconePasta)

        }

        return true
    }

    override fun endVisitJsonArray(node: JsonArray) {
        if(node.hasAnnotation == false) {
            textoJson = textoJson.substring(0, textoJson.length - 2)
            textoJson += "\n],\n"
        }

        currentDirectory = currentDirectory!!.parentItem //sai do array, volta ao nó anterior

    }
}