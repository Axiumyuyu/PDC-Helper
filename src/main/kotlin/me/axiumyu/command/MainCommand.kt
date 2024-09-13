package me.axiumyu.command

import me.axiumyu.PDCHelper.Companion.server
import me.axiumyu.Util.subCommand
import me.axiumyu.Util.tileEntities
import me.axiumyu.command.subCommandHandler.*
import org.bukkit.Bukkit.getEntity
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material.AIR
import org.bukkit.block.Block
import org.bukkit.block.TileState
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID.fromString

class MainCommand : CommandExecutor {
    /*
     * 命令格式： <>必选   []可选
     *            0          1          2
     * /pdc <holderType> [target] <get/remove/set> <nameSpaceKey> [type] [value]
     *
     *          0                 1              2           3
     * /pdc <holderType> <get/remove/set> []nameSpaceKey] [type] [value]
     */

    //命令主入口
    override fun onCommand(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>?
    ): Boolean {
        if (!p0.isOp) {
            p0.sendMessage("You have no permission to use this command")
            return false
        }
        val params = p3?.slice(1 until p3.size) ?: return false
        when (p3[0]) {
            //每个分支由不同的类处理，
            //将参数表去除第一个后传递给下一个函数，确保第一个参数始终为子命令(subCommand)
            "player" -> {
                if (subCommand.contains(p3[1])) {
                    //                 0               1
                    // / pdc player <get/remove/set> <nameSpaceKey> [type] [value]
                    PlayerPDC.invoke(p0 as Player, params)
                } else {
                    //                 0               1            2
                    // / pdc player <target> | <get/remove/set> <nameSpaceKey> [type] [value]
                    server.getPlayer(p3[1])?.let { PlayerPDC.invoke(it, params.subList(1, params.size)) }
                        ?: run { p0.sendMessage("Player not found") }
                }
            }

            "world" -> {
                //              0                1
                // /pdc world <get/remove/set> <nameSpaceKey> [type] [value]
                if (subCommand.contains(p3[1]) && p0 is Player) {
                    WorldPDC.invoke(p0.world, params, p0)
                } else {
                    //              0                1            2
                    // /pdc world <target> | <get/remove/set> <nameSpaceKey> [type] [value]
                    server.getWorld(p3[1])?.let { WorldPDC.invoke(it, params.subList(1, params.size), p0) }
                        ?: run { p0.sendMessage("World not found") }
                }
            }

            "entity" -> {
                //                   0                1
                // /pdc entity <get/remove/set> <nameSpaceKey> [type] [value]
                if (subCommand.contains(p3[1]) && p0 is Player) {
                    p0.getTargetEntity(50)?.let { EntityPDC.invoke(it, params, p0) }
                        ?: p0.sendMessage("No Entity you are looking at")
                } else {
                    //                 0                1            2
                    // /pdc entity <target> | <get/remove/set> <nameSpaceKey> [type] [value]
                    getEntity(fromString(p3[1]))?.let { EntityPDC.invoke(it, params.subList(1, params.size), p0) }
                        ?: run { p0.sendMessage("Entity not found") }
                }
            }

            "item" -> {
                //                 0                1
                // /pdc item <get/remove/set> <nameSpaceKey> [type] [value]
                if (p0 is Player && p0.inventory.itemInMainHand.type != AIR) {
                    ItemPDC.invoke(p0.inventory.itemInMainHand, params, p0)
                } else {
                    p0.sendMessage("No item in mainHand")
                }
            }

            /*
             *chunk,tile 可以在控制台命令行获取，因此做了检测
             * 下文的 getChunk(p0, params)/getBlock(p0, params)
             * （要不还是删掉这个功能吧）
             */
            "chunk" -> {
                val chunk by lazy {
                    try {
                        getChunk(p0, params)
                    } catch (_: Exception) {
                        p0.sendMessage("Invalid Number")
                    }
                }
                //                 0                1
                // /pdc chunk <get/remove/set> <nameSpaceKey> [type] [value]
                if (p0 is Player) {
                    ChunkPDC.invoke(p0.chunk, params, p0)
                } else {
                    if (chunk is Chunk) {
                        ChunkPDC.invoke(chunk as Chunk, params.subList(3, params.size), p0)
                    }
                }
            }

            "tile" -> {

                val block by lazy {
                    try {
                        getBlock(p0, params)
                    } catch (_: Exception) {
                        p0.sendMessage("Invalid Number")
                    }
                }
                //                 0                1
                // /pdc tile <get/remove/set> <nameSpaceKey> [type] [value]
                if (p0 is Player && p0.getTargetBlock(tileEntities, 50).state is TileState &&
                    !subCommand.contains(p3[1])) {
                    TilePDC.invoke(p0.location.block.state as TileState, params, p0)
                } else {
                    if (block is Block) {
                        if ((block as Block).state is TileState) {
                            TilePDC.invoke((block as Block).state as TileState, params.subList(3, params.size), p0)
                        } else {
                            p0.sendMessage("No TileEntity at this location")
                        }
                    }
                }


            }

            else -> return false

        }
        return true
    }


    private fun getBlock(
        p0: CommandSender, params: List<String>
    ): Block? = if (p0 is Player) {
        //            0   1   2        3                 4
        // /pdc tile <x> <y> <z> | <get/remove/set> <nameSpaceKey> [type] [value]

        p0.world.getBlockAt(
            Location(p0.world, params[0].toDouble(), params[1].toDouble(), params[2].toDouble())
        )

    } else {
        //              0     1   2   3
        // /pdc tile <world> <x> <y> <z> | <get/...>
        server.getWorld(params[0])?.getBlockAt(
            Location(
                server.getWorld(params[0]), params[1].toDouble(), params[2].toDouble(), params[3].toDouble()
            )
        )
    }


    private fun getChunk(p0: CommandSender, params: List<String>): Chunk? = if (p0 is Player) {
        //            0   1   2        3                 4
        // /pdc tile <x> <y> <z> | <get/remove/set> <nameSpaceKey> [type] [value]
        p0.world.getChunkAt(
            Location(p0.world, params[0].toDouble(), params[1].toDouble(), params[2].toDouble())
        )
    } else {
        //              0     1   2   3
        // /pdc tile <world> <x> <y> <z> | <get/...>
        server.getWorld(params[0])?.getChunkAt(
            Location(
                server.getWorld(params[0]), params[1].toDouble(), params[2].toDouble(),
                params[3].toDouble()
            )
        )
    }
}