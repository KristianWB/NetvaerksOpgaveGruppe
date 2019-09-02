import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.net.*;

import javafx.scene.control.*;

// Server class 
public class Server extends Application {

    // Text felt til besked overblik
    private TextArea messageArea = new TextArea();

    // Vector to store active clients
    static Vector<ClientHandler> clientList = new Vector<>();

    // counter for clients
    static int clientNumber = 0;

    public void start(Stage primaryStage) throws IOException {
        //GUI build
        Scene scene = new Scene(new ScrollPane(messageArea), 450, 250);
        primaryStage.setTitle("Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread( () -> {

            // server is listening on port 8000
            try {
                ServerSocket serverSocket = new ServerSocket(8001);
                messageArea.appendText("Server started at " + new Date() + '\n');

                // running infinite loop for getting client request
                while (true) {
                    // Accept the incoming request
                    //try {

                    Socket socket = serverSocket.accept();

                    System.out.println(
                            "New client request received : " + socket +'\n'
                    );
                    messageArea.appendText(
                            "New client request received : " + socket + '\n'
                    );

                    // obtain input and output streams
                    assert socket != null;
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                    System.out.println(
                            "Creating a new handler for this client..." + '\n'
                    );
                    messageArea.appendText(
                            "Creating a new handler for this client..." + '\n'
                    );

                    // Create a new handler object for handling this request.
                    ClientHandler multiThreadClientHandler = new ClientHandler(socket, "client " + clientNumber, dis, dos);

                    // Create a new Thread with this object.
                    Thread thread = new Thread(multiThreadClientHandler);

                    System.out.println(
                            "Adding this client " + clientNumber + " to active client list" + '\n'
                    );
                    messageArea.appendText(
                            "Adding this client " + clientNumber + " to active client list" +'\n'
                    );

                    // add this client to active clients list
                    clientList.add(multiThreadClientHandler);

                    // start the thread.
                    thread.start();

                    // increment i for new client.
                    // i is used for naming only, and can be replaced
                    // by any naming scheme
                    clientNumber++;

                    //} catch (IOException e) {
                    //e.printStackTrace();
                    //}

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static void main(String[] args) {

        launch(args);
    }


    // ClientHandler class
    class ClientHandler implements Runnable {
        Scanner scn = new Scanner(System.in);
        private String name;
        final DataInputStream dis;
        final DataOutputStream dos;
        Socket s;
        boolean isloggedin;

        // constructor
        public ClientHandler(Socket s, String name,
                             DataInputStream dis, DataOutputStream dos) {
            this.dis = dis;
            this.dos = dos;
            this.name = name;
            this.s = s;
            this.isloggedin = true;
        }

        @Override
        public void run() {

            String received;
            while (true) {
                try {
                    // receive the string
                    received = dis.readUTF();

                    System.out.println(received);
                    messageArea.appendText(received);

                    if (received.equals("logout")) {
                        this.isloggedin = false;
                        this.s.close();
                        break;
                    }

                    // break the string into message and recipient part
                    StringTokenizer st = new StringTokenizer(received, "#");
                    String MsgToSend = st.nextToken();
                    String recipient = st.nextToken();

                    // search for the recipient in the connected devices list.
                    // ar is the vector storing client of active users
                    for (ClientHandler mc : Server.clientList) {
                        // if the recipient is found, write on its
                        // output stream
                        if (mc.name.equals(recipient) && mc.isloggedin == true) {
                            mc.dos.writeUTF(this.name + " : " + MsgToSend);
                            System.out.println(
                                    "Message from " + name + " Sent to recipent " + recipient
                            );
                            messageArea.appendText(
                                    "Message from " + name + " Sent to recipent " + recipient + '\n'
                            );
                            break;
                        }
                    }
                } catch (IOException e) {

                    e.printStackTrace();
                }

            }
            try {
                // closing resources
                this.dis.close();
                this.dos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}