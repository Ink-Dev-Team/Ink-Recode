package com.ink.recode.webgui

import com.ink.recode.Module
import com.ink.recode.ModuleManager
import com.ink.recode.value.BooleanValue
import com.ink.recode.value.ModeValue
import com.ink.recode.value.NumberValue
import com.ink.recode.value.StringValue
import fi.iki.elonen.NanoHTTPD
import org.slf4j.LoggerFactory

import java.io.IOException
import java.util.*

class HttpServer(port: Int) : NanoHTTPD(port) {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            logger.info("Web GUI server started on port $port")
        } catch (e: IOException) {
            logger.error("Failed to start Web GUI server: ${e.message}")
        }
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method
        val params = session.parameters

        logger.info("Received $method request for $uri")

        return when (uri) {
            "/api/modules" -> handleModulesRequest()
            "/api/module/state" -> handleModuleStateRequest(params)
            "/api/module/value" -> handleModuleValueRequest(params)
            else -> handleStaticRequest(uri)
        }
    }

    private fun handleModulesRequest(): Response {
        val modules = ModuleManager.modules.map {
            mapOf(
                "name" to it.name,
                "description" to it.description,
                "category" to it.category.name,
                "enabled" to it.enabled.toString(),
                "key" to it.key.toString(),
                "values" to it.values.map {
                    val valueMap = mutableMapOf(
                        "name" to it.name,
                        "type" to it.javaClass.simpleName,
                        "description" to it.description
                    )

                    when (it) {
                        is BooleanValue -> valueMap["value"] = it.get().toString()
                        is NumberValue -> valueMap["value"] = it.get().toString()
                        is ModeValue -> {
                            valueMap["value"] = it.get().toString()
                            valueMap["options"] = it.options.joinToString(",")
                        }
                        is StringValue -> valueMap["value"] = it.get()
                    }

                    valueMap
                }
            )
        }

        return newFixedLengthResponse(Response.Status.OK, "application/json", toJson(modules))
    }

    private fun handleModuleStateRequest(params: Map<String, List<String>>): Response {
        val moduleName = params["module"]?.firstOrNull() ?: return badRequest("Missing module name")
        val enabled = params["enabled"]?.firstOrNull()?.toBoolean() ?: return badRequest("Missing enabled parameter")

        val module = ModuleManager.getModule(moduleName)
        if (module == null) {
            return notFound("Module not found")
        }

        if (enabled) {
            module.enable()
        } else {
            module.disable()
        }

        return newFixedLengthResponse(Response.Status.OK, "application/json", toJson(mapOf("success" to true.toString(), "enabled" to module.enabled.toString())))
    }

    private fun handleModuleValueRequest(params: Map<String, List<String>>): Response {
        val moduleName = params["module"]?.firstOrNull() ?: return badRequest("Missing module name")
        val valueName = params["value"]?.firstOrNull() ?: return badRequest("Missing value name")
        val valueData = params["data"]?.firstOrNull() ?: return badRequest("Missing value data")

        val module = ModuleManager.getModule(moduleName)
        if (module == null) {
            return notFound("Module not found")
        }

        val value = module.values.find { it.name == valueName }
        if (value == null) {
            return notFound("Value not found")
        }

        try {
            when (value) {
                is BooleanValue -> {
                    val boolValue = valueData.toBoolean()
                    value.set(boolValue)
                    logger.info("BooleanValue $valueName set to $boolValue")
                }
                is NumberValue -> {
                    val numValue = valueData.toDoubleOrNull() ?: 0.0
                    value.set(numValue)
                    logger.info("NumberValue $valueName set to $numValue")
                }
                is ModeValue -> {
                    val modeValue = valueData.toIntOrNull() ?: 0
                    value.set(modeValue)
                    logger.info("ModeValue $valueName set to $modeValue")
                }
                is StringValue -> {
                    value.set(valueData)
                    logger.info("StringValue $valueName set to $valueData")
                }
            }

            return newFixedLengthResponse(Response.Status.OK, "application/json", toJson(mapOf("success" to true.toString())))
        } catch (e: Exception) {
            logger.error("Error setting value $valueName: ${e.message}")
            return internalError("Failed to set value: ${e.message}")
        }
    }

    private fun handleStaticRequest(uri: String): Response {
        val path = if (uri == "/") "/index.html" else uri
        val resource = javaClass.getResourceAsStream("/webgui$path")

        if (resource == null) {
            return notFound("File not found")
        }

        val contentType = when (path.substringAfterLast(".")) {
            "html" -> "text/html"
            "js" -> "application/javascript"
            "css" -> "text/css"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            else -> "application/octet-stream"
        }

        return newFixedLengthResponse(Response.Status.OK, contentType, resource, resource.available().toLong())
    }

    private fun badRequest(message: String): Response {
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", toJson(mapOf("error" to message)))
    }

    private fun notFound(message: String): Response {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", toJson(mapOf("error" to message)))
    }

    private fun internalError(message: String): Response {
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", toJson(mapOf("error" to message)))
    }

    private fun toJson(obj: Any): String {
        // 简单的JSON序列化，实际项目中可以使用Gson等库
        return when (obj) {
            is Map<*, *> -> "{${obj.entries.joinToString(",") { "\"${it.key}\":${toJson(it.value!!)}" }}}"
            is List<*> -> "[${obj.joinToString(",") { toJson(it!!) }}]"
            is String -> "\"$obj\""
            is Number, is Boolean -> obj.toString()
            null -> "null"
            else -> "\"${obj.toString()}\""
        }
    }
}