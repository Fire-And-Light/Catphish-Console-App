package catphish;

import java.util.HashSet;

// This is not fully consistent with a database account for the sake of space
class Account {
    static final Integer MAX_NAM_LEN = 20;
    static final Integer MAX_PAS_LEN = 30;
    static final Integer MAX_PAS_HASH_LEN = 384;
    static final Integer MAX_PAS_SALT_LEN = 32;
    static final Integer MAX_PIC_LEN = 8000;
    static final Integer MAX_BIO_LEN = 1000;

    private String username;
    private String picture; // Only one picture is supported
    private String bio;
    private HashSet<String> checked;
    private HashSet<String> liked;

    Account(String username, String picture, String bio,
        HashSet<String> checked,
        HashSet<String> liked) {
        super();
        this.username = username;
        this.picture = picture;
        this.bio = bio;
        this.checked = checked;
        this.liked = liked;
    }

    String getUser() {
        return this.username;
    }

    String getPicture() {
        return this.picture;
    }

    void setPicture(String picture) {
        this.picture = picture;
    }

    void printPicture() {
        System.out.println(this.picture);
    }

    String getBio() {
        return this.bio;
    }

    void setBio(String bio) {
        this.bio = bio;
    }

    void printBio() {
        System.out.println(this.bio);
    }

    HashSet<String> getChecked() {
        return this.checked;
    }

    void addChecked(String username) {
        this.checked.add(username);
    }

    HashSet<String> getLiked() {
        return this.liked;
    }

    void addLiked(String username) {
        this.liked.add(username);
    }

    void removeLiked(String username) {
        this.liked.remove(username);
    }

    void printProfile() {
        this.printPicture();
        System.out.println(this.getUser());
        this.printBio();
    }
}