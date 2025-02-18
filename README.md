# EchoTrail - An App to Connect Emotions and Places

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.webp" width="90" height="90" alt="EchoTrail logo" align="left">

**EchoTrail** is an Android app that allows users to leave and discover messages (written) tied to real-world locations. Explore an interactive map, read heartfelt notes, and share your thoughts when visiting meaningful places. The app leverages `Firebase` to ensure scalable, _real-time management_ of users and data.

## Key Features
- **Leave written notes** at locations you visit.
- **Explore an interactive map** to discover messages left by others.
- **Session Manager** to handle user authentication.
- **Public and private modes**: Send messages visible to everyone or dedicated to a specific person.
- **Real-time updates**: Notes are updated in real-time across all devices.
- **User-friendly interface**: Material Design components for a seamless user experience.
- **Multi-language support**: English and Italian languages are supported.

---

## Requirements
- Android Studio or Intellij IDEA
- A Firebase account

---

## Firebase Configuration
### Steps to Configure Firebase:
1. **Create a Firebase project:**
    - Go to the [Firebase Console](https://console.firebase.google.com/) for guide.
   - Add an Android app to the Firebase project.

2. **Integrate Firebase into the project:**
    - Copy the `google-services.json` file into the `app/` directory of your Android project.

3. **Sync the project:**
    - Open Android Studio or Intellij IDEA.
    - Sync with Gradle to ensure all dependencies are correctly configured.

---

## How to Build and Run the App
1. Clone the repository:
   ```bash
   git clone https://github.com/Peppe289/EchoTrail.git
   cd EchoTrail
   ```

2. Import the project into Android Studio.
3. Configure Firebase following the steps above.
4. Run the app on a physical Android device or emulator with Android 12.0 (API level 31) or higher.

---

## Project Library Dependencies
- **Firebase**: Real-time database and authentication.
- **OSMDroid**: OpenStreetMap library for Android.
- **Material Components**: Material Design components for Android.
- **OkHttp**: HTTP client for Android.
- **RecyclerView**: List and grid views for Android.

---

## Contributions
Contributions, suggestions, and improvements are always welcome! Feel free to open a pull request or create an issue to report bugs or suggest new features.

---

## License
This project is licensed under the GPL-3.0 License. See the LICENSE file for details.
