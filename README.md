# Unified - University Messaging System

Unified is a secure messaging application designed specifically for university communities. The system enables students to communicate via direct messages and group chats, while also supporting the sharing of academic resources, announcements, and assignment-related discussions.

## Features

### Core Messaging Features
- **Direct Messages**: One-on-one messaging between students
- **Group Chats**: Multi-participant conversations for study groups and clubs
- **Course Channels**: Dedicated channels for specific courses with instructor controls
- **Message Types**: Support for text messages, file attachments, and course announcements
- **Read Status**: New messages are marked as unread; viewing marks them as read
- **Message Alignment**: Distinguish sender vs. recipient via left/right alignment in the UI

### Academic Features
- **Course-Specific Channels**: Classroom group chats for specific courses
- **Academic Announcements**: Special message type for course announcements with priority indicators
- **File Sharing**: Support for academic resource sharing with file size and type information
- **Student Profiles**: Personal information including Year of Graduation, Major, School/College

### Advanced Features
- **Message Search**: Search past messages in conversations by keyword
- **Chat History Export**: Export current chat history to plain text files for review or archiving
- **User Profiles**: Comprehensive user profiles with academic information
- **Secure Authentication**: Password hashing with salt for secure user authentication

## Architecture

### Core Classes

#### User Management
- **`User`** (Abstract): Base class containing fundamental user information (ID, username, full name, email, hashed password)
- **`Student`**: Extends User with student-specific functionality (student ID, enrolled courses)

#### Messaging System
- **`Message`** (Abstract): Base class for all message types with common properties
- **`TextMessage`**: Plain text messages
- **`FileMessage`**: File attachments with metadata (name, size, type, URL)
- **`AnnouncementMessage`**: Course announcements with priority and type indicators

#### Channel System
- **`Channel`** (Abstract): Base class for communication channels with participant and message management
- **`DirectMessageChannel`**: One-on-one messaging between two users
- **`GroupChatChannel`**: Multi-participant group chats with capacity limits
- **`CourseChannel`**: Course-specific channels with instructor controls

#### Utilities
- **`PasswordManager`**: Secure password hashing and verification using SHA-256 with salt
- **`App`**: Main application coordinator handling user interface and system coordination

### Design Patterns
- **Abstract Classes**: Used for User, Message, and Channel to provide common functionality
- **Inheritance**: Specific implementations extend abstract base classes
- **Encapsulation**: Private fields with public getters/setters for data protection
- **Polymorphism**: Different message and channel types handled through common interfaces

## Technology Stack

- **Language**: Java 11
- **Build Tool**: Maven
- **Cloud Platform**: Google Cloud Platform (GCP) - prepared for future deployment
- **Security**: SHA-256 password hashing with salt
- **Data Storage**: In-memory storage (ready for database integration)

## Project Structure

```
unified/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── unified/
│                   ├── model/
│                   │   ├── User.java
│                   │   ├── Student.java
│                   │   ├── Message.java
│                   │   ├── TextMessage.java
│                   │   ├── FileMessage.java
│                   │   ├── AnnouncementMessage.java
│                   │   ├── Channel.java
│                   │   ├── DirectMessageChannel.java
│                   │   ├── GroupChatChannel.java
│                   │   └── CourseChannel.java
│                   ├── util/
│                   │   └── PasswordManager.java
│                   ├── server/          # Future GCP server implementation
│                   ├── client/          # Future client implementation
│                   └── App.java
├── pom.xml
└── README.md
```

## Getting Started

### Prerequisites
-JDK 17+ and Maven 3.9+
-macOS / Linux / Windows
-Google Cloud SDK (gcloud) — only required for B1 cloud mode

### B1. Local CLI with cloud (talks to Firestore)
Requires IAM on our GCP project unified-468307: a maintainer must grant your Google account Cloud Datastore User (roles/datastore.user).

### Quick Start
```bash
# 1) Get the code and build
git clone <this-repo>
cd unified
mvn -DskipTests clean package

# 2) Set up Application Default Credentials (one time)
gcloud config set project unified-468307
gcloud auth application-default login
export FIRESTORE_PROJECT_ID=unified-468307

# 3) Run the CLI (use a free port to avoid collisions)
PORT=0 java -jar target/app.jar
# or
PORT=0 mvn -DskipTests exec:java -Dexec.mainClass=com.unified.App
```

### B2. Local CLI without credentials (offline, in-memory)
If you don’t have GCP access, run offline mode (no Firestore; data resets on restart).

### Quick Start
```bash
git clone <this-repo>
cd unified
mvn -DskipTests clean package
CLOUD_MODE=local PORT=0 java -jar target/app.jar
```
## Note: If your clone doesn’t include support for CLOUD_MODE=local yet, ask a maintainer or use B1.
### Troubleshooting
## Address already in use
Another process is using your port. Either auto-pick a free port or kill the process:
```bash
# Prefer: let the app pick a free port
PORT=0 java -jar target/app.jar

# Or find & kill the process holding 8080
lsof -nP -iTCP:8080 -sTCP:LISTEN
kill $(lsof -tiTCP:8080 -sTCP:LISTEN)
```
## ADC not found …
You’re in cloud mode without credentials. Follow B1 to set up ADC, or use B2 with CLOUD_MODE=local.

## Usage

### First Time Setup
1. Run the application
2. Choose option 2 (Register) from the login menu
3. Enter your student information (username, full name, email, password, student ID)
4. You'll be automatically logged in after registration

### Basic Operations
1. **Login**: Use your username and password to access the system
2. **View Messages**: See all your channels and unread message counts
3. **Send Messages**: Send text messages to any channel you're part of
4. **Create Channels**: Create direct messages, group chats, or course channels
5. **Join Channels**: Join existing channels you have access to
6. **Search Messages**: Find specific messages using keywords
7. **Export Chat History**: Save conversation history to text files
8. **Manage Profile**: View and edit your personal information

### Channel Types

#### Direct Messages
- One-on-one conversations
- Automatically created when you start a conversation with another user
- Both users are automatically added as participants

#### Group Chats
- Multi-participant conversations
- Configurable maximum participant limit
- Can be public or private (invite-only)
- Suitable for study groups, clubs, or general discussions

#### Course Channels
- Dedicated to specific academic courses
- Instructor controls (can restrict student messaging)
- Course-specific metadata (course code, name, semester, year)
- Special announcement message type for academic notifications

## Security Features

- **Password Hashing**: All passwords are hashed using SHA-256 with random salt
- **Input Validation**: Basic input validation for user data
- **Access Control**: Channel-based access control for messages
- **Data Encapsulation**: Private fields with controlled access through methods

## Future Enhancements

### Planned Features
- **Database Integration**: Persistent storage using Google Cloud Datastore
- **Real-time Messaging**: WebSocket support for live message updates
- **File Storage**: Google Cloud Storage integration for file attachments
- **Web Interface**: Modern web-based user interface
- **Mobile App**: Native mobile applications
- **Push Notifications**: Real-time notifications for new messages
- **Advanced Search**: Full-text search with filters
- **Message Encryption**: End-to-end encryption for sensitive communications

### GCP Integration
- **Google Cloud Datastore**: For persistent data storage
- **Google Cloud Storage**: For file attachment storage
- **Google Cloud Pub/Sub**: For real-time messaging
- **Google Cloud Functions**: For serverless operations
- **Google Cloud Run**: For containerized deployment

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please contact the development team or create an issue in the repository.

---

**Note**: This is a basic implementation focusing on the core classes and functionality. The system is designed to be easily extensible for future enhancements including database integration, web interfaces, and cloud deployment. 
