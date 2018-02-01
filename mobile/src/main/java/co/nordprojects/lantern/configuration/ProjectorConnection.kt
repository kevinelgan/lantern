package co.nordprojects.lantern.configuration

import android.util.Log
import co.nordprojects.lantern.App
import co.nordprojects.lantern.shared.*
import org.json.JSONObject

/**
 * Created by Michael Colville on 30/01/2018.
 */
class ProjectorConnection(val transport: ConfigurationConnectionTransport) {

    var projectorConfig: ProjectorConfiguration = ProjectorConfiguration()
    companion object {
        val TAG: String = ProjectorConnection::class.java.simpleName
    }

    init {
        transport.onMessageReceived = { m -> onMessageReceived(m) }
    }

    fun onConnectionAccepted() {
        Log.i(TAG, "Connected to ${transport.endpointId}")
    }

    private fun onMessageReceived(message: ConfigurationMessage) {
        Log.d(TAG, "Received message from ${transport.endpointId}. $message")

        when (message.type) {
            ConfigurationMessage.Type.StateUpdate -> {
                projectorConfig.updateWithJSON(message.body!!)
            }
            ConfigurationMessage.Type.AvailableChannels -> {
                projectorConfig.updateAvailableChannelsWithJSON(message.body!!)
            }
            else -> { throw IllegalArgumentException("Can't handle message type ${message.type}") }
        }
    }

    fun onDisconnected() {
        Log.i(TAG, "Disconnected from ${transport.endpointId}")

        // TODO - either reconnect or destroy projector config
        // TODO - OR may not be needed as this object is destroyed on disconnect
    }

    fun sendSetPlane(direction: Direction, configuration: ChannelConfiguration) {

        val body = configuration.toJson(includingSecrets = true)
        val arguments = JSONObject().apply {
            put("plane", direction.jsonName)
        }
        val message = ConfigurationMessage(ConfigurationMessage.Type.SetPlane, arguments, body)
        transport.sendMessage(message)
    }
}