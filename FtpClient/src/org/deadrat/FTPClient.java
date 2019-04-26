package org.deadrat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class FTPClient {

    private Socket socket = null;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;
    private static boolean DEBUG = true;

    public void connect(String host, int port) throws Exception {
        if (socket != null)
            throw new IOException("Already connected");

        this.socket = new Socket(host, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        String response = readLine();
        if (!response.startsWith("220"))
            throw new Exception("Could not connect to server: " + response);
    }

    public void login(String user, String pass) throws Exception {
        sendLine("USER " + user);
        String response = readLine();
        if (!response.startsWith("331"))
            throw new Exception("Could not log in as: " + user + ":" + pass + "\n response:" + response);

        sendLine("PASS " + pass);

        response = readLine();
        if (!response.startsWith("230"))
            throw new Exception("Could not log in as: " + user + ":" + pass + "\n response:" + response);
    }

    public void disconnect() throws Exception {
        if (socket == null)
            throw new Exception("Not connected");
        try {
            sendLine("QUIT");
        } finally {
            socket = null;
        }
    }

    public String pwd() throws Exception {
        sendLine("PWD");
        String response = readLine();
        if (!response.startsWith("257"))
            throw new Exception("Received unknown response from server: " + response);
        int start = response.indexOf('"');
        return response.substring(start + 1, response.indexOf('"', start + 1));
    }

    public void cwd(String dir) throws Exception {
        sendLine("CWD " + dir);
        String response = readLine();
        if (!response.startsWith("250"))
            throw new Exception("Received unknown response from server: " + response);
    }

    public void list() throws Exception {
        String[] addr = pasv();
        sendLine("LIST");
        String response = readLine();
        if (!response.startsWith("150"))
            throw new Exception("Received unknown response from server: " + response);
        Socket dataSocket = new Socket(addr[0], Integer.parseInt(addr[1]));
        BufferedInputStream input = new BufferedInputStream(dataSocket.getInputStream());
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        BufferedOutputStream output = new BufferedOutputStream(System.out);
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        response = readLine();
        output.flush();
        input.close();

        dataSocket.close();
        if (!response.startsWith("226"))
            throw new Exception("Received unknown response from server: " + response);
    }

    private String[] pasv() throws Exception {
        sendLine("PASV");
        String response = readLine();
        if (!response.startsWith("227"))
            throw new IOException("Unable to enter passive mode: " + response);
        String[] parts = response.substring(response.indexOf('(') + 1, response.indexOf(')')).split(",");
        String ip = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3];
        String port = Integer.toString(Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]));
        return new String[] { ip, port };
    }

    public void stor(File file) throws Exception {
        if (!file.exists())
            throw new Exception("Specified file does not exist");
        if (!file.isFile())
            throw new Exception("Can not upload specified file!");

        BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));

        String[] serverDataSockAddr = pasv();
        sendLine("STOR " + file.getName());
        Socket dataSocket = new Socket(serverDataSockAddr[0], Integer.parseInt(serverDataSockAddr[1]));

        String response = readLine();
        if (!response.startsWith("125")) {
            input.close();
            dataSocket.close();
            throw new IOException("Could not send file: " + file.getName() + "response:  " + response);
        }

        BufferedOutputStream output = new BufferedOutputStream(dataSocket.getOutputStream());
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
        output.close();
        input.close();
        dataSocket.close();

        response = readLine();
        if (!response.startsWith("226"))
            throw new IOException("Could not send file: " + file.getName() + "response:  " + response);

    }

    public void bin() throws Exception {
        sendLine("TYPE I");
        String response = readLine();
        if (!response.startsWith("200"))
            throw new Exception("Could not enter binary mode");
    }

    public void ascii() throws Exception {
        sendLine("TYPE A");
        String response = readLine();
        if (!response.startsWith("200"))
            throw new Exception("Could not enter ascii mode");
    }

    private void sendLine(String line) throws Exception {
        if (socket == null)
            throw new Exception("Not connected to any server.");
        try {
            writer.write(line + "\r\n");
            writer.flush();
            if (DEBUG) {
                System.out.println("> " + line);
            }
        } catch (IOException e) {
            socket = null;
            throw e;
        }
    }

    private String readLine() throws Exception {
        String line = reader.readLine();
        if (DEBUG) {
            System.out.println("< " + line);
        }
        return line;
    }
}