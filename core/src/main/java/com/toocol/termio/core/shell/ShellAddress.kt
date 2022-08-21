package com.toocol.termio.core.shell

import com.toocol.termio.utilities.module.IAddress

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
enum class ShellAddress(
    // the address string of message
    private val address: String
) : IAddress {

    /**
     * accept the shell cmd
     */
    RECEIVE_SHELL("ssh.accept.shell.cmd"),

    /**
     * exhibit the shell feedback
     */
    DISPLAY_SHELL("ssh.exhibit.shell"),

    /**
     * start uf command: chose file to upload
     */
    START_UF_COMMAND("ssh.start.uf"),

    /**
     * start uf command: chose file and download to local
     */
    START_DF_COMMAND("ssh.start.df"),

    /**
     * execute the single command and response the execution's result.
     */
    EXECUTE_SINGLE_COMMAND("ssh.exec.single.command"),

    /**
     * execute the single command in certain shell and response the execution's result.
     */
    EXECUTE_SINGLE_COMMAND_IN_CERTAIN_SHELL("ssh.exec.single.command.in.certain.shell");

    override fun address(): String {
        return address
    }
}