package me.axiumyu.command.subCommandHandler

import me.axiumyu.Util.checkNameSpace
import me.axiumyu.Util.mainProcess
import me.axiumyu.Util.subCommand
import org.bukkit.World
import org.bukkit.command.CommandSender

object WorldPDC {

    fun invoke(world: World,params: List<String>,cs: CommandSender){
        params.forEachIndexed { index, i ->
            if (params.size > index + 1 && checkNameSpace(params[index + 1]) && subCommand.contains(i)) {
                mainProcess(cs,index, params, world.persistentDataContainer)
            }
        }
    }
}