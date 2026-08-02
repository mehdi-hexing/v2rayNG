package com.v2ray.ang.fmt

import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.extension.idnHost
import com.v2ray.ang.extension.isNotNullEmpty
import com.v2ray.ang.util.HttpUtil
import com.v2ray.ang.util.Utils
import java.net.URI

object HttpFmt : FmtBase() {
    fun parse(str: String): ProfileItem? {
        val config = ProfileItem.create(EConfigType.HTTP)

        val uri = URI(Utils.fixIllegalUrl(str))
        if (uri.idnHost.isEmpty()) return null
        if (uri.port <= 0) return null

        config.remarks = Utils.decodeURIComponent(uri.fragment.orEmpty()).let { it.ifEmpty { "none" } }
        config.server = uri.idnHost
        config.serverPort = uri.port.toString()

        if (!uri.userInfo.isNullOrEmpty()) {
            val result = if (uri.userInfo.contains(":")) {
                uri.userInfo.split(":", limit = 2)
            } else {
                Utils.decode(uri.userInfo).split(":", limit = 2)
            }
            if (result.count() == 2) {
                config.username = result.first()
                config.password = result.last()
            }
        }

        return config
    }

    fun toUri(config: ProfileItem): String {
        val userInfo = if (config.username.isNotNullEmpty()) {
            val pw = "${config.username}:${config.password}"
            "${Utils.encode(pw, true)}@"
        } else {
            ""
        }

        val host = Utils.getIpv6Address(HttpUtil.toIdnDomain(config.server.orEmpty()))
        val port = config.serverPort.orEmpty()
        val remarks = Utils.encodeURIComponent(config.remarks)

        return "$userInfo$host:$port#$remarks"
    }
}
