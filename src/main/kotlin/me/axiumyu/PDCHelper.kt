package me.axiumyu

import me.axiumyu.command.MainCommand
import org.bukkit.Server
import org.bukkit.plugin.java.JavaPlugin

class PDCHelper : JavaPlugin() {
    companion object {
        lateinit var server: Server
    }

    override fun onEnable() {
        //不知道为什么直接用Bukkit的getPlayer()等方法似乎无法正确获得对应的玩家，世界等等
        Companion.server = this.server
        getCommand("pdc")?.setExecutor(MainCommand())
    }

    override fun onDisable() {
    }

}
