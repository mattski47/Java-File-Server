package com.nau;

import java.util.*;
import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket;
        Socket clientSocket;
        String request, messageType;
        DataInputStream in;
        OutputStream out;

        int port = 8989;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Failed to create socket");
            return;
        }

        System.out.println("Waiting for connections on port: " + port);

        while (true) { // wait for connection
            clientSocket = serverSocket.accept();
            System.out.println("Accepted connection");

            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

            while (true) { // wait for messages
                StringTokenizer st;
                try {
                    request = getRequestLine(in);
                } catch (IOException e) {
                    System.out.println("Invalid Request");
                    continue;
                }

                if (request != null) {
                    // print request
                    System.out.println("\n-------------");
                    System.out.println(request);
                    System.out.println("-------------");
                }

                // begin parsing request
                try {
                    st = new StringTokenizer(request);
                    messageType = st.nextToken();
                } catch (NullPointerException e) {
                    break;
                } catch (NoSuchElementException e) {
                    System.out.println("Invalid Request");
                    continue;
                }

                if (messageType.equals("GET")) {
                    StringBuilder path = new StringBuilder();

                    //path.append("/var/www/");

                    try {
                        if (st.hasMoreTokens()) {
                            path.append(st.nextToken());
                        } else {
                            throw new FileNotFoundException();
                        }

                        System.out.println("Looking for: " + path.toString());

                        InputStream fileStream = new BufferedInputStream(new FileInputStream(path.toString()));

                        int fileSize = fileStream.available();
                        System.out.println("Found with content length: " + fileSize);

                        out.write(("DATA 200 OK \r\ncontent-length: " + fileSize + "\r\n").getBytes());
                        
                        byte[] byteValue = new byte[8196];
                        int bytesRead;
                        
                        while (fileSize > 0) {
                            bytesRead = fileStream.read(byteValue, 0, (int) Math.min(byteValue.length, fileSize));
                            
                            out.write(byteValue, 0, bytesRead);
                            
                            fileSize -= bytesRead;
                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("Not found");
                        out.write(("DATA 404 Not found \r\n").getBytes());
                    }
                } else if (messageType.equals("CLOSE")) {
                    close(clientSocket);
                    break;
                }
            }
        }
    }

    private static String getRequestLine(DataInputStream in) throws IOException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
        return buffer.readLine();
    }

    private static void close(Socket clientSocket) {
        // close connection
        try {
            clientSocket.close();
            System.out.println("Closed connection");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
