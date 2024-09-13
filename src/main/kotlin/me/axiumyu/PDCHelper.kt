package me.axiumyu

import me.axiumyu.command.MainCommand
import org.bukkit.Server
import org.bukkit.plugin.java.JavaPlugin

class PDCHelper : JavaPlugin() {
    companion object {
        lateinit var server: Server
    }

    override fun onEnable() {
        Companion.server = this.server
        getCommand("pdc")?.setExecutor(MainCommand())
    }

    override fun onDisable() {
    }

}
