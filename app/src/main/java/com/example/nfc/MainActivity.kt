package com.example.nfc

import android.Manifest
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.nfc.ui.theme.NFCTheme
import java.nio.charset.Charset

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var receivedContact by mutableStateOf<String?>(null)
    private var selectedContact by mutableStateOf<String?>(null)
    private var permissionGranted by mutableStateOf(false)

    private val selectContactLauncher = registerForActivityResult(ActivityResultContracts.PickContact()) { uri: Uri? ->
        uri?.let {
            selectedContact = getContactInfo(it)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        permissionGranted = isGranted
        if (isGranted) {
            selectContactLauncher.launch(null)
        } else {
            Toast.makeText(this,"Permission id required",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        permissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

        setContent {
            NFCTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NFCShareApp(nfcAdapter, receivedContact, selectedContact, permissionGranted, { selectContact() }, { shareContact() })
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        enableForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        disableForegroundDispatch()
    }

    private fun enableForegroundDispatch() {
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val filters = arrayOf<IntentFilter>()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, null)
    }

    private fun disableForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null && selectedContact != null) {
                writeNfcMessage(tag, selectedContact!!)
            }
        } else if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMessages != null) {
                val messages = arrayOfNulls<NdefMessage>(rawMessages.size)
                for (i in rawMessages.indices) {
                    messages[i] = rawMessages[i] as NdefMessage
                }
                if (messages.isNotEmpty()) {
                    receivedContact = String(messages[0]!!.records[0].payload, Charset.forName("UTF-8"))
                }
            }
        }
    }

    private fun writeNfcMessage(tag: Tag, message: String) {
        val ndefMessage = createNdefMessage(message)
        val ndef = Ndef.get(tag)
        ndef?.connect()
        ndef?.writeNdefMessage(ndefMessage)
        ndef?.close()
    }

    private fun createNdefMessage(content: String): NdefMessage {
        val mimeType = "application/vnd.com.example.nfcshare"
        val mimeBytes = mimeType.toByteArray(Charset.forName("US-ASCII"))
        val payload = content.toByteArray(Charset.forName("UTF-8"))
        val record = NdefRecord.createMime(mimeType, payload)
        return NdefMessage(arrayOf(record))
    }

    private fun getContactInfo(uri: Uri): String {
        val contentResolver: ContentResolver = contentResolver
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val nameIndex = cursor?.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
        val name = nameIndex?.let { cursor.getString(it) } ?: "Unknown"
        val idIndex = cursor?.getColumnIndex(ContactsContract.Contacts._ID)
        val id = idIndex?.let { cursor.getString(it) } ?: "Unknown"
        val phoneCursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?", arrayOf(id), null
        )
        phoneCursor?.moveToFirst()
        val phoneIndex = phoneCursor?.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val phoneNumber = phoneIndex?.let { phoneCursor.getString(it) } ?: "Unknown"
        cursor?.close()
        phoneCursor?.close()
        return "Contact Information: $name, $phoneNumber"
    }

    private fun selectContact() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            selectContactLauncher.launch(null)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun shareContact() {
        // This function will be called when the user clicks the share button
    }
}






@Composable
fun NFCShareApp(
    nfcAdapter: NfcAdapter?,
    receivedContact: String?,
    selectedContact: String?,
    permissionGranted: Boolean,
    onSelectContact: () -> Unit,
    onShareContact: () -> Unit
) {
    var nfcEnabled by remember { mutableStateOf(nfcAdapter?.isEnabled == true) }

    Column (
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (nfcEnabled) {
            ShareContactScreen(receivedContact, selectedContact, permissionGranted, onSelectContact, onShareContact)
        } else {
            Text("NFC is not enabled. Please enable it in your device settings.")
        }
    }
}

@Composable
fun ShareContactScreen(
    receivedContact: String?,
    selectedContact: String?,
    permissionGranted: Boolean,
    onSelectContact: () -> Unit,
    onShareContact: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (receivedContact != null) {
            Text("Received Contact:")
            Text(receivedContact, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 8.dp))
        } else if (selectedContact != null) {
            Text("Contact ready to share:")
            Text(selectedContact, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 8.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onShareContact) {
                Text("Share Contact")
            }
        } else {
            Text("NFC is enabled. Ready to share contact.")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSelectContact) {
            Text("Select Contact")
        }
        if (!permissionGranted) {
            Text("Permission to access contacts is required. Please grant permission to select a contact.")
        }
        Button(onClick = onSelectContact)
        {
            Text("Select Contact")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NFCTheme {
       val nfcAdapter = NfcAdapter.getDefaultAdapter(LocalContext.current)
        NFCShareApp(nfcAdapter, null, null, false, {}, {})
    }
}