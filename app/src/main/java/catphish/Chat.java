package catphish;

import java.util.concurrent.ConcurrentHashMap;

class Chat {
    static final Integer MAX_MSG_VIEWED = 7;
    static final Integer MAX_CHAR_PER_LINE = 45;
    static final Integer RIGHTSHIFT = 15;

    private ConcurrentHashMap<Integer, Message> messages;
    private Integer messageID; // So the order of the messages are tracked
    private String perspective; // So sent and received messages are formatted based on the sender
    private Integer messagePointer; // So the viewable messages are tracked

    Chat(ConcurrentHashMap<Integer, Message> messages) {
        super();
        this.messages = messages;
        this.messageID = messages.size();
        this.perspective = null;
        this.messagePointer = messageID;
    }

    Chat() {
        this(new ConcurrentHashMap<Integer, Message>());
    }

    void addMessage(String sender, String message) {
        this.messageID++;
        this.messages.put(this.messageID, new Message(sender, message));
        this.messagePointer = this.messageID;
        // Log4j message sent by sender
    }

    void setMessages(ConcurrentHashMap<Integer, Message> messages) {
        this.messages = messages;
    }

    Message getMessage(Integer messageID) {
        return messages.get(messageID);
    }

    ConcurrentHashMap<Integer, Message> getMessages() {
        return this.messages;
    }

    // Prints only Chat.MAX_MSG_VIEWED or less
    void printViewableMessages() {
        Integer start;
        Boolean leftJust = false;
        Message message;
        String author = "";
        String[] words;
        String nextWord = "";
        String currentLine = "";
        Integer nextWordSpace = 0;
        Integer nextLineSpace = 0;
        String firstPiece = "";
        String secondPiece = "";

        System.out.println();

        if (this.messagePointer - Chat.MAX_MSG_VIEWED < 1) {
            start = 1;

        } else {
            start = this.messagePointer - Chat.MAX_MSG_VIEWED + 1; // Add one so that Chat.MAX_MSG_VIEWED messages are view rather than one extra
        }
        
        // Iterate through messages
        for (Integer messageID = start; messageID <= this.messagePointer; messageID++) { // Increasing order because printing is downwards on the console
            message = this.messages.get(messageID);
            author = message.getAuthor();
            words = message.getMessage().split(" ");

            // Iterate through words per message
            for (Integer wordID = 0; wordID < words.length; wordID++) {
                nextWord = words[wordID];
                nextWordSpace = nextWord.length();

                // If new line
                if (currentLine.equals("")) {
                    nextLineSpace = nextWordSpace; // The next line starts at the next word

                } else {
                    nextLineSpace = nextLineSpace + nextWordSpace + 1; // Add one to compensate for the space between current line and the next word
                }

                // If the next word is larger than a full line
                if (nextWordSpace > Chat.MAX_CHAR_PER_LINE) {
                    // Print the current line without the next word
                    if (!currentLine.equals("")) {
                        this.printSingleLineLeftJust(author, currentLine);
                    }

                    // Split the word into one full line and the rest of the word
                    firstPiece = nextWord.substring(0, Chat.MAX_CHAR_PER_LINE);
                    secondPiece = nextWord.substring(Chat.MAX_CHAR_PER_LINE);
                    nextWordSpace = secondPiece.length();

                    // Print the full line
                    this.printSingleLineRightJust(author, firstPiece);

                    // If the rest of the word is still larger than a full line, print another full line
                    while (nextWordSpace > Chat.MAX_CHAR_PER_LINE) {
                        firstPiece = secondPiece.substring(0, Chat.MAX_CHAR_PER_LINE);
                        secondPiece = secondPiece.substring(Chat.MAX_CHAR_PER_LINE);
                        nextWordSpace = secondPiece.length();

                        this.printSingleLineLeftJust(author, firstPiece);
                    }
                    
                    // Since the message exceeds a full line, the whole message is left-justified
                    leftJust = true;

                    // The next line starts over and is the rest of the word that's less than a full line
                    currentLine = secondPiece;
                    nextLineSpace = nextWordSpace;

                // If the current line plus the next word results in a larger-than-full line
                } else if (nextLineSpace > Chat.MAX_CHAR_PER_LINE) {
                    // Since the message exceeds a full line, the whole message is left-justified
                    leftJust = true;

                    // Print the current line without the next word
                    this.printSingleLineLeftJust(author, currentLine);

                    // The next line starts over and is the next word
                    currentLine = nextWord;
                    nextLineSpace = nextWord.length();

                // If the current line plus the next word results in a less-than-full line
                } else {
                    if (currentLine.equals("")) {
                        currentLine = nextWord;

                    } else {
                        currentLine = currentLine + " " + nextWord;
                    }
                }
            }

            // If the messages exceeds a full line
            if (leftJust) {
                this.printSingleLineLeftJust(author, currentLine);
                leftJust = false;

            } else {
                this.printSingleLineRightJust(author, currentLine);
            }

            // Restart the current line if there's more lines left
            currentLine = "";
        }

        System.out.println();
    }

    // When messages are smaller than or Chat.MAX_CHAR_PER_LINE, right-justify the line
    void printSingleLineRightJust(String author, String line) {
        if (author.equals(this.perspective)) {
            System.out.println(String.format("%" + (Chat.RIGHTSHIFT + Chat.MAX_CHAR_PER_LINE) + "s", String.format("%" + Chat.MAX_CHAR_PER_LINE + "s", line)));

        } else {
            System.out.println(String.format("%-" + Chat.MAX_CHAR_PER_LINE + "s", line));
        }
    }

    // When messages are larger than Chat.MAX_CHAR_PER_LINE, left-justify the entire message
    void printSingleLineLeftJust(String author, String line) {
        if (author.equals(this.perspective)) {
            System.out.println(String.format("%" + (Chat.RIGHTSHIFT + Chat.MAX_CHAR_PER_LINE) + "s", String.format("%-" + Chat.MAX_CHAR_PER_LINE + "s", line)));

        } else {
            System.out.println(String.format("%-" + Chat.MAX_CHAR_PER_LINE + "s", line));
        }
    }

    void scrollUp() {
        if (this.messagePointer - 1 >= 7) {
            this.messagePointer--;
        }
    }

    void scrollDown() {
        if (this.messagePointer + 1 <= this.messages.size()) {
            this.messagePointer++;
        }
    }

    void setPerspective(String username) {
        this.perspective = username;
    }

    Integer getMessagePointer() {
        return this.messagePointer;
    }

    void setMessagePointer(Integer messagePointer) {
        this.messagePointer = messagePointer;
    }
}