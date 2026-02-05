package engine;

public class ParsedEmail {
    public String from;
    public String replyTo;
    public String subject;
    public String returnPath;
    public String rawHeaders;
    public String body;

    public ParsedEmail() {
        this.from = "";
        this.replyTo = "";
        this.subject = "";
        this.returnPath = "";
        this.rawHeaders = "";
        this.body = "";
    }
}