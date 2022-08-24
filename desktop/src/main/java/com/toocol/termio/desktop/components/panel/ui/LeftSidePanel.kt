package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.desktop.components.sidebar.ui.SessionManageSidebar
import com.toocol.termio.desktop.components.terminal.ui.DesktopTerminalFactory
import com.toocol.termio.platform.component.Component
import com.toocol.termio.platform.component.ComponentsParser
import com.toocol.termio.platform.component.RegisterComponent
import com.toocol.termio.platform.ui.TBorderPane
import com.toocol.termio.platform.ui.TScene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/22 22:17
 * @version: 0.0.1
 */
@RegisterComponent(value = [
    Component(clazz = SessionManageSidebar::class, id = 1, initialVisible = true)
])
class LeftSidePanel(id: Long) : TBorderPane(id){
    private val parser: ComponentsParser = ComponentsParser()

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "left-side-panel"
        )
    }

    override fun initialize() {
        styled()
        val majorPanel = findComponent(MajorPanel::class.java, 1)
        prefWidthProperty().bind(majorPanel.widthProperty().multiply(0.15))
        prefHeightProperty().bind(majorPanel.heightProperty())

        parser.parse(LeftSidePanel::class.java)
        parser.initializeAll()

        val scene = findComponent(TScene::class.java, 1)
        val alt1: KeyCombination = KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN)
        scene.accelerators[alt1] = Runnable {
            val ratio = if (isVisible) {
                hide()
                1.0
            } else {
                show()
                0.85
            }
            findComponent(WorkspacePanel::class.java, 1).prefWidthProperty().bind(majorPanel.widthProperty().multiply(ratio))
            DesktopTerminalFactory.getAllTerminals().forEach {
                it.prefWidthProperty().bind(majorPanel.widthProperty().multiply(ratio))
                it.getConsoleTextAre().prefWidthProperty().bind(majorPanel.widthProperty().multiply(ratio))
            }
        }

        center = parser.getAsNode(SessionManageSidebar::class.java)
    }

    override fun actionAfterShow() {
    }
}