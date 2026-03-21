package com.ink.recode.webgui

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ink.recode.Module
import com.ink.recode.ModuleManager
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread

data class ModuleInfo(
    val name: String,
    val description: String,
    val category: String,
    val enabled: Boolean,
    val key: Int,
    val values: List<ValueInfo>
)

data class ValueInfo(
    val name: String,
    val description: String,
    val type: String,
    val value: Any
)

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val data: Any? = null
)

object HttpServer {
    
    private val logger = LoggerFactory.getLogger("WebGUI")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val port = 8000
    
    fun start() {
        if (isRunning) {
            logger.warn("HTTP server is already running on port $port")
            return
        }
        
        try {
            serverSocket = ServerSocket(port)
            isRunning = true
            logger.info("HTTP server started on port $port")
            logger.info("Access WebGUI at: http://localhost:$port")
            
            thread(name = "HTTP-Server") {
                while (isRunning) {
                    try {
                        val client = serverSocket?.accept()
                        if (client != null) {
                            thread(name = "HTTP-Client-${client.port}") {
                                handleClient(client)
                            }
                        }
                    } catch (e: Exception) {
                        if (isRunning) {
                            logger.error("Error accepting client connection", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to start HTTP server", e)
            isRunning = false
        }
    }
    
    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
            logger.info("HTTP server stopped")
        } catch (e: Exception) {
            logger.error("Error stopping HTTP server", e)
        }
    }
    
    private fun handleClient(client: Socket) {
        try {
            client.use { socket ->
                val input = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
                val output = socket.getOutputStream()
                
                val requestLine = input.readLine() ?: return
                logger.debug("Request: $requestLine")
                
                val parts = requestLine.split(" ")
                if (parts.size < 2) return
                
                val method = parts[0]
                val path = parts[1].split("?")[0]
                
                when (path) {
                    "/" -> handleRoot(output)
                    "/api/modules" -> handleGetModules(output)
                    "/api/toggle" -> handleToggle(input, output)
                    "/api/value" -> handleSetValue(input, output)
                    else -> handleNotFound(output)
                }
            }
        } catch (e: Exception) {
            logger.error("Error handling client", e)
        }
    }
    
    private fun handleRoot(output: java.io.OutputStream) {
        val html = loadHtmlContent()
        sendResponse(output, 200, "OK", "text/html", html)
    }
    
    private fun handleGetModules(output: java.io.OutputStream) {
        try {
            val modules = ModuleManager.modules.map { module ->
                val valueInfos = module.values.map { value ->
                    val valueData = when (value) {
                        is com.ink.recode.value.ModeValue -> mapOf(
                            "value" to value.get(),
                            "options" to value.options,
                            "current" to value.current
                        )
                        is com.ink.recode.value.BooleanValue -> mapOf(
                            "value" to value.get()
                        )
                        is com.ink.recode.value.NumberValue -> mapOf(
                            "value" to value.get(),
                            "min" to value.min,
                            "max" to value.max,
                            "step" to value.step
                        )
                        else -> mapOf("value" to value.get())
                    }
                    
                    ValueInfo(
                        name = value.name,
                        description = value.description,
                        type = when (value) {
                            is com.ink.recode.value.ModeValue -> "mode"
                            is com.ink.recode.value.BooleanValue -> "boolean"
                            is com.ink.recode.value.NumberValue -> "number"
                            else -> "unknown"
                        },
                        value = valueData
                    )
                }
                
                ModuleInfo(
                    name = module.name,
                    description = module.description,
                    category = module.category.name,
                    enabled = module.enabled,
                    key = module.key,
                    values = valueInfos
                )
            }
            
            val response = ApiResponse(true, "Modules retrieved successfully", modules)
            sendJsonResponse(output, 200, response)
        } catch (e: Exception) {
            logger.error("Error getting modules", e)
            sendJsonResponse(output, 500, ApiResponse(false, "Error retrieving modules"))
        }
    }
    
    private fun handleToggle(input: BufferedReader, output: java.io.OutputStream) {
        try {
            val body = readRequestBody(input)
            val params = parseParams(body)
            
            val moduleName = params["name"]
            if (moduleName == null) {
                sendJsonResponse(output, 400, ApiResponse(false, "Module name is required"))
                return
            }
            
            val module = ModuleManager.modules.find { it.name == moduleName }
            if (module == null) {
                sendJsonResponse(output, 404, ApiResponse(false, "Module not found: $moduleName"))
                return
            }
            
            module.toggle()
            logger.info("Module ${module.name} toggled to ${module.enabled}")
            
            sendJsonResponse(output, 200, ApiResponse(true, "Module ${module.name} toggled successfully", mapOf(
                "name" to module.name,
                "enabled" to module.enabled
            )))
        } catch (e: Exception) {
            logger.error("Error toggling module", e)
            sendJsonResponse(output, 500, ApiResponse(false, "Error toggling module: ${e.message}"))
        }
    }
    
    private fun handleSetValue(input: BufferedReader, output: java.io.OutputStream) {
        try {
            val body = readRequestBody(input)
            val params = parseParams(body)
            
            val moduleName = params["name"]
            val valueName = params["value"]
            val valueData = params["data"]
            
            if (moduleName == null || valueName == null) {
                sendJsonResponse(output, 400, ApiResponse(false, "Module name and value name are required"))
                return
            }
            
            val module = ModuleManager.modules.find { it.name == moduleName }
            if (module == null) {
                sendJsonResponse(output, 404, ApiResponse(false, "Module not found: $moduleName"))
                return
            }
            
            val value = module.values.find { it.name == valueName }
            if (value == null) {
                sendJsonResponse(output, 404, ApiResponse(false, "Value not found: $valueName"))
                return
            }
            
            when (value) {
                is com.ink.recode.value.BooleanValue -> {
                    val boolValue = valueData?.toBoolean() ?: false
                    value.set(boolValue)
                    logger.info("BooleanValue $valueName set to $boolValue")
                }
                is com.ink.recode.value.NumberValue -> {
                    val numValue = valueData?.toFloatOrNull() ?: 0f
                    value.set(numValue)
                    logger.info("NumberValue $valueName set to $numValue")
                }
                is com.ink.recode.value.ModeValue -> {
                    val modeValue = valueData?.toIntOrNull() ?: 0
                    value.set(modeValue)
                    logger.info("ModeValue $valueName set to $modeValue")
                }
                else -> {
                    sendJsonResponse(output, 400, ApiResponse(false, "Unsupported value type"))
                    return
                }
            }
            
            sendJsonResponse(output, 200, ApiResponse(true, "Value set successfully", mapOf(
                "module" to moduleName,
                "value" to valueName,
                "data" to value.get()
            )))
        } catch (e: Exception) {
            logger.error("Error setting value", e)
            sendJsonResponse(output, 500, ApiResponse(false, "Error setting value: ${e.message}"))
        }
    }
    
    private fun handleNotFound(output: java.io.OutputStream) {
        val response = ApiResponse(false, "Endpoint not found")
        sendJsonResponse(output, 404, response)
    }
    
    private fun sendResponse(
        output: java.io.OutputStream,
        statusCode: Int,
        statusText: String,
        contentType: String,
        body: String
    ) {
        val response = StringBuilder()
        response.append("HTTP/1.1 $statusCode $statusText\r\n")
        response.append("Content-Type: $contentType; charset=UTF-8\r\n")
        response.append("Content-Length: ${body.toByteArray(StandardCharsets.UTF_8).size}\r\n")
        response.append("Connection: close\r\n")
        response.append("\r\n")
        response.append(body)
        
        output.write(response.toString().toByteArray(StandardCharsets.UTF_8))
        output.flush()
    }
    
    private fun sendJsonResponse(output: java.io.OutputStream, statusCode: Int, data: Any) {
        val json = gson.toJson(data)
        sendResponse(output, statusCode, getStatusText(statusCode), "application/json", json)
    }
    
    private fun getStatusText(statusCode: Int): String {
        return when (statusCode) {
            200 -> "OK"
            400 -> "Bad Request"
            404 -> "Not Found"
            500 -> "Internal Server Error"
            else -> "Unknown"
        }
    }
    
    private fun readRequestBody(input: BufferedReader): String {
        val contentLength = readContentLength(input)
        if (contentLength <= 0) return ""
        
        val buffer = CharArray(contentLength)
        var totalRead = 0
        while (totalRead < contentLength) {
            val read = input.read(buffer, totalRead, contentLength - totalRead)
            if (read == -1) break
            totalRead += read
        }
        
        return String(buffer, 0, totalRead)
    }
    
    private fun readContentLength(input: BufferedReader): Int {
        var length = 0
        var line: String?
        
        while (input.readLine().also { line = it } != null) {
            if (line.isNullOrEmpty()) break
            if (line!!.lowercase().startsWith("content-length:")) {
                length = line!!.substringAfter(":").trim().toIntOrNull() ?: 0
            }
        }
        
        return length
    }
    
    private fun parseParams(body: String): Map<String, String> {
        if (body.isEmpty()) return emptyMap()
        
        val params = mutableMapOf<String, String>()
        val pairs = body.split("&")
        
        for (pair in pairs) {
            val keyValue = pair.split("=")
            if (keyValue.size == 2) {
                val key = java.net.URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name())
                val value = java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name())
                params[key] = value
            }
        }
        
        return params
    }
    
    private fun loadHtmlContent(): String {
        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>InkRecode WebGUI</title>
                <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&display=swap" rel="stylesheet">
                <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
                <script type="importmap">
                {
                    "imports": {
                        "@material/web/": "https://esm.run/@material/web/"
                    }
                }
                </script>
                <script type="module">
                    import '@material/web/all.js';
                    import {styles as typescaleStyles} from '@material/web/typography/md-typescale-styles.js';
                    document.adoptedStyleSheets.push(typescaleStyles.styleSheet);
                </script>
                <style>
                    body {
                        margin: 0;
                        padding: 16px;
                        background: var(--md-sys-color-surface-container-lowest);
                        min-height: 100vh;
                    }
                    
                    .container {
                        max-width: 1400px;
                        margin: 0 auto;
                    }
                    
                    md-filled-card {
                        margin-bottom: 16px;
                    }
                    
                    .category-section {
                        margin-bottom: 24px;
                    }
                    
                    .modules-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
                        gap: 16px;
                    }
                    
                    .value-row {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        padding: 8px 0;
                    }
                    
                    .value-label {
                        color: var(--md-sys-color-on-surface-variant);
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <md-filled-card style="width: 100%; margin-bottom: 16px;">
                        <div style="padding: 24px;">
                            <h1 class="md-typescale-headline-medium">🎮 InkRecode WebGUI</h1>
                            <p class="md-typescale-body-medium" style="color: var(--md-sys-color-on-surface-variant);">远程管理客户端模块和设置</p>
                            <div style="margin-top: 16px; display: flex; gap: 8px;">
                                <md-outlined-button onclick="loadModules()">
                                    <md-icon slot="icon">refresh</md-icon>
                                    刷新
                                </md-outlined-button>
                                <md-filled-button onclick="window.location.reload()">
                                    <md-icon slot="icon">home</md-icon>
                                    首页
                                </md-filled-button>
                            </div>
                        </div>
                    </md-filled-card>
                    
                    <div id="message-container"></div>
                    
                    <md-chip-set style="margin-bottom: 16px;">
                        <md-assist-chip selected onclick="filterCategory('all')" id="chip-all">
                            <md-icon slot="icon">apps</md-icon>
                            全部
                        </md-assist-chip>
                        <md-assist-chip onclick="filterCategory('combat')" id="chip-combat">
                            <md-icon slot="icon">sports_martial_arts</md-icon>
                            战斗
                        </md-assist-chip>
                        <md-assist-chip onclick="filterCategory('movement')" id="chip-movement">
                            <md-icon slot="icon">directions_run</md-icon>
                            移动
                        </md-assist-chip>
                        <md-assist-chip onclick="filterCategory('render')" id="chip-render">
                            <md-icon slot="icon">visibility</md-icon>
                            渲染
                        </md-assist-chip>
                        <md-assist-chip onclick="filterCategory('player')" id="chip-player">
                            <md-icon slot="icon">person</md-icon>
                            玩家
                        </md-assist-chip>
                    </md-chip-set>
                    
                    <div id="modules-container">
                        <div style="text-align: center; padding: 48px;">
                            <md-circular-progress indeterminate></md-circular-progress>
                            <p class="md-typescale-body-large" style="margin-top: 16px;">正在加载模块...</p>
                        </div>
                    </div>
                </div>
                
                <script type="module">
                    let allModules = [];
                    let currentFilter = 'all';
                    
                    const categoryIcons = {
                        'combat': 'sports_martial_arts',
                        'movement': 'directions_run',
                        'render': 'visibility',
                        'player': 'person'
                    };
                    
                    const categoryNames = {
                        'combat': '战斗',
                        'movement': '移动',
                        'render': '渲染',
                        'player': '玩家'
                    };
                    
                    async function loadModules() {
                        try {
                            const response = await fetch('/api/modules');
                            const result = await response.json();
                            
                            if (result.success) {
                                allModules = result.data;
                                displayModules(allModules);
                            } else {
                                showMessage(result.message, 'error');
                            }
                        } catch (error) {
                            showMessage('加载模块失败: ' + error.message, 'error');
                        }
                    }
                    
                    function filterCategory(category) {
                        currentFilter = category;
                        
                        // 更新chip选中状态
                        document.querySelectorAll('md-assist-chip').forEach(chip => {
                            chip.selected = false;
                        });
                        document.getElementById('chip-' + category).selected = true;
                        
                        // 显示过滤后的模块
                        displayModules(allModules);
                    }
                    
                    function displayModules(modules) {
                        const container = document.getElementById('modules-container');
                        container.innerHTML = '';
                        
                        // 过滤模块
                        let filteredModules = modules;
                        if (currentFilter !== 'all') {
                            filteredModules = modules.filter(m => m.category.toLowerCase() === currentFilter);
                        }
                        
                        if (filteredModules.length === 0) {
                            container.innerHTML = '<div style="text-align: center; padding: 48px;"><md-icon style="font-size: 48px;">info</md-icon><p class="md-typescale-body-large">该分类下没有模块</p></div>';
                            return;
                        }
                        
                        // 按类别分组
                        const groupedModules = {};
                        filteredModules.forEach(function(module) {
                            const category = module.category.toLowerCase();
                            if (!groupedModules[category]) {
                                groupedModules[category] = [];
                            }
                            groupedModules[category].push(module);
                        });
                        
                        // 按类别顺序显示
                        const categoryOrder = ['combat', 'movement', 'render', 'player'];
                        
                        if (currentFilter === 'all') {
                            // 显示所有类别
                            categoryOrder.forEach(function(category) {
                                if (groupedModules[category] && groupedModules[category].length > 0) {
                                    displayCategorySection(container, category, groupedModules[category]);
                                }
                            });
                        } else {
                            // 只显示选中的类别
                            if (groupedModules[currentFilter]) {
                                displayCategoryGrid(container, groupedModules[currentFilter]);
                            }
                        }
                    }
                    
                    function displayCategorySection(container, category, modules) {
                        const section = document.createElement('div');
                        section.className = 'category-section';
                        
                        const title = document.createElement('div');
                        title.style.cssText = 'display: flex; align-items: center; gap: 8px; margin-bottom: 16px; padding: 0 8px;';
                        title.innerHTML = '<md-icon>' + categoryIcons[category] + '</md-icon><span class="md-typescale-title-large">' + categoryNames[category] + '</span><span class="md-typescale-body-medium" style="color: var(--md-sys-color-on-surface-variant);">(' + modules.length + ')</span>';
                        section.appendChild(title);
                        
                        const grid = document.createElement('div');
                        grid.className = 'modules-grid';
                        
                        modules.forEach(function(module) {
                            grid.appendChild(createModuleCard(module));
                        });
                        
                        section.appendChild(grid);
                        container.appendChild(section);
                    }
                    
                    function displayCategoryGrid(container, modules) {
                        const grid = document.createElement('div');
                        grid.className = 'modules-grid';
                        
                        modules.forEach(function(module) {
                            grid.appendChild(createModuleCard(module));
                        });
                        
                        container.appendChild(grid);
                    }
                    
                    function createModuleCard(module) {
                        const card = document.createElement('md-filled-card');
                        card.style.cssText = 'width: 100%;';
                        
                        let valuesHtml = '';
                        if (module.values && module.values.length > 0) {
                            valuesHtml = '<md-divider style="margin: 16px 0;"></md-divider><div class="md-typescale-label-large" style="color: var(--md-sys-color-primary); margin-bottom: 12px;">设置</div>';
                            module.values.forEach(function(value, index) {
                                const valueId = 'value-' + module.name.replace(/\s+/g, '-') + '-' + value.name.replace(/\s+/g, '-');
                                
                                if (value.type === 'boolean') {
                                    valuesHtml += '<div class="value-row">';
                                    valuesHtml += '<span class="md-typescale-body-medium value-label">' + escapeHtml(value.name) + '</span>';
                                    valuesHtml += '<md-switch ' + (value.value.value ? 'selected' : '') + ' onclick="toggleValue(\'' + escapeHtml(module.name) + '\', \'' + escapeHtml(value.name) + '\')"></md-switch>';
                                    valuesHtml += '</div>';
                                } else if (value.type === 'number') {
                                    valuesHtml += '<div style="margin-bottom: 16px;">';
                                    valuesHtml += '<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">';
                                    valuesHtml += '<span class="md-typescale-body-medium value-label">' + escapeHtml(value.name) + '</span>';
                                    valuesHtml += '<span class="md-typescale-body-small" id="' + valueId + '-label" style="color: var(--md-sys-color-on-surface-variant);">' + value.value.value + '</span>';
                                    valuesHtml += '</div>';
                                    valuesHtml += '<md-slider id="' + valueId + '" value="' + value.value.value + '" min="' + value.value.min + '" max="' + value.value.max + '" step="' + value.value.step + '" style="width: 100%;" oninput="updateSliderValue(\'' + valueId + '\', this.value); setNumberValue(\'' + escapeHtml(module.name) + '\', \'' + escapeHtml(value.name) + '\', this.value)"></md-slider>';
                                    valuesHtml += '</div>';
                                } else if (value.type === 'mode') {
                                    valuesHtml += '<div class="value-row">';
                                    valuesHtml += '<span class="md-typescale-body-medium value-label">' + escapeHtml(value.name) + '</span>';
                                    valuesHtml += '<select class="value-select" onchange="setModeValue(\'' + escapeHtml(module.name) + '\', \'' + escapeHtml(value.name) + '\', this.value)">';
                                    value.value.options.forEach(function(option, idx) {
                                        valuesHtml += '<option value="' + idx + '" ' + (idx === value.value.value ? 'selected' : '') + '>' + escapeHtml(option) + '</option>';
                                    });
                                    valuesHtml += '</select>';
                                    valuesHtml += '</div>';
                                }
                            });
                        }
                        
                        card.innerHTML = 
                            '<div style="padding: 16px;">' +
                                '<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">' +
                                    '<span class="md-typescale-title-medium">' + escapeHtml(module.name) + '</span>' +
                                    '<md-assist-chip>' + escapeHtml(module.category) + '</md-assist-chip>' +
                                '</div>' +
                                '<div class="md-typescale-body-medium" style="color: var(--md-sys-color-on-surface-variant); margin-bottom: 16px;">' + escapeHtml(module.description) + '</div>' +
                                valuesHtml +
                                '<md-divider style="margin: 16px 0;"></md-divider>' +
                                '<div style="display: flex; justify-content: space-between; align-items: center;">' +
                                    '<div style="display: flex; align-items: center; gap: 8px;">' +
                                        '<md-icon style="color: ' + (module.enabled ? 'var(--md-sys-color-primary)' : 'var(--md-sys-color-error)') + ';">' + (module.enabled ? 'check_circle' : 'cancel') + '</md-icon>' +
                                        '<span class="md-typescale-body-medium" style="color: var(--md-sys-color-on-surface-variant);">' + (module.enabled ? '已启用' : '已禁用') + '</span>' +
                                    '</div>' +
                                    (module.enabled ? 
                                        '<md-outlined-button onclick="toggleModule(\'' + escapeHtml(module.name) + '\')">禁用</md-outlined-button>' :
                                        '<md-filled-button onclick="toggleModule(\'' + escapeHtml(module.name) + '\')">启用</md-filled-button>'
                                    ) +
                                '</div>' +
                            '</div>';
                        return card;
                    }
                    
                    async function toggleModule(moduleName) {
                        try {
                            const response = await fetch('/api/toggle', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/x-www-form-urlencoded',
                                },
                                body: 'name=' + encodeURIComponent(moduleName)
                            });
                            
                            const result = await response.json();
                            
                            if (result.success) {
                                showMessage(result.message, 'success');
                                loadModules();
                            } else {
                                showMessage(result.message, 'error');
                            }
                        } catch (error) {
                            showMessage('切换模块失败: ' + error.message, 'error');
                        }
                    }
                    
                    async function toggleValue(moduleName, valueName) {
                        try {
                            const response = await fetch('/api/value', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/x-www-form-urlencoded',
                                },
                                body: 'name=' + encodeURIComponent(moduleName) + '&value=' + encodeURIComponent(valueName) + '&data=true'
                            });
                            
                            const result = await response.json();
                            
                            if (result.success) {
                                showMessage(result.message, 'success');
                                loadModules();
                            } else {
                                showMessage(result.message, 'error');
                            }
                        } catch (error) {
                            showMessage('修改设置失败: ' + error.message, 'error');
                        }
                    }
                    
                    async function setNumberValue(moduleName, valueName, value) {
                        try {
                            const response = await fetch('/api/value', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/x-www-form-urlencoded',
                                },
                                body: 'name=' + encodeURIComponent(moduleName) + '&value=' + encodeURIComponent(valueName) + '&data=' + encodeURIComponent(value)
                            });
                            
                            const result = await response.json();
                            
                            if (result.success) {
                                showMessage(result.message, 'success');
                            } else {
                                showMessage(result.message, 'error');
                            }
                        } catch (error) {
                            showMessage('修改设置失败: ' + error.message, 'error');
                        }
                    }
                    
                    async function setModeValue(moduleName, valueName, value) {
                        try {
                            const response = await fetch('/api/value', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/x-www-form-urlencoded',
                                },
                                body: 'name=' + encodeURIComponent(moduleName) + '&value=' + encodeURIComponent(valueName) + '&data=' + encodeURIComponent(value)
                            });
                            
                            const result = await response.json();
                            
                            if (result.success) {
                                showMessage(result.message, 'success');
                            } else {
                                showMessage(result.message, 'error');
                            }
                        } catch (error) {
                            showMessage('修改设置失败: ' + error.message, 'error');
                        }
                    }
                    
                    function showMessage(message, type) {
                        const container = document.getElementById('message-container');
                        const snackbar = document.createElement('md-snackbar');
                        snackbar.setAttribute('open', '');
                        snackbar.innerHTML = '<md-icon slot="icon">' + (type === 'success' ? 'check_circle' : 'error') + '</md-icon>' + message;
                        container.appendChild(snackbar);
                        
                        setTimeout(() => {
                            snackbar.remove();
                        }, 3000);
                    }
                    
                    function escapeHtml(text) {
                        const div = document.createElement('div');
                        div.textContent = text;
                        return div.innerHTML;
                    }
                    
                    function updateSliderValue(valueId, value) {
                        const label = document.getElementById(valueId + '-label');
                        if (label) {
                            label.textContent = value;
                        }
                    }
                    
                    // 页面加载时自动加载模块
                    loadModules();
                    
                    // 每5秒自动刷新一次
                    setInterval(loadModules, 5000);
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}