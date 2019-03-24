package org.deadrat;

public class Main {

    public static void main(String[] args) {
        var client = new FTPClient(1024);

        try {
            client.setAddress("127.0.0.1", "21");           
            client.open();
        } catch (Exception e) {
            System.out.println("failed in open");
        }
        try {
            client.login("anonymous", "?");
        } catch (Exception e) {
            System.out.println("failed in user");
        }
        try {
            client.getDataPort();
        }
        catch (Exception e)
        {
            System.out.println("failed to get data port");
        }
        try {
            client.list();
        } catch (Exception e) {
            System.out.println(e);
        }
        try {
            client.getFile("unknown.png", "ppp.png");
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}
