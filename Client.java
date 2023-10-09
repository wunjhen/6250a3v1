import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


public class Client {
    // Perform a http GET request from the specified URL.
    // You can add error checking, such as page not found using exceptions
    public static String get(String urlName) throws Exception {
        URL url = new URI(urlName).toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String reply = "";
        String line;
        while ((line = in.readLine()) != null) {
            reply += line + "\n";
        }
        in.close();
        return reply;
    }

    // Perform a http POST request from the specified URL and pass in the data string.
    // You can add error checking, such as page not found using exceptions
    public static String post(String urlName, String data) throws Exception {
        URL url = new URI(urlName).toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
        new DataOutputStream(con.getOutputStream()).write(data.getBytes("UTF-8"));
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String reply = "";
        String line;
        while ((line = in.readLine()) != null) {
            reply += line + "\n";
        }
        in.close();
        return reply;
    }


    public static void main(String[] args) throws Exception {
        // the program starts up with one default user, root, who is part of the admin group,
        // and start with a randomly generated password which is printed by the server program.
        // intialize a user belonging to admin group and generate a ramdom password.
        System.out.println(get("http://localhost:2250/intialization"));
        

        // admin_console testing
        System.out.println(get("http://localhost:2250/admin_console?operation=add&currentUser=root&captureUserName=wenzhen&recipient=wenzhen.shang@uon.edu.au"));
        System.out.println(get("http://localhost:2250/admin_console?operation=modify&currentUser=root&captureUserName=wenzhen&recipient=admin"));
        System.out.println(get("http://localhost:2250/admin_console?operation=delete&currentUser=root&captureUserName=wenzhen&recipient=null"));

        // Multi-factor authentication. user has to verify themselves with multi-factor authentication after using admin_console to add user.
        System.out.println(get("http://localhost:2250/authentication?usernameinfo=root&passwordinfo=JMs949bRu3j"));
        System.out.println(get("http://localhost:2250/authentication_reply?usernameInfo=root&code=1goS5"));


        // after multi-factor authentication, test token.
        System.out.println(post("http://localhost:2250/authentication", "token"));
        

        // security level symbol: 0 = top security, 1 = security, 2 = unclassified
        // read data from expenses.txt
        System.out.println(post("http://localhost:2250/audit_expenses", "0"));
        // write data in expenses.txts
        System.out.println(get("http://localhost:2250/add_expense?securityLevel=0&newExpense=testgetmethod"));
        // read data from timesheets.txt
        System.out.println(post("http://localhost:2250/audit_timesheets", "0"));
        // write data in timesheets.txt
        System.out.println(get("http://localhost:2250/submit_timesheet?securityLevel=0&newTimesheet=testgetmethod"));
        // read data from meeting_minutes.txt
        System.out.println(post("http://localhost:2250/view_meeting_minutes", "1"));
        // write data in meeting_minutes.txt
        System.out.println(get("http://localhost:2250/add_meeting_minutes?securityLevel=1&newMeetingMinutes=testgetmethod"));
        // read data from roster.txt
        System.out.println(post("http://localhost:2250/view_roster", "2"));
        // write data in roster.txt
        System.out.println(get("http://localhost:2250/roster_shift?securityLevel=2&newRoster=testgetmethod"));

    }
}