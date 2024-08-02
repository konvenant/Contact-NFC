# NFC Contact Sharing App

This is a simple Android application built using Jetpack Compose that allows users to share contact information between two NFC-enabled devices. The app checks if NFC is enabled and facilitates contact sharing by tapping the devices together.

## Features

- Select a contact from the device's contact list.
- Share the selected contact via NFC.
- Receive and display the shared contact on another NFC-enabled device.
- Handle permission requests for accessing contacts.
- User-friendly interface with clear instructions.

## Prerequisites

- Android device with NFC capability.
- Android Studio installed on your development machine.
- A minimum SDK version of 21 (Lollipop) is required.

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/your-username/nfc-contact-sharing-app.git
cd nfc-contact-sharing-app
```

### Open in Android Studio

1. Open Android Studio.
2. Select `File > Open`.
3. Navigate to the project directory and select it.
4. Let Android Studio import and build the project.

### Running the App

1. Connect your NFC-enabled Android device to your development machine.
2. Select the device in Android Studio.
3. Click the "Run" button or press `Shift + F10`.

## Usage

1. **Enable NFC**: Ensure that NFC is enabled on both devices.
2. **Select Contact**:
   - Open the app.
   - Click on the "Select Contact" button.
   - If prompted, grant permission to access contacts.
   - Select a contact from the contact picker.
3. **Share Contact**:
   - The selected contact will be displayed as "Contact ready to share".
   - Click the "Share Contact" button.
   - Bring the devices close together to share the contact information.
4. **Receive Contact**:
   - On the receiving device, the contact information will be displayed once received.

## Code Overview

### MainActivity.kt

- **Initialization**: Sets up NFC adapter and checks for contact permission.
- **Foreground Dispatch**: Enables and disables foreground dispatch for NFC.
- **Intent Handling**: Processes NFC intents to read or write contact information.
- **Contact Selection**: Handles contact selection and permission requests.

### Composables

- **NFCShareApp**: Main composable function that sets up the UI and handles state.
- **ShareContactScreen**: UI screen for displaying contact information and sharing options.

### Permissions

- **AndroidManifest.xml**: Includes necessary permissions for NFC and contact access.

```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-feature android:name="android.hardware.nfc" android:required="true" />
```

## Screenshots

![Select Contact](screenshots/select_contact.png)
![Share Contact](screenshots/share_contact.png)
![Receive Contact](screenshots/receive_contact.png)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Thanks to the Jetpack Compose team for creating a modern UI toolkit for Android.
- Inspired by various NFC-related tutorials and documentation.

---

Feel free to customize this README further to fit your project details and repository structure. Be sure to include screenshots and a LICENSE file in your project for completeness.
