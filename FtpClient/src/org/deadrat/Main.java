package org.deadrat;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        FTPClient client = new FTPClient();
        Scanner scanner = new Scanner(System.in);
        String host = "";
        String name = "";
        String dir = "";
        while (true) {
            try {
                System.out.print("ftp:\\\\" + name + "@" + host + "\t" + dir + ">");
                String line = scanner.nextLine().trim();
                if (line.startsWith("open")) {
                    String[] address = line.split(" ")[1].split(":");
                    client.connect(address[0], Integer.parseInt(address[1]));
                    host = address[0] + ":" + address[1];
                } else if (line.startsWith("login")) {
                    String[] credentials = line.split(" ")[1].split(":");
                    client.login(credentials[0], credentials[1]);
                    name = credentials[0];
                } else if (line.startsWith("pwd")) {
                    dir = client.pwd();
                } else if (line.startsWith("list") || line.startsWith("ls") || line.startsWith("dir")) {
                    client.list();
                } else if (line.startsWith("quit")) {
                    client.disconnect();
                } else if (line.startsWith("exit")) {
                    client.disconnect();
                    break;
                } else if (line.startsWith("test")){
                    client.connect("localhost", 21);
                    client.login("anonymous", "p");
                    client.pwd();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
