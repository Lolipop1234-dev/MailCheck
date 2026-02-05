package engine;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class EmailParser {

    public ParsedEmail parseFile(String path) throws IOException {

        ParsedEmail email = new ParsedEmail();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(path),
                        StandardCharsets.UTF_8
                )
        );

        StringBuilder headerBuilder = new StringBuilder();
        StringBuilder bodyBuilder = new StringBuilder();

        boolean inHeader = true;
        String line;

        while ((line = reader.readLine()) != null) {

            if (inHeader) {
                headerBuilder.append(line).append("\n");

                if (line.startsWith("From:")) {
                    email.from = line.substring(5).trim();
                }
                else if (line.startsWith("Reply-To:")) {
                    email.replyTo = line.substring(9).trim();
                }
                else if (line.startsWith("Subject:")) {
                    email.subject = line.substring(8).trim();
                }
                else if (line.startsWith("Return-Path:")) {
                    email.returnPath = line.substring(12).trim();
                }

                // Header endet bei leerer Zeile
                if (line.trim().isEmpty()) {
                    inHeader = false;
                }
            }
            else {
                bodyBuilder.append(line).append("\n");
            }
        }

        reader.close();

        email.rawHeaders = headerBuilder.toString();
        email.body = bodyBuilder.toString();

        return email;
    }
}
