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
- Java 11 or higher
- Maven 3.6 or higher

### Building the Project

### Quick Start
```bash
# Build the project
./build.sh

# Run the application
./run.sh

# Or run directly
java -cp build com.unified.App
```

### Manual Build
```bash
# Compile
cd src/main/java
javac -d ../../../build com/unified/util/PasswordManager.java com/unified/model/*.java com/unified/App.java

# Run
java -cp build com.unified.App
```
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