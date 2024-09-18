package catphish;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Data access class rather than data access object since I don't think I would be using more than one object
class Database {
    private static final Connection CONN = DatabaseConnector.getConnection();

    static void ensureAccountTableExists() throws SQLException {
        String SQL = "CREATE TABLE IF NOT EXISTS Account("
            +   "username VARCHAR(" + Account.MAX_NAM_LEN + ") PRIMARY KEY,"
            +   "password_salt VARCHAR(" + Account.MAX_PAS_SALT_LEN + ") NOT NULL,"
            +   "password_hash VARCHAR(" + Account.MAX_PAS_HASH_LEN + ") NOT NULL,"
            +   "picture VARCHAR(" + Account.MAX_PIC_LEN + "),"
            +   "bio VARCHAR(" + Account.MAX_BIO_LEN + "),"
            + ");";
        PreparedStatement statement = Database.CONN.prepareStatement(SQL);
        statement.execute();
    }

    static void ensureCheckedTableExists(String username) throws SQLException {
        String SQL = "CREATE TABLE IF NOT EXISTS " + username + "Checked" + "("
            +   "username VARCHAR(" + Account.MAX_NAM_LEN + "),"
            +   "FOREIGN KEY (username) REFERENCES Account(username) ON DELETE CASCADE,"
            + ");";
        PreparedStatement statement = Database.CONN.prepareStatement(SQL);
        statement.execute();
    }

    static void ensureLikedTableExists(String username) throws SQLException {
        String SQL = "CREATE TABLE IF NOT EXISTS " + username + "Liked" + "("
            +   "username VARCHAR(" + Account.MAX_NAM_LEN + "),"
            +   "FOREIGN KEY (username) REFERENCES Account(username) ON DELETE CASCADE,"
            + ");";
        PreparedStatement statement = Database.CONN.prepareStatement(SQL);
        statement.execute();
    }

    static void ensureMatchesTableExists(String username) throws SQLException {
        String SQL = "CREATE TABLE IF NOT EXISTS " + username + "Matches" + "("
            +   "username VARCHAR(" + Account.MAX_NAM_LEN + "),"
            +   "FOREIGN KEY (username) REFERENCES Account(username) ON DELETE CASCADE,"
            + ");";
        PreparedStatement statement = Database.CONN.prepareStatement(SQL);
        statement.execute();
    }

    static void ensureMatchChatTableExists(String username1, String username2) throws SQLException {
        String chatTable = Database.retrieveChatName(username1, username2);
        String SQL = "CREATE TABLE IF NOT EXISTS " + chatTable + "("
            +   "time_sent DATETIME NOT NULL,"
            +   "author VARCHAR(" + Account.MAX_NAM_LEN + "),"
            +   "message VARCHAR(" + Message.MAX_LEN + "),"
            +   "FOREIGN KEY (author) REFERENCES Account(username) ON DELETE CASCADE,"
            + ");";
        PreparedStatement statement = Database.CONN.prepareStatement(SQL);
        statement.execute();
    }

    static Boolean accountExists(String username) {
        try {
            Database.ensureAccountTableExists();

            PreparedStatement statement = Database.CONN.prepareStatement("SELECT * FROM Account WHERE username = ?;");
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return true;

            } else {
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    static void addAccount(String username, String passwordSalt,  String passwordHash, String picture, String bio, HashSet<String> checked, HashSet<String> liked, ConcurrentHashMap<String, Chat> matches) {
        try {
            String SQL;

            Database.ensureAccountTableExists();

            // Insert account into the Account table
            SQL = "INSERT INTO Account VALUES (?, ?, ?, ?, ?);";
            PreparedStatement statement = Database.CONN.prepareStatement(SQL);
            statement.setString(1, username);
            statement.setString(2, passwordSalt);
            statement.setString(3, passwordHash);
            statement.setString(4, picture);
            statement.setString(5, bio);
            statement.executeUpdate();

            Database.ensureMatchesTableExists(username);

            if (matches != null) {
                String matchesTable = username + "Matches";
                String match;
                Chat chat;
                String chatTable;
    
                // Insert matches and chats
                for (Map.Entry<String, Chat> e : matches.entrySet()) {
                    match = e.getKey();
                    chat = e.getValue();
                    chatTable = Database.retrieveChatName(username, match);
    
                    // Insert that match into the table of the user's matches
                    SQL = "INSERT INTO " + matchesTable + " VALUES (?);";
                    statement = Database.CONN.prepareStatement(SQL);
                    statement.setString(1, match);
                    statement.executeUpdate();
    
                    Database.ensureMatchChatTableExists(username, match);
    
                    // Insert messages of the chat
                    for (Message message : chat.getMessages().values()) {
                        SQL = "INSERT INTO " + chatTable + " VALUES (?, ?, ?);";
                        statement = Database.CONN.prepareStatement(SQL);
                        statement.setTimestamp(1, new Timestamp((new Date()).getTime()));
                        statement.setString(2, message.getAuthor());
                        statement.setString(3, message.getMessage());
                        statement.executeUpdate();
                    }
                }
            }

            Database.ensureCheckedTableExists(username);

            if (checked != null) {
                String checkedTable = username + "Checked";

                // Insert checked user into the user's table of users checked
                for (String checkedUser : checked) {
                    SQL = "INSERT INTO " + checkedTable + " VALUES (?);";
                    statement = Database.CONN.prepareStatement(SQL);
                    statement.setString(1, checkedUser);
                    statement.executeUpdate();
                }
            }

            Database.ensureLikedTableExists(username);

            if (liked != null) {
                String likedTable = username + "Liked";

                // Insert liked user into the user's table of users liked
                for (String likedUser : liked) {
                    SQL = "INSERT INTO " + likedTable + " VALUES (?);";
                    statement = Database.CONN.prepareStatement(SQL);
                    statement.setString(1, likedUser);
                    statement.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void removeAccount(String username) {
        try {
            String SQL;

            SQL = "DELETE FROM Account WHERE username = ?;";
            PreparedStatement statement = Database.CONN.prepareStatement(SQL);
            statement.setString(1, username);
            statement.executeUpdate();

            SQL = "TRUNCATE TABLE " + username + "Checked;";
            statement = Database.CONN.prepareStatement(SQL);
            statement.execute();

            SQL = "DROP TABLE " + username + "Checked;";
            statement = Database.CONN.prepareStatement(SQL);
            statement.execute();

            SQL = "SELECT * FROM " + username + "Matches;";
            statement = Database.CONN.prepareStatement(SQL);
            ResultSet matches = statement.executeQuery();
            String match;

            while (matches.next()) {
                match = matches.getString("username");
                Database.unmatch(username, match);
            }

            SQL = "DROP TABLE " + username + "Matches;";
            statement = Database.CONN.prepareStatement(SQL);
            statement.execute();

            SQL = "DROP TABLE " + username + "Liked;";
            statement = Database.CONN.prepareStatement(SQL);
            statement.execute();

            // Check if the Account table is empty, and remove it if is
            SQL = "SELECT * FROM Account;";
            statement = Database.CONN.prepareStatement(SQL);
            ResultSet accounts = statement.executeQuery();

            if (!accounts.next()) {
                SQL = "DROP TABLE Account;";
                statement = Database.CONN.prepareStatement(SQL);
                statement.execute();
            }
            

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void createAccount(String username, String password) {
        String passwordSalt = Salt.generate();
        String passwordHash = Hash.SHA384toString(password + passwordSalt);
        String defaultPicture = """
            .......................................................
            .......................................................
            .......................           .....................
            ......................             ....................
            .....................              ....................
            ......................             ....................
            .....................               ...................
            ......................            .....................
            .......................           .....................
            ........................         ......................
            ........................         ......................
            .....................               ...................
            ................                         ..............
            .............                               ...........
                """;
        String bio = null;
        HashSet<String> checked = null;
        HashSet<String> liked = null;
        ConcurrentHashMap<String, Chat> matches = null;

        Database.addAccount(username, passwordSalt, passwordHash, defaultPicture, bio, checked, liked, matches);
    }

    static Boolean validPassword(String username, String password) {
        try {
            Database.ensureAccountTableExists();

            PreparedStatement statement = Database.CONN.prepareStatement("SELECT * FROM Account WHERE username = ?;");
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            rs.next();

            return rs.getString("password_hash").equals(Hash.SHA384toString(password + rs.getString("password_salt")));

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    static Account retrieveAccount(String username) {
        try {
            PreparedStatement statement = Database.CONN.prepareStatement("SELECT * FROM Account WHERE username = ?;");
            statement.setString(1, username);
            ResultSet accSet = statement.executeQuery();
            accSet.next();

            String picture = accSet.getString("picture");
            String bio = accSet.getString("bio");

            HashSet<String> checked = new HashSet<String>();
            String checkedTable = username + "Checked";
            String checkedUser;

            statement = Database.CONN.prepareStatement("SELECT * FROM " + checkedTable + ";");
            ResultSet checkedSet = statement.executeQuery();

            // Iterate through checked users
            while (checkedSet.next()) {
                checkedUser = checkedSet.getString("username");
                checked.add(checkedUser);
            }

            HashSet<String> liked = new HashSet<String>();
            String likedTable = username + "Liked";
            String likedUser;

            statement = Database.CONN.prepareStatement("SELECT * FROM " + likedTable + ";");
            ResultSet likedSet = statement.executeQuery();

            // Iterate through liked users
            while (likedSet.next()) {
                likedUser = likedSet.getString("username");
                liked.add(likedUser);
            }

            return new Account(username, picture, bio, checked, liked);

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    static String retrieveChatName(String username1, String username2) {
        if (username1.compareTo(username2) < 0) {
            return username1 + username2;

        } else {
            return username2 + username1;
        }
    }

    static void setBio(String username, String bio) {
        try {
            PreparedStatement statement = Database.CONN.prepareStatement("UPDATE Account SET bio = ? WHERE username = ?;");
            statement.setString(1, bio);
            statement.setString(2, username);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void setPicture(String username, String picture) {
        try {
            PreparedStatement statement = Database.CONN.prepareStatement("UPDATE Account SET picture = ? WHERE username = ?;");
            statement.setString(1, picture);
            statement.setString(2, username);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static Account retrieveCandidate(Account user) {
        HashSet<String> checked = user.getChecked();
        // Select from Account, not including the current account
        String SQL = "SELECT username FROM Account WHERE username != '" + user.getUser() + "' AND ";

        // Select from Account, not including checked accounts
        for (String checkedUser: checked) {
            SQL = SQL + "username != '" + checkedUser + "' AND ";
        }

        // Removes the trailing AND after excluding the current account and checked accounts
        SQL = SQL.substring(0, SQL.length() - " AND ".length());

        try {
            PreparedStatement statement = Database.CONN.prepareStatement(SQL);
            ResultSet unchecked = statement.executeQuery();

            if (unchecked.next()) {
                return retrieveAccount(unchecked.getString("username"));

            } else {
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    static void check(String current, String candidate) {
        try {
            PreparedStatement statement = Database.CONN.prepareStatement("INSERT INTO " + current + "Checked VALUES (?)");
            statement.setString(1, candidate);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static Boolean like(Account current, Account candidate) {
        try {
            String currentName = current.getUser();
            String candidateName = candidate.getUser();

            PreparedStatement statement = Database.CONN.prepareStatement("INSERT INTO " + currentName + "Liked VALUES (?)");
            statement.setString(1, candidateName);
            statement.executeUpdate();

            if (candidate.getLiked().contains(currentName)) {
                // Create chat table
                Database.ensureMatchChatTableExists(current.getUser(), candidate.getUser());

                // Insert candidate into current's matches table
                statement = Database.CONN.prepareStatement("INSERT INTO " + currentName + "Matches VALUES (?);");
                statement.setString(1, candidateName);
                statement.executeUpdate();

                // Insert current into candidate's matches table
                statement = Database.CONN.prepareStatement("INSERT INTO " + candidateName + "Matches VALUES (?);");
                statement.setString(1, currentName);
                statement.executeUpdate();

                return true;
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    static void addMessage(String sender, String receiver, String message) {
        try {
            String chatTable = Database.retrieveChatName(sender, receiver);
            PreparedStatement statement = Database.CONN.prepareStatement("INSERT INTO " + chatTable + " VALUES (?, ?, ?)");
            statement.setTimestamp(1, new Timestamp((new Date()).getTime()));
            statement.setString(2, sender);
            statement.setString(3, message);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void unmatch(String current, String match) {
        try {
            PreparedStatement statement = Database.CONN.prepareStatement("DELETE FROM " + current + "Liked WHERE username = ?");
            statement.setString(1, match);
            statement.executeUpdate();

            statement = Database.CONN.prepareStatement("DELETE FROM " + match + "Liked WHERE username = ?");
            statement.setString(1, current);
            statement.executeUpdate();

            statement = Database.CONN.prepareStatement("DELETE FROM " + current + "Matches WHERE username = ?");
            statement.setString(1, match);
            statement.executeUpdate();

            statement = Database.CONN.prepareStatement("DELETE FROM " + match + "Matches WHERE username = ?");
            statement.setString(1, current);
            statement.executeUpdate();

            String chatTable = Database.retrieveChatName(current, match);
            statement = Database.CONN.prepareStatement("TRUNCATE TABLE " + chatTable + ";");
            statement.execute();
            statement = Database.CONN.prepareStatement("DROP TABLE " + chatTable + ";");
            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static Boolean stillMatched(String current, String liked) {
        try {
            PreparedStatement statement = Database.CONN.prepareStatement("SELECT * FROM " + current + "Matches WHERE username = ?");
            statement.setString(1, liked);
            ResultSet matchedSet = statement.executeQuery();

            return matchedSet.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    static void printProfile(String username) {
        try {
            PreparedStatement statement = Database.CONN.prepareStatement("SELECT * FROM Account WHERE username = ?;");
            statement.setString(1, username);
            ResultSet accSet = statement.executeQuery();
            accSet.next();

            String picture = accSet.getString("picture");
            String bio = accSet.getString("bio");

            System.out.println(picture);
            System.out.println(username);
            System.out.println(bio);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static HashSet<String> updateMatches(String username) {
        try {
            String matchesTable = username + "Matches";

            Database.ensureMatchesTableExists(username);
            
            PreparedStatement statement = Database.CONN.prepareStatement("SELECT * FROM " + matchesTable + ";");
            ResultSet matchesSet = statement.executeQuery();
    
            HashSet<String> matches = new HashSet<String>();
            String match;
    
            while (matchesSet.next()) {
                match = matchesSet.getString("username");
                matches.add(match);
            }

            return matches;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    static Chat updateChat(String sender, String receiver) {
        try {
            String chatTable = Database.retrieveChatName(sender, receiver);
            PreparedStatement statement = Database.CONN.prepareStatement("SELECT * FROM " + chatTable + ";");
            ResultSet messageSet = statement.executeQuery();
            ConcurrentHashMap<Integer, Message> messages = new ConcurrentHashMap<Integer, Message>();
            Integer messageID = 1;
            Message message;

            // Iterate through the messages
            while (messageSet.next()) {
                message = new Message(messageSet.getString("author"), messageSet.getString("message"));
                messages.put(messageID, message);
                messageID++;
            }

            Chat chat = new Chat(messages);
            chat.setPerspective(sender);

            return chat;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}