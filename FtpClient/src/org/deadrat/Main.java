package org.deadrat;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        var client = new FTPClient(1024);
        Scanner reader = new Scanner(System.in);
        while (true)
        {
            System.out.print("ftp > ");
            String line = reader.nextLine();

            if (line.contains("open"))
            {
                try
                {
                    var cmdargs = line.split(" ");
                    if (cmdargs[1].equals("localhost"))
                    {
                        client.setAddress("127.0.0.1", "21");
                    }
                    else
                    {
                        client.setAddress(cmdargs[1], cmdargs[2]);
                    }
                    client.open();
                } catch (Exception e)
                {
                    System.err.println("Exception occured when trying to open a ftp connection.");
                    System.err.println(e);
                }
            }
            else if (line.contains("login"))
            {
                try
                {
                    var cmdargs = line.split(" ");
                    client.login(cmdargs[1], cmdargs[2]);
                } catch (Exception e)
                {
                    System.err.println("Exception occured when trying to login to a remote ftp connection.");
                    System.err.println(e);
                }
            }
            else if (line.substring(0, 3).equals("dir") || line.substring(0, 2).equals("ls"))
            {
                try
                {
                    client.list();
                } catch (Exception e)
                {
                    System.err.println("Exception occured when trying to list remote files.");
                    System.err.println(e);
                }
            }
            else if (line.substring(0, 3).equals("get") || line.substring(0, 8).equals("download"))
            {
                var cmdargs = line.split(" ");
                try
                {
                    client.getFile(cmdargs[1], cmdargs[2]);
                } catch (Exception e)
                {
                    System.err.println("Exception occured when trying to get remote file " + cmdargs[1] + ".");
                    System.err.println(e);
                }
            }
        }
    }
}
