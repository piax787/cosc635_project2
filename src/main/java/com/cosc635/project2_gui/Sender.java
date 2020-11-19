/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cosc635.project2_gui;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Sender {

    private static final int BASE_SEQUENCE_NUMBER = 0;
    private static int windowSize = 4;
    private static int currentSeqNum = 1;
    private static int totalPacketsSent = 0;
    private static long start = 0;
    private static long end = 0;
    private static int max = 4094;// subtracted 2 to fit seqNum and lastPacket identifier
    private static int userNum;
    private static int testNum;
    private static int packetLoss;
    private static int startIndex;
    private static int endIndex;
    private static byte[] data;
    private static int lostSeqNum;

    static int packetLossSim() {
        //user inputs number 0-99
        Scanner reader = new Scanner(System.in);
        do {
            System.out.println("Please enter a number from 0-99:");
            userNum = Integer.parseInt(reader.nextLine());
            if (userNum < 0 || userNum > 99) {
                System.out.println("Invalid. Please enter a number from 0-99: \n");
                testNum = -1;   //wrong input = loop again
            } else if (userNum >= 0 && userNum <= 99) {
                return userNum;
            }
        } while (userNum >= 0 && userNum <= 99 || testNum == -1);
        return userNum;
    }

    static byte[] checkIfLastPacket(byte[] totalBytes, int endIndex, int currentSeqNum) {
        byte[] num = new byte[2];
        num[0] = (byte) currentSeqNum;
        if (totalBytes.length == endIndex) {
            num[1] = (byte) -1;
        } else {
            num[1] = (byte) 0;
        }
        return num;
    }

    static byte[] setupPacket(int max, int currentSeqNum, byte[] totalBytes) {
        startIndex = max * currentSeqNum;
        endIndex = startIndex + max;
        data = new byte[4094];
        if (endIndex > totalBytes.length) {
            endIndex = totalBytes.length;
        }
        data = Arrays.copyOfRange(totalBytes, startIndex, endIndex); //get bytes for the current packet from totalBytes
        byte[] seqNum = checkIfLastPacket(totalBytes, endIndex, currentSeqNum);
        byte[] destination = new byte[data.length + seqNum.length];
        System.arraycopy(seqNum, 0, destination, 0, seqNum.length);
        System.arraycopy(data, 0, destination, seqNum.length, data.length);
        return destination;
    }

    static int goBackN(int lostSeqNum) {
//        each step adds one because we are 1 based indexing
        if (lostSeqNum % 4 == 0) {
            return lostSeqNum - 4 + 1;
        } else {
            return lostSeqNum - (lostSeqNum % 4) + 1;
        }
    }

    public static void start(String protocolChoice, String randomNumInput) throws Exception {
        String input = "";
        System.out.println("SAW or GBN?");
        // if user did not choose protocol from gui, get value from command line
        if (protocolChoice.isEmpty()) {
            Scanner user = new Scanner(System.in);
            input = user.next();
        } else {
            input = protocolChoice;
        }

        if (input.contentEquals("SAW")) {
            // Create a socket
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(1000);

            // Generating the sequence number
            Integer sequenceNumber = BASE_SEQUENCE_NUMBER - 1;

            // Get path for COSC635_P2_DataSent.txt file
            String currentDirPath = System.getProperty("user.dir");
            Path filepath = Paths.get(currentDirPath, "src", "main", "java", "com", "cosc635", "project2_gui", "COSC635_P2_DataSent.txt");
            System.out.println(filepath.toString());
            //Reading file 
            Scanner file = new Scanner(new File(filepath.toString()));
//            Scanner file = new Scanner(new File("COSC635_P2_DataSent.txt"));
            //Putting file into ArrayList        
            ArrayList<String> result = new ArrayList<>();
            while (file.hasNext()) {
                result.add(file.next());
            }
            //Starting while loop to run as long as there
            //are words in the file	   
            while (sequenceNumber < result.size() - 1) {
                boolean timedOut = true;

                while (timedOut) {
                    sequenceNumber++;

                    // Create a byte array for sending and receiving data
                    byte[] sendData = new byte[4096];
                    byte[] receiveData = new byte[4096];

                    //Setting the string to the next word in the ArrayList            
                    String inp = result.get(sequenceNumber);

                    // Get path for COSC635_P2_DataSent.txt file
                    Path receiverfilepath = Paths.get(currentDirPath, "src", "main", "java", "com", "cosc635", "project2_gui", "COSC635_P2_DataReceived.txt");
                    System.out.println(filepath.toString());
                    BufferedWriter myWriter = new BufferedWriter(new FileWriter(filepath.toString()));
//                  BufferedWriter myWriter = new BufferedWriter(new FileWriter("C:\\Users\\Aharon's PC\\Desktop\\COSC635_P2_DataReceived.txt"));
                    myWriter.write(result.toString() + " ");
                    myWriter.close();
                    // Get the IP address of the server
                    InetAddress IPAddress = InetAddress.getLocalHost();

                    System.out.println("Sending Packet (Sequence Number " + sequenceNumber + ")");

                    // Get byte data for message                 
                    sendData = inp.getBytes();
                    ByteBuffer buf = ByteBuffer.wrap(sendData);
                    try {
                        // Send the UDP Packet to the server
                        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, IPAddress, 8888);
                        socket.send(packet);

                        // Receive the server's packet
                        DatagramPacket received = new DatagramPacket(receiveData, receiveData.length);
                        socket.receive(received);

                        // Get the message from the server's packet
                        String msg = new String(received.getData(), received.getOffset(), received.getLength());

                        System.out.println("FROM SERVER:" + msg);
                        // If we receive an ack, stop the while loop
                        timedOut = false;
                    } catch (SocketTimeoutException exception) {
                        // If we don't get an ack, prepare to resend sequence number
                        System.out.println("Timeout (Sequence Number " + sequenceNumber + ")");
                        // The sequence number is subtracted so that
                        // the program knows to resend the dropped
                        // packet              
                        sequenceNumber--;

                    }
                }
            }

            socket.close();
        }
        if (input.contentEquals("GBN")) {
            userNum = Integer.parseInt(randomNumInput);
            // Get path for COSC635_P2_DataSent.txt file
            String currentDirPath = System.getProperty("user.dir");
            Path filepath = Paths.get(currentDirPath, "src", "main", "java", "com", "cosc635", "project2_gui", "COSC635_P2_DataSent.txt");
            System.out.println(filepath.toString());
            //Convert entire file to bytes
            byte[] totalBytes = Files.readAllBytes(filepath);
            try {//SETUP OF CONNECTION AND FILE
                DatagramSocket ds = new DatagramSocket();
                ds.setSoTimeout(3000); //arbitrary milliseconds
                // if random number did not recieve from gui, ask from command line
                if (Integer.toString(userNum).isEmpty()) {
                    userNum = packetLossSim();//user inputs number 0-99
                }
                start = System.nanoTime(); //start the timer
                while (true) {//BEGIN SENDING DATA
                    int eachRoundCompare = currentSeqNum + windowSize;
                    while (currentSeqNum < eachRoundCompare) {  //CHECK IF WINDOW SIZE HAS BEEN SENT
                        byte[] destination = setupPacket(max, currentSeqNum, totalBytes);
                        DatagramPacket pkt = new DatagramPacket(destination, destination.length, InetAddress.getLocalHost(), 8888);
                        int pseudoNum = new Random(System.currentTimeMillis()).nextInt(99); //pseudonumber generated using random seed set to current system time
                        System.out.println("Pseudonum is " + pseudoNum);

                        if (pseudoNum < userNum) { // SIMULATE LOSS ELSE SEND
                            System.out.println("Packet being lost = " + currentSeqNum);
                            ++packetLoss; //keep count of total packet losses
                            lostSeqNum = currentSeqNum;
                            ++currentSeqNum;
                            ++totalPacketsSent;
                        } else { //SEND PACKETS
                            ds.send(pkt);
                            System.out.println("currentSeqNum sent " + currentSeqNum);
                            ++currentSeqNum;
                            ++totalPacketsSent;
                        }
                        if (currentSeqNum == eachRoundCompare) { //if end of round check for ack
                            try {
                                System.out.println("Waiting for ack after sending packet number " + (currentSeqNum - 1));
                                byte[] ackBytes = new byte[1024]; //arbitrary number for ACK bytes
                                DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length); //create new Datagram packet for ACK coming in -- need to fill in parameters
                                ds.receive(ack);
                                System.out.println("ack received " + ack.getData());
                            } catch (SocketTimeoutException e) {
                                System.out.println("Timeout error, packet " + lostSeqNum + " lost");
                                currentSeqNum = goBackN(lostSeqNum);
                                System.out.println("The refreshed currentSeqNum after loss is " + currentSeqNum);
                            }
                        }
                        if (totalBytes.length < currentSeqNum * 4094) {
                            System.out.println("Packets sent: " + totalPacketsSent);
                            System.out.println("Lost packets: " + packetLoss);
                            return;
                        }
                    }
                }
            } finally {
                end = System.nanoTime(); //end the timer
                System.out.println("Elapsed time in nanoseconds: " + (end - start));
                System.out.println("Elapsed time in seconds: " + (end - start) / 1000000);
                System.out.println("Goodbye!");
            }
        }
    }
}
