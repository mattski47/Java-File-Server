package com.nau;

import java.util.*;
import java.io.*;
import java.net.*;

public class Client {
    // socket variable
    static Socket socket;
    static DataInputStream in;
    static DataOutputStream out;
    static Boolean connected = false;

    public static void main(String[] args) {
        // write your code here
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String userInput, command;

        while (true) {
//            System.out.println("Enter Command: OPEN, GET, CLOSE, QUIT");
            try {
                // wait for input
                userInput = br.readLine();
            } catch (IOException e) {
                System.out.println(e);
                continue;
            }

            // get user input tokens
            StringTokenizer st = new StringTokenizer(userInput);

            // extract the command
            try {
                command = st.nextToken().toLowerCase();
            } catch (NoSuchElementException e) {
                System.out.println("Invalid Command");
                continue;
            }

            if (command.equals("open")) {
                if (st.countTokens() == 2) {
                    try {
                        open(st.nextToken(), Integer.parseInt(st.nextToken()));
                        connected = true;
                    } catch (NumberFormatException e) {
                        System.out.println(e);
                    }
                } else {
                    System.out.println("Invalid Command - Please provide a ADDRESS:IP argument.");
                }
            } else if (command.equals("get")) {
                // Check that socket is connected
                if (!connected) {
                    System.out.println("Invalid Command - Please connect to server.");
                    continue;
                }
                // Check remaining argument length
                if (st.countTokens() == 2) {
                    get(st.nextToken(), st.nextToken());
                } else {
                    System.out.println("Invalid Command - Please provide a source and destination.");
                }
            } else if (command.equals("close")) {
                // Check that socket is connected
                if (!connected) {
                    System.out.println("Invalid Command - Please connect to server.");
                    continue;
                }
                
                close();
            } else if (command.equals("quit")) {
                if (connected) {
                    close();
                }
                
                return;
            } else {
                System.out.println("Invalid Command");
            }
        }
    }

    private static void open(String ip, int port) {
        // open socket connection and save in variable
        try {
            socket = new Socket(ip, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            System.out.printf("Connected to %s:%d\n", ip, port);
        } catch (IOException e) {
            System.out.println("Could not bind socket");
        }
    }

    private static void get(String sourcePath, String destPath) {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
        
        //System.out.println(sourcePath + ", " + destPath);

        try {
            // send request for a file
            out.write(("GET " + sourcePath + " \r\n").getBytes());

            System.out.println("-------------");


            String header = buffer.readLine();
            System.out.println(header);

            if (header.startsWith("DATA 200 OK")) {
                String contentLength = buffer.readLine();
                System.out.println(contentLength);
                
                StringTokenizer st = new StringTokenizer(contentLength);
                st.nextToken();
                int length = Integer.parseInt(st.nextToken());

                byte[] data = new byte[8196];
                int bytesRead;

                try (FileOutputStream file = new FileOutputStream(destPath)) {
                    while (length > 0) {
                        bytesRead = in.read(data, 0, (int) Math.min(data.length, length));

                        // write file
                        file.write(data, 0, bytesRead);

                        length -= bytesRead;
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Could not write file.");
                }

                System.out.println("File received.");
            }

            System.out.println("-------------");
            // if not found alert error
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static String getResponseLine(DataInputStream in) throws IOException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
        return buffer.readLine();
    }

    private static void close() {
        // close socket connection
        try {
            out.write(("CLOSE \r\n").getBytes());
            System.out.println("Closing connection");
            socket.close();
            connected = false;
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
