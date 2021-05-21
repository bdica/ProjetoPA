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
    val textBoxMaior: Boolean
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
    var jsonSelecionado: String = ""
    
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
                if(tree.selection.first().text == "(object)") {
                    println(jsonSelecionado) //selecionado
                    labelElements.text = "" + jsonSelecionado
                    elementoSelecionado!!.data = jsonSelecionado
                }
                else {
                    println(tree.selection.first().text) //selecionado
                    labelElements.text = "" + tree.selection.first().data
                }

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

    fun setIcons() {
        val iconePasta = Image(null, setup.folderIcon)
        val iconeFicheiro = Image(null, setup.fileIcon)

        tree.items.forEach {
            if(it.text == "(object)") {
                it.image = iconePasta
            }

            it.items.forEach {
                it.image = iconeFicheiro

                if(it.data.toString().startsWith("[")) { //no caso de objeto, lista ou map
                    it.image = iconePasta
                }
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
                        }
                        action.execute(this@JsonTreeSkeleton)
                    }
                })
            }
        }
        shell.pack()
    }

    fun open(o: Any) {

        shell.setSize(setup.width, setup.height)
        shell.text = setup.title
        shell.layout = setup.layoutManager

        val visitor = JsonTraverser(tree)

        if(o is List<*> || o is Map<*,*>) {
            var ja = JsonArray(o, null)
            jsonSelecionado = ja.createJson()
        }

        if(o is String) {
            val oj = JsonObject(o)
            oj.accept(visitor)
        }

        val oj = JsonObject(o)
        oj.accept(visitor)

        jsonSelecionado = visitor.textoJson

        setIcons()
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

class JsonTraverser(private val arvore: Tree): Visitor {

    var currentDirectory: TreeItem? = null //estrutura grafica
    var textoJson = "{" //texto final

    override fun visitJsonObject(oj: JsonObject): Boolean {

        println("**********VISITEI UM JsonObject**********")

        oj.readObject() //ao visitar um JsonObject, le as suas variaveis e coloca-as na lista children

        textoJson += "\n"

        val node: TreeItem = if (currentDirectory == null)
            TreeItem(arvore, SWT.NONE)
        else
            TreeItem(currentDirectory, SWT.NONE)
        node.text = "(object)" //valor que é apresentado na janela que se vê da arvore
        node.data = textoJson //valor a mostrar em json quando se clica num item da arvore
        currentDirectory = node

        return true
    }

    override fun endVisitJsonObject(oj: JsonObject) {

        println("**********SAI DE UM JsonObject**********")

        textoJson = textoJson.substring(0, textoJson.length - 2)
        textoJson += "\n}"

        currentDirectory = currentDirectory!!.parentItem //depois de visitar um objeto, volta ao diretorio anterior
    }

    override fun visitJsonVariable(vj: JsonVariable): Boolean {

        println("**********VISITEI UM JsonVariable**********")

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

            if(vj.objeto.objetoRecebido is Int || vj.objeto.objetoRecebido is Double) {
                keyEncontrada = "number"
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

            textoJson += "\"" + keyEncontrada + "\": " + vj.converterValorEmJson() + ",\n"

            val node = TreeItem(currentDirectory, SWT.NONE)
            node.data = vj.converterValorEmJson()

            if(vj.recebeuObjeto == true) {
                node.text = keyEncontrada
                //currentDirectory = node
            }
            else {
                node.text = "\"" + keyEncontrada + "\": " + vj.converterValorEmJson() + "\n"
            }

            //currentDirectory = node //cria nó para variavel e para objetos

        }

        return true
    }

    override fun endVisitJsonVariable(node: JsonVariable) {
        /*if(node.recebeuObjeto == true) {
            currentDirectory = currentDirectory!!.parentItem //se tiver num objeto, fecha o nó
        }*/

        //currentDirectory = currentDirectory!!.parentItem //fecha o nó da variavel
    }

    override fun visitJsonArray(ja: JsonArray): Boolean {

        println("**********VISITEI UM JsonArray**********")

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
            textoJson += "\"" + keyEncontrada + "\": " + ja.createJson() + ",\n"

            val node = TreeItem(currentDirectory, SWT.NONE)
            node.text = keyEncontrada
            node.data = ja.createJson()
            //currentDirectory = node //entra no array, abre um novo nó
        }

        return true
    }

    override fun endVisitJsonArray(node: JsonArray) {
        //currentDirectory = currentDirectory!!.parentItem //sai do array, volta ao nó anterior
    }
}