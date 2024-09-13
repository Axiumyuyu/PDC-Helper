package me.axiumyu

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.TileState
import org.bukkit.command.CommandSender
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.io.IOException
import java.io.Serializable


object Util {
    @JvmField
    val subCommand = listOf("get", "remove", "set", "list", "clear")

    @JvmField
    val typeMap = hashMapOf(
        "byte" to PersistentDataType.BYTE,
        "byte_array" to PersistentDataType.BYTE_ARRAY,
        "double" to PersistentDataType.DOUBLE,
        "float" to PersistentDataType.FLOAT,
        "int" to PersistentDataType.INTEGER,
        "long" to PersistentDataType.LONG,
        "short" to PersistentDataType.SHORT,
        "string" to PersistentDataType.STRING,
        "boolean" to PersistentDataType.BOOLEAN,
        "int_array" to PersistentDataType.INTEGER_ARRAY,
        "long_array" to PersistentDataType.LONG_ARRAY
    )

    val tileEntities by lazy { Material.entries.filter { it.createBlockData().createBlockState() is TileState }.toSet() }

    @JvmStatic
    fun checkNameSpace(nameSpace: String): Boolean {
        return Regex("^[a-z0-9]+:[a-z0-9]+\$").matches(nameSpace)
    }

    @JvmStatic
    fun String.toIntArray(): IntArray {
        if (this.startsWith("[I;") && this.endsWith("]")) return this.substring(3, this.length - 1).split(",")
            .map { it.toInt() }.toIntArray()
        else throw IllegalArgumentException("不正确的格式，应以[I;开头，以]结尾")
    }

    @JvmStatic
    fun String.toLongArray(): LongArray {
        if (this.startsWith("[L;") && this.endsWith("]")) return this.substring(3, this.length - 1).split(",")
            .map { it.toLong() }.toLongArray()
        else throw IllegalArgumentException("不正确的格式，应以[L;开头，以]结尾")
    }

    @JvmStatic
    fun setData(
        pdc: PersistentDataContainer,
        dataType: PersistentDataType<*, *>,
        nameSpace: NamespacedKey,
        data: String
    ) {
        return when (dataType) {
            PersistentDataType.STRING -> pdc.set(nameSpace, PersistentDataType.STRING, data)
            PersistentDataType.INTEGER -> pdc.set(nameSpace, PersistentDataType.INTEGER, data.toInt())
            PersistentDataType.DOUBLE -> pdc.set(nameSpace, PersistentDataType.DOUBLE, data.toDouble())
            PersistentDataType.LONG -> pdc.set(nameSpace, PersistentDataType.LONG, data.toLong())
            PersistentDataType.FLOAT -> pdc.set(nameSpace, PersistentDataType.FLOAT, data.toFloat())
            PersistentDataType.BOOLEAN -> pdc.set(nameSpace, PersistentDataType.BOOLEAN, data.toBoolean())
            PersistentDataType.BYTE_ARRAY -> pdc.set(nameSpace, PersistentDataType.BYTE_ARRAY, data.toByteArray())
            PersistentDataType.LONG_ARRAY -> pdc.set(nameSpace, PersistentDataType.LONG_ARRAY, data.toLongArray())
            PersistentDataType.INTEGER_ARRAY -> pdc.set(nameSpace, PersistentDataType.INTEGER_ARRAY, data.toIntArray())
            else -> {}
        }
    }

    @JvmStatic
    fun getData(
        p0: PersistentDataContainer,
        dataType: PersistentDataType<*, *>,
        nameSpace: NamespacedKey,
    ): Serializable? {
        return when (dataType) {
            PersistentDataType.STRING -> p0.get(nameSpace, PersistentDataType.STRING)
            PersistentDataType.INTEGER -> p0.get(nameSpace, PersistentDataType.INTEGER)
            PersistentDataType.DOUBLE -> p0.get(nameSpace, PersistentDataType.DOUBLE)
            PersistentDataType.LONG -> p0.get(nameSpace, PersistentDataType.LONG)
            PersistentDataType.FLOAT -> p0.get(nameSpace, PersistentDataType.FLOAT)
            PersistentDataType.BOOLEAN -> p0.get(nameSpace, PersistentDataType.BOOLEAN)
            PersistentDataType.BYTE_ARRAY -> p0.get(nameSpace, PersistentDataType.BYTE_ARRAY)
            PersistentDataType.LONG_ARRAY -> p0.get(nameSpace, PersistentDataType.LONG_ARRAY)
            PersistentDataType.INTEGER_ARRAY -> p0.get(nameSpace, PersistentDataType.INTEGER_ARRAY)
            else -> throw IllegalArgumentException("未知的数据类型")
        }
    }

    @JvmStatic
    fun allFromString(allName: String): NamespacedKey? {
        val split = allName.split(":")
        return if (split.size == 2) {
            NamespacedKey(split[0], split[1])
        } else null
    }

    @JvmStatic
    fun mainProcess(
        receiver: CommandSender,
        index: Int,
        params: List<String>,
        pdc: PersistentDataContainer
    ) {
        val namespacedKey = allFromString(params[index + 1])
        if (namespacedKey == null && !subCommand.contains(params[index])) return
        when (params[index]) {
            "get" -> {
                try {
                    if (params.size > index + 2 && typeMap.containsKey(params[index + 2])) {
                        val dataType = typeMap[params[index + 2]] as PersistentDataType
                        receiver.sendMessage(
                            "${params[index + 1]}的值（指定为${dataType}类型）为${
                                namespacedKey?.let { getData(pdc, dataType, it) }.toString()
                            }"
                        )
                        receiver.sendMessage("如发现与预期值不对请尝试更改类型或者不指定类型")
                    } else {
                        var result: Any? = null
                        typeMap.values.forEach {
                            result = namespacedKey?.let { p0 -> pdc.get(p0, it) }
                            if (result != null) return@forEach
                        }
                        result?.let { receiver.sendMessage("${params[index + 1]}的值为$result") }
                            ?: receiver.sendMessage("${params[index + 1]}不存在或类型无法推断")
                    }
                } catch (e: IllegalArgumentException) {
                    e.message?.let { receiver.sendMessage(it) }
                }
            }

            "list" -> {
                try {
                    receiver.sendMessage(pdc.serializeToBytes().toString())
                } catch (_: IOException) {
                    receiver.sendMessage("序列化失败")
                }
            }

            "remove" -> {
                namespacedKey?.let {
                    if (pdc.has(it)) {
                        pdc.remove(namespacedKey)
                        receiver.sendMessage("成功删除${params[index + 1]}")
                    } else {
                        receiver.sendMessage("${params[index + 1]}不存在")
                    }
                }

            }

            "set" -> {
                if (params.size >= index + 3 && typeMap.containsKey(params[index + 2])) {
                    val dataType = typeMap[params[index + 2]] as PersistentDataType<*, *>
                    try {
                        namespacedKey?.let { setData(pdc, dataType, it, params[index + 3]) }
                        receiver.sendMessage(
                            "成功设置${params[index + 1]}为${params[index + 3]}（指定为${dataType}类型）"
                        )
                    } catch (_: IllegalArgumentException) {
                        receiver.sendMessage("参数${params[index + 3]}无法转换为${dataType}类型")
                    }
                } else {
                    receiver.sendMessage("请指定正确的类型和值")
                }
            }

            "clear" -> {
                val keys = pdc.keys
                receiver.sendMessage("正在移除共${keys.size}个键")
                keys.forEach { pdc.remove(it) }
                receiver.sendMessage("完成")
            }
        }
    }
}