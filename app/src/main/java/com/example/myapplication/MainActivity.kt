package com.example.myapplication

import android.app.PendingIntent
import android.content.Intent
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MY_TAG"
        private const val ACTION_USB_PERMISSION = "com.example.myapplication.USB_PERMISSION"
        private const val BAUD_RATE = 115200
        private const val DATA_BITS = 8
        private const val COMMAND_TIMEOUT = 1000
    }

    private var driver: UsbSerialDriver? = null
    private var connection: UsbDeviceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                MainContent(
                    onCommandSend = { command ->
                        sendCommand(command)
                    },
                )
            }
        }
        setUpConnection()
    }

    private fun setUpConnection() {
        val manager = getSystemService(USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            return
        }

        driver = availableDrivers[0]
        connection = manager.openDevice(driver?.device)

        if (connection == null) {
            val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            manager.requestPermission(driver?.device, permissionIntent)
        } else {
            Log.d(TAG, "Connected")
        }
    }

    private fun sendCommand(command: String) {
        val port = driver?.ports?.firstOrNull() // Most devices have just one port (port 0)
        port?.open(connection)
        port?.setParameters(
            BAUD_RATE,
            DATA_BITS,
            UsbSerialPort.STOPBITS_1,
            UsbSerialPort.PARITY_NONE,
        )
        port?.write(command.toByteArray(), COMMAND_TIMEOUT)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    onCommandSend: (String) -> Unit,
) {
    var command by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = command,
                onValueChange = { newText -> command = newText },
            )
            Button(onClick = { onCommandSend(command) }) {
                Text(text = "Send")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        MainContent {
        }
    }
}
