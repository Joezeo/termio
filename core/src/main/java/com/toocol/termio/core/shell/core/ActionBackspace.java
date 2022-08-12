package com.toocol.termio.core.shell.core;

import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.event.CharEvent;
import com.toocol.termio.utilities.utils.ASCIIStrCache;
import com.toocol.termio.utilities.utils.CharUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:46
 */
public final class ActionBackspace extends ShellCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.BACKSPACE};
    }

    @Override
    public boolean act(Shell shell, CharEvent charEvent, char inChar) {
        if (shell.status.equals(Shell.Status.QUICK_SWITCH)) {
            return false;
        }
        int[] cursorPosition = shell.term.getCursorPosition();
        if (cursorPosition[0] <= shell.prompt.get().length()) {
            Printer.bel();
            shell.status = Shell.Status.NORMAL;
            return false;
        }

        if (cursorPosition[0] < shell.currentPrint.length() + shell.prompt.get().length()) {
            // cursor has moved
            int index = cursorPosition[0] - shell.prompt.get().length() - 1;
            if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                String removal = "\u007F".repeat(shell.remoteCmd.length());
                shell.remoteCmd.deleteCharAt(index);
                shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(shell.remoteCmd);
                removal += shell.remoteCmd.toString();
                shell.tabAccomplishLastStroke = ASCIIStrCache.toString(inChar);
                shell.writeAndFlush(removal.getBytes(StandardCharsets.UTF_8));
                remoteCursorOffset = true;
            }
            if (shell.status.equals(Shell.Status.NORMAL)) {
                shell.cmd.deleteCharAt(index);
            }
            shell.currentPrint.deleteCharAt(index);
            shell.term.hideCursor();
            Printer.virtualBackspace();
            Printer.print(shell.currentPrint.substring(index, shell.currentPrint.length()) + CharUtil.SPACE);
            shell.term.setCursorPosition(cursorPosition[0] - 1, cursorPosition[1]);
            shell.term.showCursor();
        } else {
            if (localLastInputBuffer.length() > 0) {
                localLastInputBuffer.deleteCharAt(localLastInputBuffer.length() - 1);
            }
            if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                // This is ctrl+backspace
                shell.writeAndFlush('\u007F');
                if (shell.remoteCmd.length() > 0) {
                    String newVal = shell.remoteCmd.toString().substring(0, shell.remoteCmd.length() - 1);
                    shell.remoteCmd.delete(0, shell.remoteCmd.length()).append(newVal);
                }
                if (shell.localLastCmd.length() > 0) {
                    String newVal = shell.localLastCmd.toString().substring(0, shell.localLastCmd.length() - 1);
                    shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(newVal);
                }
            }
            if (shell.status.equals(Shell.Status.NORMAL)) {
                shell.cmd.deleteCharAt(shell.cmd.length() - 1);
            }
            if (shell.currentPrint.length() > 0) {
                String newVal = shell.currentPrint.toString().substring(0, shell.currentPrint.length() - 1);
                shell.currentPrint.delete(0, shell.currentPrint.length()).append(newVal);
            }

            Printer.virtualBackspace();
        }
        return false;
    }
}