# OpenInventory for Android

![App Icon](./app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp)

OpenInventory is an offline-first Point of Sale (POS) and inventory management application designed for small to medium-sized businesses. Originally a desktop application, this project brings the full feature set to the Android platform, supporting both phones and tablets.

The application is built with a modern, robust, and scalable architecture, focusing on performance and a clean, intuitive user experience. It's designed to be a reliable tool for business owners, especially in environments where consistent internet access may not be available.

## Features

- **Full POS Functionality**: A fast and responsive interface for processing sales.
- **Inventory Management**: Easily add, edit, and track product stock levels. View active and archived products.
- **Barcode Scanning**: Use the device's camera to quickly scan product barcodes and add them to the cart. The scanner is based on the GMS-free ZXing library, ensuring it works on all Android devices.
- **Sales Reporting**: Generate detailed sales reports for any date range, with expandable views to see individual items in each transaction.
- **Saved Carts**: Save a customer's cart to be restored and completed later. A badge indicates the number of pending carts.
- **Customer Management**: Save customer information for repeat business and track sales history.
- **Optional Security**: The app can be used in a simple, single-user mode without a password. Users can opt-in to enable security at any time, which activates a multi-user system with role-based access.
- **Role-Based Access Control (RBAC)**:
    - **Staff**: Can process sales.
    - **Admin**: Can manage products and view reports.
    - **Superadmin**: Has full control, including managing other user accounts.
- **Responsive UI**: The layout automatically adapts for a great experience on both phones and tablets.
- **Offline First**: All data is stored locally in an SQLite database, ensuring the app is fully functional without an internet connection.

## Technology Stack

This application is built using the latest technologies for modern Android development.

- **UI**:
    - **Jetpack Compose**: The entire UI is built with Compose.
    - **Material Design 3**: Implements the latest Material You design principles.
- **Architecture**:
    - **MVVM (Model-View-ViewModel)**
    - **Repository Pattern**
- **Language**:
    - **100% Kotlin**
- **Asynchronous Programming**:
    - **Kotlin Coroutines & Flow**: Used throughout the app for managing background threads and handling streams of data from the database for a non-blocking UI.
- **Database**:
    - **SQLDelight 2.x**: A type-safe SQL database library that generates Kotlin APIs directly from your SQL statements. It was chosen over Room for its powerful handling of complex schemas, triggers, and multi-database setups.
    - **SQLite**: The underlying database engine.
- **Dependency Injection**:
    - **Hilt**
- **Image Loading**:
    - **Coil 3**
- **Barcode Scanning**:
    - **ZXing (via zxing-android-embedded)**
- **Data Serialization**:
    - **kotlinx.serialization**: The official Kotlin library for converting data objects (like the cart) to and from JSON for database storage.

## Getting Started

### Prerequisites

- Android Studio (latest stable version recommended)
- An Android device or emulator running API level 24 (Android 7.0) or higher.

### Building and Running

1.  **Clone the repository**:
    ```sh
    git clone https://github.com/clemoseitano/OpenInventory-Android.git
    ```
2.  **Open in Android Studio**:
    - Open Android Studio.
    - Select "Open an existing Project".
    - Navigate to and select the cloned project directory.
3.  **Build the Project**:
    - Android Studio will automatically sync the Gradle files and download the necessary dependencies.
    - Click the "Run 'app'" button (the green play icon) to build and install the application on your selected device or emulator.

### Initial Setup (Optional)

On the first launch, the application will start in **non-authenticated mode**. All features except user management will be available.

To enable security and multi-user features:
1.  Navigate to the **Settings** screen from the navigation drawer.
2.  Tap on **"Enable Security & Multi-User"**.
3.  You will be prompted to create the first **superuser** account. This is a one-time migration process that secures your existing data.
4.  After creating the account, the app will prompt you to restart.
5.  Upon restarting, you will be presented with the login screen.

## Contributing

Contributions are welcome! If you'd like to contribute, please fork the repository and create a pull request. You can also open an issue with the "enhancement" tag to suggest new features.

---