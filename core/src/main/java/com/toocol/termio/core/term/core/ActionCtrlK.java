package com.toocol.termio.core.term.core;

import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.event.CharEvent;
import com.toocol.termio.utilities.utils.StrUtil;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:04
 */
public final class ActionCtrlK extends TermCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.CTRL_K};
    }

    @Override
    public boolean actOnConsole(Term term, CharEvent charEvent, char inChar) {
        Printer.clear();
        term.lineBuilder.delete(0, term.lineBuilder.length());
        term.executeCursorOldX.set(Term.getPromptLen());
        term.printScene(false);
        TermPrinter.displayBuffer = StrUtil.EMPTY;
        return false;
    }

    @Override
    public boolean actOnDesktop(Term term, CharEvent charEvent, char inChar) {
        return false;
    }
}
