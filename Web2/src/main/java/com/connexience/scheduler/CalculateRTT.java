package com.connexience.scheduler;

/**
 * Created by naa166 - Anirudh Agarwal on 17/07/2015.
 */
//PingClient.java by Arjun Radhakrishnan-1000767933
import java.io.*;
import java.net.*;
import java.util.*;

public class CalculateRTT{

    private static long sentTime;
    private static int returnSequence;
    private static String Ping="PING";
    private static String line;
    private static DatagramPacket receivePacket;
    private static long temp[]=new long[10];
    private static int i,t=0,count=0,loss=0;
    private static long sum,avg,max=0,min=0,rtt;


    public static String findRTT(String address, String enginePort) throws Exception{
        String dsn=address;
        int port = Integer.parseInt(enginePort);
        // Create a datagram socket for receiving and sending UDP packets through the port specified on the command line.
        DatagramSocket socket = new DatagramSocket();
        //set the socket time out to 1 sec.
        socket.setSoTimeout(1000);
        //geting server ip address from dns.
        //InetAddress serverAddress = InetAddress.getByName(dsn);
        InetAddress serverAddress = InetAddress.getLocalHost();
        //loop for sending the 10 ping to server.
        for(i = 0; i < 10; i++) {
            //ping message is created.
            Ping="  PING:"+ i +":"+ System.currentTimeMillis() + "\n";
            //ping message is converted to bytes and stored in a byte array.
            byte[] Sent = Ping.getBytes();
            //datagram packets are created for sending and receving packets.
            DatagramPacket sendPacket = new DatagramPacket(Sent,Sent.length,serverAddress,port);
            System.out.println("Checking server address" + serverAddress + Sent + Sent.length +port );
            DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
            //packet is sent with ping in it.
            socket.send(sendPacket);
            try{
                //to receive the packet sent by the server to client
                socket.receive(receivePacket);
                //read and parse the data inside the packet
                readData(receivePacket);
                //check whether the pings are insequence.
                checkSequence(returnSequence,i);
            }
            //to catch the time out exception
            catch(SocketTimeoutException e){
                e.printStackTrace();
                System.out.println("Ping "+i+ " Timeout");
                loss++;
            }
        }
        //methed to calculate maximum,minimum and average RTT
        calculateRtt();
        //method to print the output.

        String result = "\nMinimum RTT = "+min + "\tMaximum RTT = "+max + "\tAverage RTT = " + avg + "\tLoss Rate = "+loss*10+"%";
        return result;
    }

    /*
    * read and parse the packed send from server
    */
    public static void readData(DatagramPacket receivePacket) throws IOException{
        //reading the data in the packet into a buffer
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(receivePacket.getData())));
        //reading the buffer and storing its data into a string
        line = br.readLine();
        //spliting the string using delimiters and storing the data into string array.
        String resp[] = line.split(":");
        //parse the data into the corresponding datatypes and storing it into respective data variables
        returnSequence = Integer.parseInt(resp[1]);
        sentTime = Long.parseLong(resp[2].trim());
    }
    /*
    * printdata is called print the data returned from the server
    */
    private static void printData(DatagramPacket receivePacket) throws IOException {
        //getting the current system time
        long currentTime = System.currentTimeMillis();
        //calculating rtt
        rtt = currentTime - sentTime;
        System.out.println(line+ " :successfully received " + " RTT:" + rtt+"ms");
    }
    /*
    * method checkSequence cheks whether the ping are insequence
    */
    private static boolean checkSequence(int returnSeq, int packetNumber) throws IOException {
        //checks sent and the returned packed sequence
        if (returnSequence == packetNumber){
            //if true prints the data in the packet
            printData(receivePacket);
            temp[count]=rtt;
            //count the no of pings in sequence
            count++;
            return true;
        }
        //print the ping is out of order and sents the exceped ping
        else{
            System.out.print("Found Ping "+returnSequence+" out of order .  Excepted is Ping "+(packetNumber-1));
            //count the no of lost packets
            loss++;
            return false;
        }
    }
    /*
    * calculateRtt calculates Minimum ,Maximum and Average RTT.
    */
    private static void calculateRtt(){
        //calculate Avrage RTT
        for (int j=0; j < count; j++){
            sum=sum+temp[j];
            avg=sum/count;
        }
        //calculate Maximum RTT
        for(int x=0;x < count;x++){
            if(temp[x] > max )
                max = temp[x];
        }
        //calculate minimum RTT
        min=temp[0];
        for(int y=1;y < count;y++){
            if(temp[y] < min )
                min=temp[y];
        }
    }
    /*
    * print the Min ,max,avg and lossrate
    */
    /*public static String outPut() {
        for(int a=0;a<100;a++){
            System.out.print("*");
        }
        System.out.print("\nMinimum RTT = "+min);
        System.out.print("\tMaximum RTT = "+max);
        System.out.print("\tAverage RTT = "+avg);
        System.out.println("\tLoss Rate = "+loss*10+"%");
    }*/
}
