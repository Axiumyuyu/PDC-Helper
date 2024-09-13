package me.axiumyu.command.subCommandHandler

import me.axiumyu.Util.checkNameSpace
import me.axiumyu.Util.mainProcess
import me.axiumyu.Util.subCommand
import org.bukkit.block.TileState
import org.bukkit.command.CommandSender

/*
* /pdc tile [target] <get/remove/set/list/clear> <nameSpaceKey> [type] [value]
*  index: <get/remove/set/list/clear>
*  index+1:<nameSpaceKey>
*  index+2:<type>
*  index+3:<value>
*/
object TilePDC{

    fun invoke(tileState: TileState,params: List<String>,cs: CommandSender){
        params.forEachIndexed { index, i ->
            if (params.size > index + 1 && checkNameSpace(params[index + 1]) && subCommand.contains(i)) {
                 mainProcess(cs,index, params, tileState.persistentDataContainer)
            }
        }
    }
}