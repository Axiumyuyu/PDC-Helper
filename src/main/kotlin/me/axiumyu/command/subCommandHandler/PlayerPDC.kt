package me.axiumyu.command.subCommandHandler

import me.axiumyu.Util.checkNameSpace
import me.axiumyu.Util.mainProcess
import me.axiumyu.Util.subCommand
import org.bukkit.entity.Player

object PlayerPDC {

    fun invoke(p1: Player, params: List<String>){
        params.forEachIndexed { index, i ->
            if (params.size > index + 1 && checkNameSpace(params[index + 1]) && subCommand.contains(i)) {
                 mainProcess(p1,index, params, p1.persistentDataContainer)
            }
        }
    }
}