/**
 * Modify this class to add the authentication and access control features specified in the assignment specifications.
 */

package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class Controller {

    // generate letters of random password range
    public final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    public final String DIGITS = "0123456789";
    public final String SPECIAL = "!@#$%^&*()-_=+[]{}|;:'\",.<>?";
    // group map—list collection
    public Map <String, String> mapGroup = new HashMap<>();
    // user information such as username and password
    public Map <String, String> mapUser = new HashMap<>();
    public Map <String, String> mapCode = new HashMap<>();
    public Map <String, Date> mapToken = new HashMap<>();
    public Map <String, String> mapUsernameToken = new HashMap<>();
    // token
    public String token;
    public Date creationTime;
    public static final long VALID_DURATION_MS = 15 * 60 * 1000; // 15 minutes in milliseconds

    public String getToken() {
        return token;
    }

    public boolean isTokenValid(Date date) {
        Date currentTime = new Date();
        long elapsedTime = currentTime.getTime() - date.getTime();
        return elapsedTime <= VALID_DURATION_MS;
    }
    
    public String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        // Add at least one character from each character set
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        // Generate the remaining characters
        for (int i = 4; i < length; i++) {
            String characterSet = getRandomCharacterSet(random);
            password.append(characterSet.charAt(random.nextInt(characterSet.length())));
        }
        // Shuffle the characters in the password for randomness
        return shuffleString(password.toString());
    }
    // get random character set
    public String getRandomCharacterSet(SecureRandom random) {
        String[] characterSets = {UPPER, LOWER, DIGITS};
        return characterSets[random.nextInt(characterSets.length)];
    }
    // shuffle choosing letter
    public String shuffleString(String input) {
        SecureRandom random = new SecureRandom();
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }
    // regex
    public String regexCapure(String pattern, String text){
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(text);
        // output
        if (matcher.find()) {
            String result = matcher.group(1);
            return result;
        }else{
            return null;
        }
    }
    

    // The program should start up with one default user, root, who is a part of the admin group
    // and starts with a randomly generated password which is printed by the server program.
    @GetMapping("/intialization")
	public String intialization() {
        mapGroup.put("root", "admin");
        int length = 12;
        String generatePassword = generateRandomPassword(length);
        mapUser.put("root", generatePassword);
        System.out.println("Random Password: " + generatePassword);
		return "initialization server, and password is: " + generatePassword;
    }

    @GetMapping("/admin_console")
    public String adminConsole(@RequestParam String operation, @RequestParam String currentUser, @RequestParam String captureUserName, @RequestParam String recipient) {
        if(mapGroup.get(currentUser)=="admin"){
            if(operation.equals("add")){
                // sepcify the desired length of the password
                int length = 12;
                String generatePassword = generateRandomPassword(length);
                System.out.println("Random Password: " + generatePassword);
                final String username = "edsionshang@gmail.com";
                final String password = "xpbu hldi nnik egri";
                // Set up mail properties
                Properties prop = new Properties();
                prop.put("mail.smtp.host", "smtp.gmail.com");
                prop.put("mail.smtp.port", "465");
                prop.put("mail.smtp.auth", "true");
                prop.put("mail.smtp.socketFactory.port", "465");
                prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                prop.put("mail.smtp.starttls.required", "true");
                prop.put("mail.smtp.ssl.protocols", "TLSv1.2");
                // Create a session with an authenticator
                Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
                try {
                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(username));
                    message.setRecipients(
                            Message.RecipientType.TO,
                            InternetAddress.parse(recipient)
                    );
                    message.setSubject("password verify");
                    message.setText("Congrats! " + captureUserName + ", your generated password is '"+generatePassword+"', please save it safely!");
                    Transport.send(message);
                    // put added user in the map, and default this user with group of user category.
                    mapGroup.put(captureUserName, "user");
                    // store user information, username and generated password
                    mapUser.put(captureUserName, generatePassword);
                    System.out.println("Done");
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                return "adding user successfully";
            }else if(operation.equals("modify")){
                // modify user information, captureusername is name of user whose group will be updated.
                // recipient is new group name
                mapGroup.put(captureUserName,recipient);
                return "modifying user successfully";
            }else if(operation.equals("delete")){
                mapGroup.remove(captureUserName);
                mapUser.remove(captureUserName);
                return "deleting user successfully";
            }else{
                return "invalid command line";
            }
        }else{
            return "Sorry, This console can only be accessed by clients that are authenticated as a user in\n" + //
                        "the admin group.";
        }
            
    }
        
    

    @GetMapping("/authentication")
    public String authentication(@RequestParam String usernameinfo, @RequestParam String passwordinfo) {
        String token = mapUsernameToken.get(usernameinfo);
        if(mapToken.get(token)!=null){
            if(isTokenValid(mapToken.get(token))){
                return "Token is valid, you don't need to verify anymore!";
            }
        }
        if(mapUser.get(usernameinfo)==null){
            return "Current username is not exist!";
        }
        if(passwordinfo==mapUser.get(usernameinfo)||passwordinfo.equals(mapUser.get(usernameinfo))){
            // website address
            String recipient = "wenzhen.shang@uon.edu.au";
            // generate a random code
            // sepcify the desired length of the password
            int length = 6;
            String generateCode = generateRandomPassword(length);
            System.out.println("Random Password: " + generateCode);
            final String myemailaddress = "edsionshang@gmail.com";
            final String myemailpassword = "xpbu hldi nnik egri";
            // Set up mail properties
            Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", "465");
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.socketFactory.port", "465");
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            prop.put("mail.smtp.starttls.required", "true");
            prop.put("mail.smtp.ssl.protocols", "TLSv1.2");
            // Create a session with an authenticator
            Session session = Session.getInstance(prop,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(myemailaddress, myemailpassword);
                }
            });
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(myemailaddress));
                message.setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(recipient)
                );
                message.setSubject("password verify");
                message.setText("You're signed up to server, Please enter the following verification code:   "+generateCode+"   to complete the operation.");
                Transport.send(message);
                mapCode.put(usernameinfo, generateCode);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }else{
            return "User name not found or incorrect password";
        }
        return "first step of account verfiy successful!";
    }
    
    @GetMapping("/authentication_reply")
	public String authenticationReply(@RequestParam String usernameInfo, @RequestParam String code) {
        if(mapUser.get(usernameInfo)==null){
            return "Current username is not exist!";
        }
        if(mapCode.get(usernameInfo)==code||mapCode.get(usernameInfo).equals(code)){
            token = UUID.randomUUID().toString();
            creationTime = new Date();
            mapToken.put(token, creationTime);
            mapUsernameToken.put(usernameInfo, token);
            return "Congrats! you complete multi-factor authentication. Your token is:" + "'" + token + "'";
        }else{
            return "Ooop! the code is invalid.";
        }
    }

    // read endpoint of expenses.txt
    @PostMapping("/audit_expenses")
    public String auditExpenses(@RequestBody Integer securityLevel) {
        if(securityLevel==0){
            try (
                Scanner fileIn = new Scanner(new File("expenses.txt"))
            ) {
                String expenses = "";
                while (fileIn.hasNextLine()) {
                    expenses += fileIn.nextLine() + "\n";
                }
                return expenses;
            } catch (FileNotFoundException fne) {
                return "No expenses yet";
            }
        }else{
            return "This request cannot access resources in top security level!";
        }
    }

    // write endpoint of expenses.txt
    @GetMapping("/add_expense")
    public String addExpense(@RequestParam Integer securityLevel, @RequestParam String newExpense) {
        if(securityLevel==0){
            try (
                BufferedWriter fileOut = new BufferedWriter(new FileWriter("expenses.txt", true))
            ) {
                fileOut.append(newExpense + "\n");
                return "Expense added";
            } catch (IOException ioe) {
                return "Unable to add expense";
            }
        }else{
            return "This request cannot access resources in top security level!";
        }
    }

    // read endpoint of timesheets.txt
    @PostMapping("/audit_timesheets")
    public String auditTimesheets(@RequestBody Integer securityLevel) {
        if(securityLevel==0){
            try (
                Scanner fileIn = new Scanner(new File("timesheets.txt"))
            ) {
                String timesheets = "";
                while (fileIn.hasNextLine()) {
                    timesheets += fileIn.nextLine() + "\n";
                }
                return timesheets;
            } catch (FileNotFoundException fne) {
                return "No timesheets yet";
            }
        }else{
            return "This request cannot access resources in top security level!";
        }
    }

    // write endpoint of timesheets.txt
    @GetMapping("/submit_timesheet")
    public String submitTimesheet(@RequestParam Integer securityLevel, @RequestParam String newTimesheet) {
        if(securityLevel==0){
            try (
                BufferedWriter fileOut = new BufferedWriter(new FileWriter("timesheets.txt", true))
            ) {
                fileOut.append(newTimesheet + "\n");
                return "Timesheet added";
            } catch (IOException ioe) {
                return "Unable to add timesheet";
            }
        }else{
            return "This request cannot access resources in top security level!"; 
        }
    }

    // read endpoint of meeting_minutes.txt
    @PostMapping("/view_meeting_minutes")
    public String viewMeetingMinutes(@RequestBody Integer securityLevel) {
        if(securityLevel<=1){
            try (
                Scanner fileIn = new Scanner(new File("meeting_minutes.txt"))
            ) {
                String meetingMinutes = "";
                while (fileIn.hasNextLine()) {
                    meetingMinutes += fileIn.nextLine() + "\n";
                }
                return meetingMinutes;
            } catch (FileNotFoundException fne) {
                return "No meeting minutes yet";
            }
        }else{
            return "This request cannot access resources in security level!"; 
        }
    }
    
    // write endpoint of meeting_minutes.txt
    @GetMapping("/add_meeting_minutes")
    public String addMeetingMinutes(@RequestParam Integer securityLevel, @RequestParam String newMeetingMinutes) {
        if(securityLevel<=1){
            try (
                BufferedWriter fileOut = new BufferedWriter(new FileWriter("meeting_minutess.txt", true))
            ) {
                fileOut.append(newMeetingMinutes + "\n");
                return "Meeting minutes added";
            } catch (IOException ioe) {
                return "Unable to add meeting minutes";
            }
        }else{
            return "This request cannot access resources in security level!";
        }
    }

    // read endpoint of roster.txt
    @PostMapping("/view_roster")
    public String viewroster(@RequestBody Integer securityLevel) {
        if(securityLevel<=2){
            try (
                Scanner fileIn = new Scanner(new File("roster.txt"))
            ) {
                String roster = "";
                while (fileIn.hasNextLine()) {
                    roster += fileIn.nextLine() + "\n";
                }
                return roster;
            } catch (FileNotFoundException fne) {
                return "No roster yet";
            }
        }else{
            return "This request cannot access resources in unclassified level!";
        }
    }

    // write endpoint of roster.txt
    @GetMapping("/roster_shift")
    public String addRoster(@RequestParam Integer securityLevel, @RequestParam String newRoster) {
        if(securityLevel<=2){
            try (
                BufferedWriter fileOut = new BufferedWriter(new FileWriter("roster.txt", true))
            ) {
                fileOut.append(newRoster + "\n");
                return "Shift rostered";
            } catch (IOException ioe) {
                return "Unable to roster shift";
            }
        }else{
            return "This request cannot access resources in unclassified level!";
        }
    }
}