package catphish;

import java.io.Console;
import java.util.HashSet;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class Catphish {
    private Logger logger;
    private Scanner input;
    private String choice;
    private Account acc;
    
    public static void main(String args[]) {
        Catphish app = new Catphish();
        app.initialize();
    }

    void initialize() {
        this.logger = LogManager.getLogger(Catphish.class);
        this.input = new Scanner(System.in);
        this.logger.info("Entering the main menu");
        this.mainMenu();
    }

    void mainMenu() {
        Boolean open = true;
        this.printBanner();
        this.printMainOptions();
        this.choice = this.input.nextLine();

        while (open) {
            switch (this.choice) {
                case "SIGN UP":
                    this.logger.info("Signing up");
                    this.signUp();
                    this.printBanner();
                    this.printMainOptions();
                    this.choice = this.input.nextLine();
                    break;

                case "SIGN IN":
                    this.logger.info("Signing in");
                    this.signIn();
                    this.printBanner();
                    this.printMainOptions();
                    this.choice = this.input.nextLine();
                    break;

                case "CLOSE":
                    this.logger.info("User closing the program");
                    this.close();
                    break;

                default:
                    this.logger.error("Invalid input in the main menu");
                    // Intentionally not printing the banner
                    this.printMainOptions();
                    this.choice = this.input.nextLine();
                    break;
            }
        }
    }

    void printBanner() {
        String banner = """
            █████████             █████              █████       ███          █████     
            ███░░░░░███           ░░███              ░░███       ░░░          ░░███      
           ███     ░░░   ██████   ███████   ████████  ░███████   ████   █████  ░███████  
          ░███          ░░░░░███ ░░░███░   ░░███░░███ ░███░░███ ░░███  ███░░   ░███░░███ 
          ░███           ███████   ░███     ░███ ░███ ░███ ░███  ░███ ░░█████  ░███ ░███ 
          ░░███     ███ ███░░███   ░███ ███ ░███ ░███ ░███ ░███  ░███  ░░░░███ ░███ ░███ 
           ░░█████████ ░░████████  ░░█████  ░███████  ████ █████ █████ ██████  ████ █████
            ░░░░░░░░░   ░░░░░░░░    ░░░░░   ░███░░░  ░░░░ ░░░░░ ░░░░░ ░░░░░░  ░░░░ ░░░░░ 
                                            ░███                                         
                                            █████                                        
                                           ░░░░░                                         
                """;

        System.out.println(banner);
    }

    void printMainOptions() {
        System.out.println("Enter 'SIGN UP', 'SIGN IN' or 'CLOSE'");
    }

    void signUp() {
        String username;
        String password;
        Integer attempts = 3;

        while (attempts >= 1) {
            System.out.println("Attempts: " + attempts);
            System.out.print("Username: ");
            username = this.input.nextLine();

            if (username.length() > Account.MAX_NAM_LEN) {
                this.logger.error("Username exceeds " + Account.MAX_NAM_LEN + " characters");
                System.out.println("Username exceeds " + Account.MAX_NAM_LEN + " characters");
                attempts--;

            } else if (username.contains(" ")) {
                this.logger.error("Included spaces");
                System.out.println("No spaces are allowed");
                attempts--;

            } else if (username.equals("")) {
                this.logger.error("Empty string");
                System.out.println("Enter a minimum of one character");
                attempts--;

            } else if (Database.accountExists(username)) {
                this.logger.error("Account already exists");
                System.out.println("An account with that username already exists");
                attempts--;

            } else {
                Console console = System.console();
                password = new String(console.readPassword("Password: "));

                if (password.length() > Account.MAX_PAS_LEN) {
                    this.logger.error("Password exceeds " + Account.MAX_PAS_LEN + " characters");
                    System.out.println("Password exceeds " + Account.MAX_PAS_LEN + " characters");
                    attempts--;

                } else {
                    Database.createAccount(username, password);
                    this.logger.info(username + " created");
                    this.logger.info("Returning to the main menu");
                    System.out.println("Account Created!");
                    break;
                }
            }
        }

        this.logger.info("Sign-up failed");
        this.logger.info("Returning to the main menu");
    }

    void signIn() {
        this.validateAccount();

        if (this.acc != null) {
            this.logger.info(this.acc.getUser() + " signed in");
            this.logger.info("Entering the home menu");
            this.homeMenu();

        } else {
            this.logger.info("Sign-in failed");
            this.logger.info("Returning to the main menu");
        }
    }

    void validateAccount() {
        String username;
        String password;
        Integer attempts = 3;

        while (attempts >= 1) {
            System.out.println("Attempts: " + attempts);
            System.out.print("Username: ");
            username = this.input.nextLine();

            if (Database.accountExists(username)) {
                this.logger.info("Attempting to sign into " + username);

                Console console = System.console();
                password = new String(console.readPassword("Password: "));
    
                if (Database.validPassword(username, password)) {
                    this.acc = Database.retrieveAccount(username);
                    break;

                } else {
                    this.logger.info("Invalid password");
                    System.out.println("Invalid password");
                }

            } else {
                this.logger.info("Account doesn't exist");
                System.out.println("No account with that username exists");
            }

            attempts--;
        }
    }

    void homeMenu() {
        Boolean open = true;
        this.printHomeOptions();
        this.choice = this.input.nextLine();

        while (open) {
            switch (this.choice) {
                case "PROFILE":
                    this.logger.info("Viewing profile");
                    this.profile();
                    this.printHomeOptions();
                    this.choice = this.input.nextLine();
                    break;

                case "MATCH":
                    this.logger.info("Looking for people to match with");
                    this.match();
                    this.printHomeOptions();
                    this.choice = this.input.nextLine();
                    break;

                case "MATCHES":
                    this.logger.info("Viewing current matches");
                    this.matches();
                    this.printHomeOptions();
                    this.choice = this.input.nextLine();
                    break;

                case "SIGN OUT":
                    this.logger.info("Signing out");
                    this.signOut();
                    this.logger.info("Returning to the main menu");
                    return;

                case "DELETE":
                    if (this.isSure()) {
                        this.logger.info("Deleting account");
                        this.delete();
                        this.logger.info("Returning to the main menu");
                        return;

                    } else {
                        this.printHomeOptions();
                        this.choice = this.input.nextLine();
                        break;
                    }

                case "CLOSE":
                    this.logger.info("User closing the program");
                    this.close();
                    break;
                    
                default:
                    this.logger.info("Invalid input in the home menu");
                    this.printHomeOptions();
                    this.choice = this.input.nextLine();
                    break;
            }
        }
    }

    void printHomeOptions() {
        System.out.println("Enter 'PROFILE', 'MATCH', 'MATCHES', 'SIGN OUT', 'DELETE' or 'CLOSE'");
    }

    void profile() {
        Boolean open = true;
        this.acc.printProfile();
        this.printProfileOptions();
        this.choice = this.input.nextLine();

        while (open) {
            switch (this.choice) {
                case "PIC":
                    this.logger.info("Editing picture");
                    this.editPicture();
                    this.acc.printProfile();
                    this.printProfileOptions();
                    this.choice = this.input.nextLine();
                    break;

                case "BIO":
                    this.logger.info("Editing bio");
                    this.editBio();
                    this.acc.printProfile();
                    this.printProfileOptions();
                    this.choice = this.input.nextLine();
                    break;

                case "HOME":
                    this.logger.info("Returning to the home menu");
                    return;

                case "CLOSE":
                    this.logger.info("User closed the program");
                    this.close();
                    break;
                    
                default:
                    this.logger.info("Invalid input in the profile menu");
                    this.printProfileOptions();
                    this.choice = this.input.nextLine();
                    break;
            }
        }
    }

    void printProfileOptions() {
        System.out.println("Enter 'PIC', 'BIO', 'HOME' or 'CLOSE'");
    }

    void editPicture() {
        System.out.println("Enter your picture. Enter 'DEFAULT' to use default picture or 'DONE' when finished");
        String picture = "";
        String line = "";
        
        while (this.input.hasNextLine()) {
            line = this.input.nextLine();
            
            if (line.equals("DONE")) {
                break;
            }

            if (line.equals("DEFAULT")) {
                picture = """
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
                break;
            }

            picture = picture + "\n" + line;
        }

        if (picture.length() > Account.MAX_PIC_LEN) {
            picture = picture.substring(0, Account.MAX_PIC_LEN);
        }

        this.acc.setPicture(picture);
        Database.setPicture(this.acc.getUser(), picture);
    }

    void editBio() {
        System.out.println("Enter bio. Single lines are allowed. Enter a double line when complete");
        Boolean writing = true;
        Boolean enteredTwice = false;
        String previousLine = "nonempty value for the initial 'while' check";
        String currentLine = "";
        String bio = "";

        while (writing) {
            currentLine = this.input.nextLine();
            enteredTwice = previousLine.equals("") && currentLine.equals("");

            if (enteredTwice) {
                writing = false;
                break;
            }

            previousLine = currentLine;
            bio = bio + "\n" + currentLine;
        }

        if (bio.length() > Account.MAX_BIO_LEN) {
            bio = bio.substring(0, Account.MAX_BIO_LEN);
        }

        this.acc.setBio(bio);
        Database.setBio(this.acc.getUser(), bio);
    }

    void match() {
        Boolean open = true;
        Account candidate = Database.retrieveCandidate(this.acc);

        if (candidate == null) {
            this.logger.info("There are no people");
            this.logger.info("Returning to the home menu");
            System.out.println("There are no people");
            return;

        } else {
            this.logger.info("Viewing " + candidate.getUser());
            Database.printProfile(candidate.getUser());;
            this.printMatchOptions();
            this.choice = this.input.nextLine();    
        }

        while (open) {
            switch (this.choice) {
                case "":
                    this.logger.info("Liked");
                    this.acc.addChecked(candidate.getUser());
                    Database.check(this.acc.getUser(), candidate.getUser());
                    this.acc.addLiked(candidate.getUser());
                    Boolean matched = Database.like(this.acc, candidate);
                    
                    if (matched) {
                        this.logger.info("Matched");
                        System.out.println("Matched!");
                        System.out.println("Enter NEXT or HOME");

                        while (matched) {
                            this.choice = this.input.nextLine();
                
                            switch (this.choice) {
                                case "NEXT":
                                    matched = false;
                                    break;
                
                                case "HOME":
                                    this.logger.info("Returning to the home menu");
                                    return;

                                default:
                                    this.logger.error("Invalid input in the match menu");
                                    System.out.println("Enter NEXT or HOME");
                                    this.choice = this.input.nextLine();  
                                    break;
                            }
                        }
                    }

                    candidate = Database.retrieveCandidate(this.acc);

                    if (candidate == null) {
                        this.logger.info("There are no people");
                        this.logger.info("Returning to the home menu");
                        System.out.println("There are no people");
                        return;

                    } else {
                        this.logger.info("Viewing " + candidate.getUser());
                        Database.printProfile(candidate.getUser());;
                        this.printMatchOptions();
                        this.choice = this.input.nextLine();    
                    }

                    break;

                case " ":
                    this.logger.info("Rejected");
                    this.acc.addChecked(candidate.getUser());
                    Database.check(this.acc.getUser(), candidate.getUser());
                    candidate = Database.retrieveCandidate(this.acc);

                    if (candidate == null) {
                        this.logger.info("There are no people");
                        this.logger.info("Returning to the home menu");
                        System.out.println("There are no people");
                        return;

                    } else {
                        this.logger.info("Viewing " + candidate.getUser());
                        Database.printProfile(candidate.getUser());;
                        this.printMatchOptions();
                        this.choice = this.input.nextLine();    
                    }
            
                    break;

                case "HOME":
                    this.logger.info("Returning to the home menu");
                    return;

                case "CLOSE":
                    this.logger.info("User closing the program");
                    this.close();
                    break;

                default:
                    this.logger.error("Invalid input in the match menu");
                    this.printMatchOptions();
                    this.choice = this.input.nextLine();  
                    break;
            }
        }
    }

    void printMatchOptions() {
        System.out.println("Enter a single line to like, a single space to reject, 'HOME' or 'CLOSE'");
    }

    void matches() {
        Boolean open = true;
        HashSet<String> matches = Database.updateMatches(this.acc.getUser());

        if (matches.isEmpty()) {
            this.logger.info("There are no matches");
            this.logger.info("Returning to the home menu");
            System.out.println("You have no matches");
            return;

        } else {
            this.printMatches(matches);
            this.printMatchesOptions();
            this.choice = this.input.nextLine();
        }

        while (open) {
            if (matches.contains(this.choice)) {
                this.logger.info(this.choice + " selected");
                this.interact(this.choice);
                matches = Database.updateMatches(this.acc.getUser());
                Boolean noMatches = matches.size() == 0;

                if (noMatches) {
                    this.logger.info("There are no matches");
                    this.logger.info("Returning to the home menu");
                    System.out.println("You have no matches");
                    return;
                }

                this.printMatches(matches);
                this.printMatchesOptions();
                this.choice = this.input.nextLine();
        
            } else if (this.choice.equals("HOME")) {
                this.logger.info("Returning to the home menu");
                return;

            } else if (this.choice.equals("CLOSE")) {
                this.logger.info("User closing the program");
                this.close();

            } else {    
                this.logger.error("Invalid input in the matches menu");           
                this.printMatchesOptions();
                this.choice = this.input.nextLine();
            }
        }
    }

    void printMatches(HashSet<String> matches) {
        System.out.println("Matches:");

        for (String match : matches) {
            System.out.println(match);
        }
    }

    void printMatchesOptions() {
        System.out.println("Enter the name of the match, 'HOME' or 'CLOSE'");
    }

    void interact(String match) {
        Boolean open = true;
        this.printMatchedOptions();
        this.choice = this.input.nextLine();

        while (open) {
            switch (this.choice) {
                case "PROFILE":
                    // If match unmatched user while in the interaction menu
                    if (Database.stillMatched(this.acc.getUser(), match)) {
                        this.logger.info("Viewing " + match + "'s profile");
                        Database.printProfile(match);
                        this.printMatchedOptions();
                        this.choice = this.input.nextLine();
                        break;

                    } else {
                        this.logger.info(match + " is no longer a match");
                        this.logger.info("Returning to the matches menu");
                        System.out.println(match +" has unmatched with you");
                        return;
                    }
                    
                case "CHAT":
                    // If match unmatched user while in the interaction menu
                    if (Database.stillMatched(this.acc.getUser(), match)) {
                        this.logger.info("Chatting with " + match);
                        this.chat(match);

                        // If match unmatched user in the chat menu
                        if (Database.stillMatched(this.acc.getUser(), match)) {
                            this.printMatchedOptions();
                            this.choice = this.input.nextLine();
                            break;
    
                        } else {
                            switch (this.choice) {
                                // If user backed out of chat, then don't signal the unmatch yet
                                case "":
                                    this.printMatchedOptions();
                                    this.choice = this.input.nextLine();
                                    break;

                                // If user already received the unmatch signal from within the chat, then propagate the signal
                                default:
                                    return;
                            }

                            break;
                        }

                    } else {
                        this.logger.info(match + " is no longer a match");
                        this.logger.info("Returning to the matches menu");
                        System.out.println(match +" has unmatched with you");
                        return;
                    }

                case "UNMATCH":
                    // If match unmatched user while in the interaction menu
                    if (Database.stillMatched(this.acc.getUser(), match)) {
                        this.logger.info("Unmatching with " + match);
                        this.logger.info("Returning to the matches menu");
                        Database.unmatch(this.acc.getUser(), match);
                        return;

                    } else {
                        this.logger.info(match + " has already unmatched");
                        this.logger.info("Returning to the matches menu");
                        System.out.println("This person has already unmatched with you");
                        return;
                    }

                case "BACK":
                    this.logger.info("Returning to the matches menu");
                    return;

                case "CLOSE":
                    this.logger.info("User closing the program");
                    this.close();
                    break;

                default:
                    this.logger.error("Invalid input in the interaction menu");
                    this.printMatchedOptions();
                    this.choice = this.input.nextLine();
                    break;
            }
        }
    }

    void printMatchedOptions() {
        System.out.println("Enter 'PROFILE', 'CHAT', 'UNMATCH' or 'BACK'");
    }

    void chat(String match) {
        Boolean open = true;
        Chat chat = Database.updateChat(this.acc.getUser(), match);
        Integer messagePointer;
        chat.setPerspective(this.acc.getUser());
        this.printMatchName(match);
        chat.printViewableMessages();
        this.printChatOptions();
        this.choice = this.input.nextLine();

        while (open) {
            switch (this.choice) {
                case "":
                    return;

                case " ":
                    if (Database.stillMatched(this.acc.getUser(), match)) {
                        messagePointer = chat.getMessagePointer();
                        chat = Database.updateChat(this.acc.getUser(), match);
                        chat.setMessagePointer(messagePointer);
                        chat.scrollUp();
                        break;

                    } else {
                        this.logger.info(match + " is no longer a match");
                        this.logger.info("Returning to the matches menu");
                        System.out.println("This person has unmatched with you");
                        return;
                    }

                case "  ":
                    if (Database.stillMatched(this.acc.getUser(), match)) {
                        messagePointer = chat.getMessagePointer();
                        chat = Database.updateChat(this.acc.getUser(), match);
                        chat.setMessagePointer(messagePointer);
                        chat.scrollDown();
                        break;

                    } else {
                        this.logger.info(match + " is no longer a match");
                        this.logger.info("Returning to the matches menu");
                        System.out.println("This person has unmatched with you");
                        return;
                    }

                default:
                    if (Database.stillMatched(this.acc.getUser(), match)) {
                        if (this.choice.length() > Message.MAX_LEN) {
                            this.choice = this.choice.substring(0, Message.MAX_LEN);
                        }
    
                        Database.addMessage(this.acc.getUser(), match, this.choice);
                        chat = Database.updateChat(this.acc.getUser(), match);
                        break;

                    } else {
                        this.logger.info(match + " is no longer a match");
                        this.logger.info("Returning to the matches menu");
                        System.out.println("This person has unmatched with you");
                        return;
                    }
            }

            this.printMatchName(match);
            chat.printViewableMessages();
            this.printChatOptions();
            this.choice = this.input.nextLine();
        }
    }

    void printMatchName(String match) {
        System.out.println();
        System.out.println(String.format("%" + (Chat.RIGHTSHIFT + Chat.MAX_CHAR_PER_LINE) / 2 + "s", match));
    }

    void printChatOptions() {
        System.out.println("Enter a single line to go back to home, a single space to scroll up, and a double space to scroll down");
    }

    void signOut() {
        this.acc = null;
    }

    Boolean isSure() {
        System.out.println("Are you sure you want to delete your account?");
        System.out.println("Enter 'YES' or 'NO'");
        this.choice = this.input.nextLine();
        Boolean undecided = true;

        while (undecided) {
            switch (this.choice) {
                case "YES":
                    return true;
                    
                case "NO":
                    return false;

                default:
                    System.out.println("Enter 'YES' or 'NO'");
                    this.choice = this.input.nextLine();
            }
        }

        return false;
    }

    void delete() {
        Database.removeAccount(this.acc.getUser());
        this.acc = null;
    }

    void close() {
        this.input.close();
        System.exit(0);
    }
}