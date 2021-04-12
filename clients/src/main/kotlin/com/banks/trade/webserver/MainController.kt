package com.banks.trade.webserver

import com.template.flows.TradeInfo
import com.template.flows.TradeInitiator
import net.corda.core.internal.toX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.node.NodeInfo
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


val SERVICE_NAMES = listOf("Notary", "Network Map Service")

/**
 *  A Spring Boot Server API controller for interacting with the node via RPC.
 */

@RestController
@RequestMapping("/") // The paths for requests are relative to this base path.
class MainController(rpc: NodeRPCConnection) {

    private val proxy = rpc.proxy
    private val me = proxy.nodeInfo().legalIdentities.first().name

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    fun X500Name.toDisplayString(): String = BCStyle.INSTANCE.toString(this)

    /** Helpers for filtering the network map cache. */
    private fun isNotary(nodeInfo: NodeInfo) = proxy.notaryIdentities().any { nodeInfo.isLegalIdentity(it) }
    private fun isMe(nodeInfo: NodeInfo) = nodeInfo.legalIdentities.first().name == me
    private fun isNetworkMap(nodeInfo: NodeInfo) = nodeInfo.legalIdentities.single().name.organisation == "Network Map Service"


    /**
     * Returns the node's name.
     */
    @GetMapping(value = ["me"], produces = [APPLICATION_JSON_VALUE])
    fun whoami() = mapOf("me" to me.toString())

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GetMapping(value = ["peers"], produces = [APPLICATION_JSON_VALUE])
    fun getPeers(): Map<String, List<String>> {
        return mapOf("peers" to proxy.networkMapSnapshot()
                .filter { isNotary(it).not() && isMe(it).not() && isNetworkMap(it).not() }
                .map { it.legalIdentities.first().name.toX500Name().toDisplayString() })
    }

    @PostMapping(value = ["issueTrade"])
    fun issueTrade(@RequestBody tradeInfo: TradeInfo, @PathVariable receiver: String): ResponseEntity<String>
    {
        val matchingPasties = proxy.partiesFromName(receiver, false)
        print("-------------")
        print(receiver)
        print("-------------")
        return try {
            val result = proxy.startFlow(::TradeInitiator,tradeInfo, matchingPasties.first()).returnValue.get()
            ResponseEntity.status(HttpStatus.CREATED).body("Issue Insurance ${result.id} Completed")

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }
}
