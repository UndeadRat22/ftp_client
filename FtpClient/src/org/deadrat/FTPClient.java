package org.deadrat;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.regex.*;

public class FTPClient
{
    private int dataPortTryCount = 0;
    private int maxTryCount = 4;
    private boolean connected;
    private boolean loggedIn;
    private boolean dataSocketCreated;
    
    private byte[] buffer;
    private InetAddress address;
    private int port;

    private int dataPort;

    private Socket controlSocket;
    private ServerSocket serverSocket;
	private Scanner controlScanner;
	private PrintWriter controlWriter;

	private Socket dataSocket;
	private InputStream dataIs;
	private OutputStream dataOs;

	private Scanner dataScanner;
	private PrintWriter dataWriter;
    private Scanner userInputScanner;
    
    private String lastMessage;

    public FTPClient(int bufferSize)
    {
        buffer = new byte[bufferSize];
    }

    public void setAddress(String address, String port) throws UnknownHostException
    {
        if (connected)
            return;
        this.address = Inet4Address.getByName(address);
        this.port = Integer.parseInt(port);
    }

    public void open() throws IOException
    {
        if (connected)
            return;
        controlSocket = new Socket(address, port);
        controlScanner = new Scanner(controlSocket.getInputStream());
        controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
        connected = true;
        printOpResult();
    }

    public void printOpResult()
    {
        lastMessage = controlScanner.nextLine();

        System.out.println(lastMessage);
    }

    public int getOperationStatusCode()
    {
        for (var str : lastMessage.trim().split(" ")) 
        {
            if (Pattern.matches("\\d{3}", str))
                return Integer.parseInt(str);
        }
        return -1;
    }

    public void getDataPort() throws IOException
    {
        if (maxTryCount == dataPortTryCount)
            throw new IOException("Failed to open a listen socket.");
        this.dataPort = (int)(Math.random() * 65565);
        int portPart1 = dataPort / 256;
        int portPart2 = dataPort % 256;
        controlWriter.println("PORT 127,0,0,1," + portPart1 + "," + portPart2 );
        printOpResult();
        try {
            dataPortTryCount++;
            serverSocket = new ServerSocket(dataPort);
        } catch (Exception e)
        {
            getDataPort();
        }
        dataPortTryCount = 0;
    }

    public void list() throws Exception
    {
        controlWriter.println("LIST");
        printOpResult();
        createDataSocket();

        while(dataScanner.hasNext())
        {
			System.out.println(dataScanner.nextLine());
        }
        closeDataSocket();
    }

    private void createDataSocket() throws Exception
    {
        dataSocket = serverSocket.accept();
        dataIs = dataSocket.getInputStream();
        dataOs = dataSocket.getOutputStream();
        dataScanner = new Scanner(dataIs);
        dataWriter = new PrintWriter(dataOs,true);
    }

    private void closeDataSocket() throws Exception
    {
        dataWriter.close();
        dataScanner.close();
        dataIs.close();
        dataOs.close();
        dataSocket.close();
    }

    public boolean getFile(String remoteFilename, String localFilename) throws Exception
    {
        getDataPort();

        var output = createLocalFile(localFilename);
        controlWriter.println("RETR " + remoteFilename);
        createDataSocket();
        printOpResult();
        BufferedInputStream input = new BufferedInputStream(dataIs);
        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }

        output.flush();
        output.close();

        closeDataSocket();
        return true;
    }

    private BufferedOutputStream  createLocalFile(String filename) throws IOException
    {
        File outFile = new File(filename);
        if (outFile.exists())
            outFile.delete();
        outFile.createNewFile();
        return new BufferedOutputStream (new FileOutputStream(outFile));
    }

    public boolean login(String username, String password) throws IOException
    {
        if (loggedIn)
            return false;
        controlWriter.println("USER " + username);
        printOpResult();
        if (getOperationStatusCode() != 331)
            return false;
        controlWriter.println("PASS " + password);
        printOpResult();
        if (getOperationStatusCode() != 230)
            return false;
        loggedIn = true;
        return true;
    }
}
