package com.cachecache.game.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Radar Bluetooth Low Energy : fonctionne de téléphone à téléphone, sans réseau, et bien plus
 * précis que le GPS à courte portée (et à l'intérieur). Deux balises :
 *  - fantôme : "PS" + son identifiant court → Pacman mesure la force du signal (RSSI) ;
 *  - Pacman  : "PM" + identifiants des fantômes trouvés → un fantôme apprend qu'il est
 *    trouvé (et que Pacman est tout près) même si le réseau est coupé.
 * Un RSSI proche de -50 dBm = collé au téléphone ; vers -95 dBm = une vingtaine de mètres.
 */
class BleProximity(context: Context) {

    companion object {
        private const val MANUFACTURER_ID = 0xFFFF // Identifiant réservé aux tests / usage privé
        val MAGIC_GHOST = byteArrayOf(0x50, 0x53)  // "PS"
        val MAGIC_PACMAN = byteArrayOf(0x50, 0x4D) // "PM"

        // Une annonce BLE fait 31 octets : place pour 5 identifiants de 4 octets
        private const val MAX_FOUND_IN_BEACON = 5

        /** Identifiant court (4 octets en hexadécimal) d'un joueur. */
        fun shortId(uid: String): String {
            val h = uid.hashCode()
            return byteArrayOf((h shr 24).toByte(), (h shr 16).toByte(), (h shr 8).toByte(), h.toByte()).toHex()
        }

        fun ghostBeacon(uid: String): ByteArray = MAGIC_GHOST + hexToBytes(shortId(uid))

        fun pacmanBeacon(foundGhostIds: List<String>): ByteArray =
            MAGIC_PACMAN + foundGhostIds.takeLast(MAX_FOUND_IN_BEACON).fold(ByteArray(0)) { acc, id ->
                acc + hexToBytes(shortId(id))
            }

        /** Identifiants courts contenus dans une balise Pacman. */
        fun shortIdsIn(payloadHex: String): List<String> = payloadHex.chunked(8).filter { it.length == 8 }

        private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }

        private fun hexToBytes(hex: String) =
            ByteArray(hex.length / 2) { i -> hex.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
    }

    /** Balise captée : contenu après le préfixe (en hexadécimal) et force du signal. */
    data class Sighting(val payloadHex: String, val rssi: Int)

    private val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter
    private var advertiseCallback: AdvertiseCallback? = null
    private var advertisedPayload: ByteArray? = null

    val isSupported: Boolean get() = adapter != null
    val isEnabled: Boolean get() = adapter?.isEnabled == true
    val isAdvertising: Boolean get() = advertiseCallback != null

    /**
     * Émet (ou met à jour) la balise de ce téléphone.
     * Renvoie false si l'appareil ne sait pas émettre en Bluetooth.
     */
    @SuppressLint("MissingPermission")
    fun advertise(payload: ByteArray, onFailure: () -> Unit): Boolean {
        if (advertiseCallback != null && advertisedPayload.contentEquals(payload)) return true
        stopAdvertising()
        val advertiser = adapter?.bluetoothLeAdvertiser ?: return false
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addManufacturerData(MANUFACTURER_ID, payload)
            .build()
        val callback = object : AdvertiseCallback() {
            override fun onStartFailure(errorCode: Int) {
                if (advertiseCallback === this) {
                    advertiseCallback = null
                    advertisedPayload = null
                }
                onFailure()
            }
        }
        advertiseCallback = callback
        advertisedPayload = payload
        return try {
            advertiser.startAdvertising(settings, data, callback)
            true
        } catch (e: SecurityException) {
            advertiseCallback = null
            advertisedPayload = null
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        val callback = advertiseCallback ?: return
        advertiseCallback = null
        advertisedPayload = null
        try {
            adapter?.bluetoothLeAdvertiser?.stopAdvertising(callback)
        } catch (_: Exception) {
            // Bluetooth coupé entre-temps : rien à arrêter
        }
    }

    /** Flux des balises captées commençant par `magic` (MAGIC_GHOST ou MAGIC_PACMAN). */
    @SuppressLint("MissingPermission")
    fun scan(magic: ByteArray): Flow<Sighting> = callbackFlow {
        val scanner = adapter?.bluetoothLeScanner
        if (scanner == null) {
            close(IllegalStateException("Bluetooth indisponible"))
            return@callbackFlow
        }
        val filter = ScanFilter.Builder()
            .setManufacturerData(MANUFACTURER_ID, magic, ByteArray(magic.size) { -1 })
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val bytes = result.scanRecord?.getManufacturerSpecificData(MANUFACTURER_ID) ?: return
                if (bytes.size < magic.size || !bytes.copyOfRange(0, magic.size).contentEquals(magic)) return
                trySend(Sighting(bytes.copyOfRange(magic.size, bytes.size).toHex(), result.rssi))
            }

            override fun onScanFailed(errorCode: Int) {
                close(IllegalStateException("Échec du scan Bluetooth ($errorCode)"))
            }
        }
        scanner.startScan(listOf(filter), settings, callback)
        awaitClose {
            try {
                scanner.stopScan(callback)
            } catch (_: Exception) {
                // Bluetooth coupé entre-temps
            }
        }
    }
}
