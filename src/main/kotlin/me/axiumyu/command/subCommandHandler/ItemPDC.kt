package me.axiumyu.command.subCommandHandler

import me.axiumyu.Util.checkNameSpace
import me.axiumyu.Util.mainProcess
import me.axiumyu.Util.subCommand
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack

object ItemPDC {

    fun invoke(item: ItemStack, params: List<String>, cs: CommandSender){
        params.forEachIndexed { index, i ->
            if (params.size > index + 1 && checkNameSpace(params[index + 1]) && subCommand.contains(i)) {
                 mainProcess(cs, index, params, item.itemMeta.persistentDataContainer)
            }
        }
    }
}