package UserRelated;


import DatabaseRelated.DatabaseConnection;
import MessagesGUI.*;
import CommentsGUI.*;
import NotificationRelated.Notification;
import Posts.ActivityPost;
import Posts.LessonPost;
import Posts.Post;
import Posts.StudyPost;
import SignupAndLogin.LoginFrame;
import com.mysql.cj.log.Log;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.xdevapi.Type;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public abstract class User{
    private String[] studyTopics;
    protected DatabaseConnection databaseConnection;
    private int id;
    private String nameAndSurname;
    private String password;
    private String email;
    private boolean isnew;
    private String department;
    private String gender;
    private String dateOfBirth;
    private byte[] profilePhoto;
    private byte[] backgroundPhoto;
    private String biography;
    private ArrayList<String> researchInterests;
    private ArrayList<StudyPost> studyPostCollection;
    private ArrayList<Notification> notificationCollection;
    private ArrayList<MessageConnection> messageConnections;

    public User(String nameAndSurname, String email, int id, String gender, String department, String password, String dateOfBirth, byte[] profilePhoto, byte[] backGroundPhoto ,boolean isItNew) {
        databaseConnection = new DatabaseConnection();
        studyPostCollection = new ArrayList<>();
        researchInterests = new ArrayList<>();
        notificationCollection = new ArrayList<>();
        messageConnections = new ArrayList<>();
        this.isnew = isItNew;
        setName(nameAndSurname);
        setEmail(email);
        setId(id);
        setGender(gender);
        setDepartment(department);
        setPassword(password);
        setDateOfBirth(dateOfBirth);
        if (isItNew) {
            setDefaultPhotos();
            if (profilePhoto != null)
                setProfilePhoto(profilePhoto,true);
            if (backGroundPhoto != null)
                setBackgroundPhoto(backGroundPhoto,true);
        }
        else {
            setProfilePhoto(pullTheProfilePhotoFromDB(id),false);
            setBackgroundPhoto(pullTheBackgroundPhotoFromDB(id),false);
        }

    }

    public ArrayList<StudyPost> getStudyPostCollection() {
        return studyPostCollection;
    }

    public void setDefaultPhotos() {
            BufferedImage bi = null;
            File f = new File("src/Other/DefaultProfilePictures/Tatice-Cristal-Intense-Java.64.png");
            try {
                bi = ImageIO.read(f);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                ImageIO.write(bi,"png",os);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            byte[] bytes = os.toByteArray();
            setProfilePhoto(bytes,true);

            // Adding Background Photo (photo to byte[])
            BufferedImage ib = null;
            try {
                ib = ImageIO.read( new File("src/Other/DefaultProfilePictures/trava-pole-polya-kholmy-nebo-oblako-oblaka.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ByteArrayOutputStream so = new ByteArrayOutputStream();
            try {
                ImageIO.write(ib,"png",so);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            byte[] bytes1 = so.toByteArray();
            setBackgroundPhoto(bytes1,true);


    }

    public void addMessageConnection(MessageConnection connection){
        messageConnections.add(connection);
    }

    public ArrayList<MessageConnection> getMessageConnections() {
        return messageConnections;
    }

    public void addStudyPost(StudyPost studyPost) {
        studyPostCollection.add(studyPost);
        if (!LoginFrame.isTrial)
            addToStudiesTable(studyPost);
    }

    public int generateStudyPostId() {
        int count = 0;
        DatabaseConnection databaseConnection = new DatabaseConnection();
        String tableName = "" + getId() + "StudiesTable";
        String selectQuery = "SELECT * FROM " + tableName;
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                count++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return nameAndSurname;
    }

    public void setName(String name) {
        this.nameAndSurname = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBiography() {
        return biography;
    }
    public void setBiography(String biography) {
        this.biography = biography;
    }

    public byte[] getProfilePhoto() {
        if (!isnew)
            return pullTheProfilePhotoFromDB(getId());
        return profilePhoto;
    }

    public byte[] getBackgroundPhoto() {
        if (!isnew)
            return pullTheBackgroundPhotoFromDB(getId());
        return backgroundPhoto;
    }

    public void setBackgroundPhoto(byte[] backGroundPhoto, boolean isItNew) {
        this.backgroundPhoto = backGroundPhoto;
        if (isItNew && !LoginFrame.isTrial)
            addBackgroundPhotoToUserInformationTable(backGroundPhoto);
    }

    public void setProfilePhoto(byte[] profilePhoto , boolean isItNew) {
        this.profilePhoto = profilePhoto;
        if(isItNew && !LoginFrame.isTrial)
            addProfilePhotoToUserInformationTable(profilePhoto);
    }
    public ArrayList<String> getResearchInterests() {
        return researchInterests;
    }

    // METHODS
    /**
     * Adds researchInterest into researchInterests ArrayList.
     * Will be used by both Student and Faculty Member
     *
     *  researchInterests indicates research interest of
     *                          a student or faculty member
     */
    public void addResearchInterest(String researchInterest) {
        researchInterests.add(researchInterest);
    }

    /**
     * This method will be used in both Student and FacultyMember classes.
     * The method receives a StudyPost object and adds it to the studyPostCollection
     *
     * @param studyPost is the study post that both student and faculty member can
     *                  share
     */
    public void postStudyPost(StudyPost studyPost) {
        studyPostCollection.add(studyPost);
    }

    /**
     * This method will first check if the passed StudyPost exists in the
     * studyPostCollection.
     * If it exists, it will delete the respective notification from the
     * studyPostCollection
     *
     * @param studyPost is the post that will be deleted in the collection
     */
    public void deleteStudyPost(StudyPost studyPost) {

        // Checking Process
        boolean isExist = false;
        for (StudyPost stuPost : studyPostCollection) {
            if (stuPost.equals(studyPost)) {
                isExist = true;
            }
        }

        if (isExist)
            studyPostCollection.remove(studyPost);
    }

    /**
     * The method receives a Notification object and adds it to the
     * notificationCollection
     * and the Notification Table of the SQL Database
     *
     * @param notification
     */
    public void addNotification(Notification notification) {
        notificationCollection.add(notification);
    }

    /**
     * This method will first check if the passed Notification exists and if it
     * exists,
     * it will delete the respective notification from the NotificationCollection
     * and the Notifications Table of the SQL Database
     *
     * @param notification
     */
    public void deleteNotification(Notification notification) {

        // Checking Process
        boolean isExist = false;
        for (Notification not : notificationCollection) {
            if (not.equals(notification)) {
                isExist = true;
            }
        }

        if (isExist)
            notificationCollection.remove(notification);

        // Cannot do the second part again
    }

    /**
     * This method will receive a Comment and adds it to the respective
     * Post’s commentCollection array and Comment Table in SQL Database
     *
     * @param post
     * @param comment
     */
    public void commentToPost(Post post, Comment comment) {
        for (StudyPost stuPost : studyPostCollection) {
            if (stuPost.equals(post)) {
                //post.getCommentCollection().addCommnet(comment);
            }
        }

        // Cannot do second part;
    }

    /**
     * @see Comment class
     * @param mainComment       // Cannot finished
     * @param comment
     */
    public void commentToComment(Comment mainComment, Comment comment) {
        //mainComment.getCommentCollection().addCommnet(comment);
    }

    /**
     * @see Comment class
     * @param comment
     */
    public void likeComment(Comment comment) {
        comment.incrementLikeCount(this);
    }


    // DatabaseHandler Methods
    public boolean createStudiesTable() {
        databaseConnection = new DatabaseConnection();
        try (Connection connection = databaseConnection.getConnection()) {
            String tableName = "" + getId() + "StudiesTable";
            if (connection != null) {
                String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                        + "postId INT PRIMARY KEY AUTO_INCREMENT,"
                        + "sender VARCHAR(50) NOT NULL,"
                        + "author VARCHAR(50) NOT NULL,"
                        + "postHeading VARCHAR(150) NULL,"
                        + "postDescription  VARCHAR(250) NOT NULL,"
                        + "postDate  VARCHAR(250) NOT NULL,"
                        + "file LONGBLOB NULL,"
                        + "Topic1 VARCHAR(50)  NULL,"
                        + "Topic2 VARCHAR(50)  NULL,"
                        + "Topic3 VARCHAR(50)  NULL,"
                        + "Topic4 VARCHAR(50)  NULL,"
                        + "Topic5 VARCHAR(50)  NULL"
                        + ");";

                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(createTableQuery);
                    System.out.println("Study Table created successfully.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        databaseConnection.closeConnection();
        return false;

    }
    public boolean addToStudiesTable(StudyPost studyPost) {
        String[] topicsToBeAdded = studyPost.getTopicCollection();
        int numberOfPostTopics = topicsToBeAdded.length;
        databaseConnection = new DatabaseConnection();
        try (Connection connection = databaseConnection.getConnection()) {
            String tableName = "" + getId() + "StudiesTable";
            byte[] imageBytes = null;
            if (studyPost.hasFile()){
                 imageBytes= studyPost.getStudyFile();
            }
            if (connection != null) {
                String insertTableQuery = "INSERT INTO " + tableName + " (postId, sender, author, postHeading, postDescription, postDate, file, ";

                for(int i=0; i<numberOfPostTopics; i++){
                    int topicNo = i+1;
                    insertTableQuery += "Topic" + topicNo + ", ";
                }
                insertTableQuery = insertTableQuery.substring(0, insertTableQuery.length() - 2);
                insertTableQuery += ") VALUES (?, ?, ?, ?, ?, ?, ?, ";

                for(int i=0; i<numberOfPostTopics; i++){
                    insertTableQuery += "?, ";
                }
                insertTableQuery = insertTableQuery.substring(0, insertTableQuery.length() - 2);
                insertTableQuery += ")";

                try (PreparedStatement preparedStatement = connection.prepareStatement(insertTableQuery)) {
                    //Get methods of the StudyPost class will be inserted accordingly
                    preparedStatement.setInt(1, studyPost.getPostID());
                    preparedStatement.setString(2, studyPost.getSender().getName());
                    preparedStatement.setString(3, studyPost.getAuthor());
                    preparedStatement.setString(4, studyPost.getStudyPostHeading());
                    preparedStatement.setString(5, studyPost.getPostDescription());
                    preparedStatement.setString(6, studyPost.getDateOfPost());
                    if ( imageBytes != null) {
                        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                        preparedStatement.setBinaryStream(7, bis, imageBytes.length);
                    }else {
                        preparedStatement.setNull(7, 0);
                    }
                    for(int i=0; i<numberOfPostTopics; i++){
                        int columnNumber = i+8;
                        String topicToAdd = topicsToBeAdded[i];
                        preparedStatement.setString(columnNumber, topicToAdd);
                    }

                    int rowsAffected = preparedStatement.executeUpdate();

                    return rowsAffected > 0;
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addBackgroundPhotoToUserInformationTable(byte[] profilePhoto) {
        databaseConnection = new DatabaseConnection();
        try (Connection connection = databaseConnection.getConnection();) {
            // Replace with your image byte array
            byte[] imageBytes = profilePhoto;

            String sql = "UPDATE userInformationTable SET backgroundPhoto = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            preparedStatement.setBinaryStream(1, bis, imageBytes.length);
            preparedStatement.setInt(2, this.getId());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean removeFromStudiesTable(StudyPost studyPost) {
        databaseConnection = new DatabaseConnection();
        String tableName = "" + getId() + "StudiesTable";
        String deleteQuery = "DELETE FROM " + tableName + " WHERE postId = ?";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {

            preparedStatement.setInt(1, studyPost.getPostID());

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            System.out.println("Post deleted successfully");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public StudyPost pullStudyPostFromDB(int userId, int studyPostID) {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        StudyPost studyPost = null;
        String[] topicCollection = new String[5];
        String tableName = "" + userId + "StudiesTable";
        String insertQuery = "SELECT * FROM " + tableName + " WHERE postId=?;";

        try (Connection connection = databaseConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

            preparedStatement.setInt(1, studyPostID);

            ResultSet resultSetOfUser = preparedStatement.executeQuery();
            if (resultSetOfUser.next()) {
                int postId = resultSetOfUser.getInt("postId");
                String senderName = resultSetOfUser.getString("sender");
                String author = resultSetOfUser.getString("author");
                String postHeading = resultSetOfUser.getString("postHeading");
                String postDesctiption = resultSetOfUser.getString("postDescription");
                String fileString = resultSetOfUser.getString("file");
                String postDate = resultSetOfUser.getString("postDate");

                if (resultSetOfUser.getString("Topic1") != null) {
                    topicCollection[0] = resultSetOfUser.getString("Topic1");
                    if (resultSetOfUser.getString("Topic2") != null) {
                        topicCollection[1] = resultSetOfUser.getString("Topic2");
                        if (resultSetOfUser.getString("Topic3") != null) {
                            topicCollection[2] = resultSetOfUser.getString("Topic3");
                            if (resultSetOfUser.getString("Topic4") != null) {
                                topicCollection[3] = resultSetOfUser.getString("Topic4");
                                if (resultSetOfUser.getString("Topic5") != null) {
                                    topicCollection[4] = resultSetOfUser.getString("Topic5");
                                }
                            }
                        }
                    }
                }
                User u = new Student(senderName,null,0,null,null,null,null, null, null,false);
                studyPost = new StudyPost(postId, u, author, postHeading, postDesctiption, null, postDate, topicCollection,false);
            } else {
                return null;
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return studyPost;
    }

    public boolean createNotificationsTable() {
        databaseConnection = new DatabaseConnection();
        try (Connection connection = databaseConnection.getConnection()) {
            String tableName = "" + getId() + "NotificationsTable";
            if (connection != null) {
                String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                        + "notificationId INT PRIMARY KEY AUTO_INCREMENT,"
                        + "senderId INT NOT NULL,"
                        + "receiverId INT NOT NULL,"
                        + "commentContent VARCHAR(350) NOT NULL,"
                        + "isRead TINYINT(1) DEFAULT 0,"
                        + "dateTime  VARCHAR(250) NOT NULL"
                        + ");";

                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(createTableQuery);
                    System.out.println("Notification Table created successfully.");
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        databaseConnection.closeConnection();
        return false;
    }

    //Right
    public boolean createMessageHistory(int connectionId) {
        databaseConnection = new DatabaseConnection();
        try (Connection connection = databaseConnection.getConnection()) {
            String tableName = "" + connectionId + "MessageHistory";
            if (connection != null) {
                String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                        + "senderId INT NOT NULL,"
                        + "receiverId INT NOT NULL,"
                        + "content VARCHAR(250) NOT NULL,"
                        + "date VARCHAR(50) NOT NULL"
                        + ");";

                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(createTableQuery);
                    System.out.println("Message History created successfully.");
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        databaseConnection.closeConnection();
        return false;
    }

    //Right
    public boolean insertToMessageHistoryTable(int connectionId, Message message) {
        databaseConnection = new DatabaseConnection();
        try (Connection connection = databaseConnection.getConnection()) {
            String tableName = "" + connectionId + "MessageHistory";
            if (connection != null) {
                String insertQuery = "INSERT INTO " + tableName + " (senderId, receiverId, content, date) VALUES (?, ?, ?, ?)";

                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setInt(1, message.getSender().getId());
                    preparedStatement.setInt(2, message.getReceiver().getId());
                    preparedStatement.setString(3, message.getContent());
                    preparedStatement.setString(4, message.getTime().toString());

                    preparedStatement.executeUpdate();
                    System.out.println("Inserted to message connection successfuly");
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        databaseConnection.closeConnection();
        return false;
    }

    public ArrayList<Message> pullMessageHistoryFromDB(int connectionId) {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        String tableName = "" + connectionId + "MessageHistory";
        String selectQuery = "SELECT * FROM " + tableName;

        ArrayList<Message> messages = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int senderId = resultSet.getInt("senderId");
                User sender = databaseConnection.pullUserByIdFromDB(senderId);
                int receiverId = resultSet.getInt("receiverId");
                User receiver = databaseConnection.pullUserByIdFromDB(receiverId);
                String content = resultSet.getString("content");
                String date = resultSet.getString("date");

                Message m1 = new Message(sender,receiver,content,date);
                messages.add(m1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return messages;
    }

    //Left
    public boolean createMessagesConnectionTable() {
        databaseConnection = new DatabaseConnection();
        try (Connection connection = databaseConnection.getConnection()) {
            String tableName = "UserMessageConnectionTable";
            if (connection != null) {
                String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                        + "connectionId INT PRIMARY KEY AUTO_INCREMENT,"
                        + "id1 INT NOT NULL,"
                        + "id2 INT NOT NULL,"
                        + "port INT NOT NULL"
                        + ");";

                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(createTableQuery);
                    System.out.println("User Message Connection Table created successfully.");
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        databaseConnection.closeConnection();
        return false;
    }

    //Left
    public boolean insertToMessageConnectionTable(int connectionId, User user1, User user2, int port) {
        databaseConnection = new DatabaseConnection();
        try (Connection connection = databaseConnection.getConnection()) {
            String tableName = "UserMessageConnectionTable";
            if (connection != null) {
                String insertQuery = "INSERT INTO " + tableName + " (connectionId, id1, id2, port) VALUES (?, ?, ?, ?)";

                //The information will be taken from message class getters
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setInt(1, connectionId);
                    preparedStatement.setInt(2, user1.getId());
                    preparedStatement.setInt(3, user2.getId());
                    preparedStatement.setInt(4, port);

                    preparedStatement.executeUpdate();
                    System.out.println("Inserted to message connection successfuly");
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        databaseConnection.closeConnection();
        return false;
    }

    public void withDrawLike(Comment comment) {
        comment.decrementLikeCount(this);
    }

    public boolean addToNotificationsTable(Notification notification) {
        databaseConnection = new DatabaseConnection();
        try (Connection connection = databaseConnection.getConnection()) {
            String tableName = "" + getId() + "NotificationsTable";
            if (connection != null) {
                String insertQuery = "INSERT INTO " + tableName + " (notificationId, senderId, receiverId, commentContent, isRead, dateTime) VALUES (?, ?, ?, ?, ?, ?)";

                //The information will be taken from message class getters
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setInt(1, notification.getNotificationID());
                    preparedStatement.setInt(2, notification.getSender().getId());
                    preparedStatement.setInt(3, notification.getReceiver().getId());
                    preparedStatement.setString(4, notification.getCommentContent());
                    preparedStatement.setInt(5, 0);
                    preparedStatement.setString(6, notification.getDateOfNotificaiton());

                    preparedStatement.executeUpdate();
                    System.out.println("Notification is inserted successfully.");
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        databaseConnection.closeConnection();
        return false;
    }

    public boolean readTheNotification(Notification notification) {
        databaseConnection = new DatabaseConnection();
        try (Connection connection = databaseConnection.getConnection()) {
            String tableName = "" + getId() + "NotificationsTable";
            if (connection != null) {
                String insertQuery = "UPDATE " + tableName + " SET isRead = ? WHERE notificationId = ?;";

                //The information will be taken from message class getters
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                    preparedStatement.setInt(1,1);
                    preparedStatement.setInt(2,notification.getNotificationID());
                    preparedStatement.executeUpdate();
                    System.out.println("Notification is read successfully.");
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        databaseConnection.closeConnection();
        return false;
    }
    public ArrayList<Notification> pullTheNotifications() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        String tableName = "" + getId() + "NotificationsTable";
        String selectQuery = "SELECT * FROM " + tableName;

        ArrayList<Notification> notifications = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int notificationId = resultSet.getInt("notificationId");
                int senderId = resultSet.getInt("senderId");
                User sender = databaseConnection.pullUserByIdFromDB(senderId);
                int receiverId = resultSet.getInt("receiverId");
                User receiver = databaseConnection.pullUserByIdFromDB(receiverId);
                String commentContent = resultSet.getString("commentContent");
                String date = resultSet.getString("dateTime");

                Notification m1 = new Notification(sender,receiver,commentContent,date,notificationId);
                m1.setReadCondition(resultSet.getInt("isRead") == 1);
                notifications.add(m1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return notifications;
    }

    public void addProfilePhotoToUserInformationTable(byte[] profilePhoto) {
        databaseConnection = new DatabaseConnection();
        try (Connection connection = databaseConnection.getConnection();) {
            // Replace with your image byte array
            byte[] imageBytes = profilePhoto;

            String sql = "UPDATE userInformationTable SET profilePhoto = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            preparedStatement.setBinaryStream(1, bis, imageBytes.length);
            preparedStatement.setInt(2, this.getId());

            preparedStatement.executeUpdate();

            System.out.println("Profile photo inserted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private byte[] readBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }


    public byte[] pullTheProfilePhotoFromDB(int userId) {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        byte[] profilePhoto = null;
        String tableName = "userInformationTable";
        String selectQuery = "SELECT profilePhoto FROM " + tableName + " WHERE id = ?;";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {

            preparedStatement.setInt(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                InputStream inputStreamProfilePhoto = resultSet.getBinaryStream("profilePhoto");
                profilePhoto = readBytesFromInputStream(inputStreamProfilePhoto);
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return profilePhoto;
    }

    public byte[] pullTheBackgroundPhotoFromDB(int userId) {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        byte[] profilePhoto = null;
        String tableName = "userInformationTable";
        String selectQuery = "SELECT backgroundPhoto FROM " + tableName + " WHERE id = ?;";

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {

            preparedStatement.setInt(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                InputStream inputStreamProfilePhoto = resultSet.getBinaryStream("backgroundPhoto");
                profilePhoto = readBytesFromInputStream(inputStreamProfilePhoto);
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return profilePhoto;
    }

    public ArrayList<LessonPost> pullFromLessonsPostTable() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        String tableName = "" + getId() + "LessonsTable";
        String selectQuery = "SELECT * FROM " + tableName;

        ArrayList<LessonPost> lessons = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int postId = resultSet.getInt("postId");
                String postDescription = resultSet.getString("postDescription");
                String typeFilter = resultSet.getString("typeFilter");
                int dateBinaryBoolean = resultSet.getInt("dateBinaryBoolean");
                int requestNum = resultSet.getInt("requestType");
                boolean requestType;
                if (requestNum == 0) {
                    requestType = false;
                } else {
                    requestType = true;
                }
                String date = resultSet.getString("dateOfPost");

                LessonPost lp1 = new LessonPost(postId, (Student) this,postDescription,typeFilter,dateBinaryBoolean,requestType,date,false);
                lessons.add(lp1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lessons;
    }

    public ArrayList<StudyPost> pullFromStudyPostTable() {
        if (LoginFrame.isTrial)
            return studyPostCollection;
        DatabaseConnection databaseConnection = new DatabaseConnection();
        String tableName = "" + getId() + "StudiesTable";
        String selectQuery = "SELECT * FROM " + tableName;

        ArrayList<StudyPost> lessons = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int postId = resultSet.getInt("postId");
                String author = resultSet.getString("author");
                String postHeading = resultSet.getString("postHeading");
                String postDescription = resultSet.getString("postDescription");
                String postDate = resultSet.getString("postDate");

                byte[] file = new byte[5];

                try {
                    InputStream blobInputStream = resultSet.getBinaryStream("file");
                    if (blobInputStream != null)
                        file = readInputStreamToByteArray(blobInputStream);
                    else
                        file = null;
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }

                String[] topicCollection = new String[5];
                topicCollection[0] = resultSet.getString("Topic1");
                topicCollection[1] = resultSet.getString("Topic2");
                topicCollection[2] = resultSet.getString("Topic3");
                topicCollection[3] = resultSet.getString("Topic4");
                topicCollection[4] = resultSet.getString("Topic5");

                StudyPost lp1 = new StudyPost(postId, this,author, postHeading,postDescription,file,postDate,topicCollection,false);
                lessons.add(lp1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lessons;
    }

    public ArrayList<ActivityPost> pullFromActivitiesPostTable() {
        String tableName = "" + getId() + "TableOfActivities";
        String selectQuery = "SELECT * FROM " + tableName;
        DatabaseConnection databaseConnection = new DatabaseConnection();
        ArrayList<ActivityPost> lessons = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int postId = resultSet.getInt("postId");
                String postDescription = resultSet.getString("postDescription");
                int numberOfAttendants = resultSet.getInt("numberOfAttendants");
                String dateOfPost = resultSet.getString("dateOfPost");
                String typeFilter = resultSet.getString("typeFilter");
                String activityDate = resultSet.getString("activityDate");

                System.out.println("Activity Post Returned Successfully");
                ActivityPost lp1 = new ActivityPost(postId, (Student) this,postDescription,numberOfAttendants,dateOfPost,typeFilter,activityDate,false);
                lessons.add(lp1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lessons;
    }
    public int getActivityCollectionSize() {
        String tableName = "" + getId() + "TableOfActivities";
        String selectQuery = "SELECT * FROM " + tableName;
        int count = 0;
        DatabaseConnection databaseConnection = new DatabaseConnection();
        ArrayList<ActivityPost> lessons = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                count++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }
    public int getLessonCollectionSize() {
        String tableName = "" + getId() + "LessonsTable";
        String selectQuery = "SELECT * FROM " + tableName;
        int count = 0;
        DatabaseConnection databaseConnection = new DatabaseConnection();
        ArrayList<ActivityPost> lessons = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                count++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }
    public int getStudyCollectionSize() {
        String tableName = "" + getId() + "StudiesTable";
        String selectQuery = "SELECT * FROM " + tableName;
        int count = 0;
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                count++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }
    public static byte[] readInputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public ArrayList<Integer> pullIDsFromUserInformationTable() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        String tableName = "userInformationTable";
        String selectQuery = "SELECT * FROM " + tableName;

        ArrayList<Integer> ids = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectQuery)) {

            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");

                ids.add(id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ids;
    }


    public void createMessageConnections() {
        ArrayList<Integer> allUsers = pullIDsFromUserInformationTable();
        if (allUsers.size()>1){
            for (Integer i : allUsers) {
                messageConnections.add(new MessageConnection(this,databaseConnection.pullUserByIdFromDB(i),22,true));
            }
        }
    }

    public void resetMessageConnections() {
        messageConnections = new ArrayList<>();
    }

    public void addMessageToMessageConnection(User receiver, String message) {
        Message temp = new Message(this,receiver,message,new Date().toString());
        for (MessageConnection connection :
                messageConnections) {
            if (connection.getOtherUser().getId() == receiver.getId())
                connection.addMessages(temp,false);
        }
    }
    public int getStudiesPostId(){
        return pullFromStudyPostTable().size();
    }

    public int generateNewPostID() {
        int count = 0;
        count+= generateStudyPostId();
        if(this instanceof Student s){
            count += s.generateActivityPostId();
            count += s.generateLessonPostId();
            s.pullFromLessonsPostTable();
        }
        return count+1;
    }


    public ArrayList<Notification> getNotifiactions() {
        return notificationCollection;
    }
}
