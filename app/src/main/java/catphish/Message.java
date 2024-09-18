package catphish;

class Message {
    static final Integer MAX_LEN = 250;

    private String author;
    private String message;

    Message(String sender, String message) {
        super();
        this.author = sender;
        this.message = message;
    }

    String getAuthor() {
        return this.author;
    }

    String getMessage() {
        return this.message;
    }
}