package com.toocol.termio.desktop.components.executor.ui

import com.toocol.termio.desktop.components.panel.ui.MajorPanel
import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import javafx.geometry.Point2D
import javafx.scene.input.InputMethodRequests
import org.fxmisc.richtext.Caret
import org.fxmisc.richtext.StyleClassedTextField

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/14 23:31
 * @version: 0.0.1
 */
class CommandExecutorInput(private val id: Long) : StyleClassedTextField(), IStyleAble, IComponent {
    override fun styleClasses(): Array<String> {
        return arrayOf(
            "command-executor-input"
        )
    }

    override fun initialize() {
        styled()
        isEditable = true
        showCaret = Caret.CaretVisibility.ON
        inputMethodRequests = InputMethodRequestsObject()
        val majorPanel = findComponent(MajorPanel::class.java, 1)
        prefWidthProperty().bind(majorPanel.widthProperty().multiply(0.85))
        prefHeightProperty().bind(majorPanel.heightProperty().multiply(0.03))
    }

    override fun id(): Long {
        return id
    }

    private class InputMethodRequestsObject : InputMethodRequests {
        override fun getSelectedText(): String {
            return ""
        }

        override fun getLocationOffset(x: Int, y: Int): Int {
            return 0
        }

        override fun cancelLatestCommittedText() {}
        override fun getTextLocation(offset: Int): Point2D {
            return Point2D(0.0, 0.0)
        }
    }
}