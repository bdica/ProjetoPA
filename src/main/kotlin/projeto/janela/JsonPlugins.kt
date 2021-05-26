package projeto.janela

import org.eclipse.swt.layout.GridLayout
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
        get() = ""
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