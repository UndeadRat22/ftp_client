package org.deadrat;

import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        FTPClient client = new FTPClient();
        Scanner scanner = new Scanner(System.in);
        String host = "host";
        String name = "name";
        String dir = "/";
        while (true) {
            try {
                System.out.print(name + "@" + host + ":"  + dir + "$");
                String line = scanner.nextLine().trim();
                if (line.startsWith("open") || line.startsWith("connect")) {
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
                    try {
                        client.disconnect();
                    } catch (Exception e)
                    {

                    }
                    break;
                } else if (line.startsWith("get")) {
                    String[] names = line.split(" ");
                    client.retrieve(names[1], names[2]);
                    System.out.println("\tTransfer complete");
                } else if (line.startsWith("store")){
                    String fn = line.split(" ")[1];
                    client.store(fn);
                    System.out.println("\tTransfer complete");
                } else if (line.startsWith("cd")){
                    String directory = line.split(" ")[1];
                    client.cwd(directory);
                    dir = client.pwd();
                }
                else if (line.startsWith("test")){
                    client.connect("localhost", 21);
                    client.login("anonymous", "p");
                    client.pwd();
                    client.store("ppp.png");
                    client.retrieve("rec.py", "rec.py");
                    client.cwd("test_dir");
                }
            } catch (Exception e) {
                if (e.getMessage().startsWith("No line found")) {
                    try {
                        client.disconnect();
                    } catch (Exception ex) {
                    }
                    finally {
                        System.exit(1);
                    }
                }
                System.out.println(e.getMessage());
            }
        }
    }
}
