/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.servertlint;
import java.io.*;
import java.util.*;
import java.net.*;

/**
 *
 * @author theda
 */
public class Server {
    static Vector<ClientHandler> clients = new Vector<>();
    //Counter for clients
    static int i = 0;
    public static void main(String [] args) throws IOException{
        char[] valueData = new char[4];
        String gameCode = new String();
        
        for (int j = 0; j < valueData.length; j++) {
            int codigoAscii = (int) Math.floor(Math.random() * (122 - 97) + 97);
            valueData[j] = (char) codigoAscii;
            gameCode=gameCode+valueData[j];
        }
        System.out.println("Codigo de acceso a partida "+gameCode);
        int port = 6666;
        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket;
        while(true){
            socket = serverSocket.accept();
            System.out.println("New client request received: " + socket);
            //Obtain input and output streams
            DataInputStream dataIS = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOS = new DataOutputStream(socket.getOutputStream());
            System.out.println("Creating a new handler for this client...");
            System.out.println("Cliente "+i+" Se ha unido");
            ClientHandler match = new ClientHandler(socket,gameCode+" " + i, dataIS,dataOS);
            Thread thread = new Thread(match);
            System.out.println("Adding this client to active client list...");
            clients.add(match);
            thread.start();
            i++;
        }
    }
}

//ClientHandler class
class ClientHandler implements Runnable{
    String name;
    String gameCode;
    int i;
    DataInputStream dataIS;
    DataOutputStream dataOS;
    Socket socket;
    boolean isLoggedIn;
    //constructor
    public ClientHandler(Socket socket, String name, DataInputStream dataIS, DataOutputStream dataOS){
        this.dataIS = dataIS;
        this.dataOS = dataOS;
        this.socket = socket;
        this.name = name;
        this.isLoggedIn = true;
    }
    @Override
    public void run(){
        String received;
        while(true){
            try{
                received = dataIS.readUTF();
                System.out.println(received);
                if(received.equals("logout")){
                    this.isLoggedIn = false;
                    this.socket.close();
                    break;
                }
                //Break the string into message and client part
                StringTokenizer stringToken = new StringTokenizer(received,"/");
                String messageToSend = stringToken.nextToken();
                String client = stringToken.nextToken();
                //search for the client in the connected devices list
                for(ClientHandler toSearch : Server.clients){
                    if(toSearch.name.equals(client) && toSearch.isLoggedIn == true){
                        toSearch.dataOS.writeUTF(this.name + " : " + messageToSend);
                        break;
                    }
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        try{
            this.dataIS.close();
            this.dataOS.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
