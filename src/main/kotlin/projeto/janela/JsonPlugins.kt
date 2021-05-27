package projeto.janela

import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.TreeItem
import projeto.JsonGenerator

//Apresentação

class DefaultSetup : JsonFrameSetup {
    override val title: String
        get() = "Visualizador de JSON"
    override val layoutManager: GridLayout
        get() = GridLayout(2, true)
    override val width: Int
        get() = 450
    override val height: Int
        get() = 400
    override val folderIcon: String
        get() = "folder_icon.png"
    override val fileIcon: String
        get() = "file_icon.png"
    override val firstNodeName: String
        get() = "Jogador"

    override fun setIcons(node: TreeItem, display: Display) {
        val iconePasta = Image(display, folderIcon)
        val iconeFicheiro = Image(display, fileIcon)

        node.items.forEach {
            if (it.data.toString().startsWith("[") || it.data.toString().startsWith("{")) {
                it.parentItem.image = iconePasta //se é um objeto ou uma lista, o seu parent tambem tem de ser um objeto ou uma lista
                it.image = iconePasta
                setIcons(it, display)
            }
            else {
                it.image = iconeFicheiro
                setIcons(it, display)
            }
            setIcons(it, display)
        }
    }

    override fun addText(node: TreeItem, display: Display) {
        node.items.forEach {
            if (it.data.toString().startsWith("[")) { //altera o nome das listas
                it.text = "children"
                addText(it, display)
            }
            else {
                addText(it, display)
            }
            addText(it, display)
        }
    }
}

//Ações

class Edit : JsonAction { //textoBoxMaior vai conter texto do lado direito deo ecrã, ao editar esse texto vai editar o json total que pode ser gerado para ficheiro de forma diferente
    var textoInicial = ""

    override val name: String
        get() = "Editar" //texto do botão

    override val textBox: Boolean
        get() = true

    override var input: String = ""

    override fun execute(window: JsonTreeSkeleton) {
        textoInicial = window.elementoSelecionado!!.text //coloca no textoInicial o texto do elemento selecionado

        window.edit(input)
        window.executedActions.add(this)
    }
    override fun undo(window: JsonTreeSkeleton) {
        window.edit(textoInicial)
    }
}

class GenerateFile : JsonAction {
    override val name: String
        get() = "Gerar Ficheiro"

    override val textBox: Boolean
        get() = false

    override var input: String = ""

    override fun execute(window: JsonTreeSkeleton) {
        var gerador = JsonGenerator()
        gerador.fileGenerator(window.elementoSelecionado!!.data.toString())
    }

    override fun undo(window: JsonTreeSkeleton) {
        window.undo()
    }
}

class Read : JsonAction {
    override val name: String
        get() = ""

    override val textBox: Boolean
        get() = false

    override var input: String = ""

    override fun execute(window: JsonTreeSkeleton) {
        window.read()
    }

    override fun undo(window: JsonTreeSkeleton) {
        window.undo()
    }
}

class Remove : JsonAction {
    override val name: String
        get() = "Delete"

    override val textBox: Boolean
        get() = false

    override var input: String = ""

    override fun execute(window: JsonTreeSkeleton) {
        window.remove()
    }

    override fun undo(window: JsonTreeSkeleton) {
        window.undo()
    }
}

class Undo : JsonAction {
    override val name: String
        get() = "Anular" //texto do botão

    override val textBox: Boolean
        get() = false

    override var input: String = ""

    override fun execute(window: JsonTreeSkeleton) {
        window.undo()
    }
    override fun undo(window: JsonTreeSkeleton) {
        window.undo()
    }
}