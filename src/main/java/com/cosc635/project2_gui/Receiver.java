/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cosc635.project2_gui;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Receiver {

    static int goBackN(int lostSeqNum) {
//        each step adds one because we are 1 based indexing
        if (lostSeqNum % 4 == 0) {
            return lostSeqNum - 4 + 1;
        } else {
            return lostSeqNum - (lostSeqNum % 4) + 1;
        }
    }

    public static void start(String protocolChoice, String randomNumInput) throws IOException {
        double choice = 0.0;
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
            // Create a server socket
            DatagramSocket serverSocket = new DatagramSocket(8888);

            // Set up byte arrays for sending/receiving data
            byte[] receiveData = new byte[4096];
            byte[] dataForSend = new byte[4096];

            //Initialization of error and success counters
            int error = 0;
            int success = 0;

            if (randomNumInput.isEmpty()) {
                System.out.println("What is the percentage of time that you want a packet to not be sent?(enter a number between 1 and 99)");
                Scanner percentage = new Scanner(System.in);
                choice = percentage.nextDouble();
            } else {
                choice = Double.parseDouble(randomNumInput);
            }

            long startTime = System.currentTimeMillis();
            // Infinite loop to check for connections 
            while (true) {

                int randomNum = new Random(System.currentTimeMillis()).nextInt();
                // Get the received packet
                DatagramPacket received = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(received);

                // Get the message from the packet
                String msg = new String(received.getData(), received.getOffset(), received.getLength());

                // Probability of successful transmission of data
                if (randomNum > choice) {
                    success++;
                    System.out.println("FROM CLIENT: " + msg);

                    // Get packet's IP and port
                    InetAddress IPAddress = received.getAddress();
                    int port = received.getPort();

                    // Creation of acknowledgment message/packet 
                    String ack = "received";
                    dataForSend = ack.getBytes();

                    // Send the packet data back to the client
                    DatagramPacket packet = new DatagramPacket(dataForSend, dataForSend.length, IPAddress, port);
                    serverSocket.send(packet);
                } else {
                    error++;
                    System.out.println("Oops, packet was dropped");
                }
                //Final results of SAW transmission (Time;Packets sent;Packets dropped;Packets received)
                int time = (int) (System.currentTimeMillis() - startTime);
                int seconds = time / 1000;
                int packets = error + success;
                System.out.println("Total packets lost:" + error);
                System.out.println("Total packets received:" + success);
                System.out.println("Total time:" + seconds + "seconds");
                System.out.println("Total packets sent:" + packets);

            }
        }
        if (input.contentEquals("GBN")) {
            String currentDirPath = System.getProperty("user.dir");
            Path filepath = Paths.get(currentDirPath, "src", "main", "java", "com", "cosc635", "project2_gui", "COSC635_P2_DataReceived.txt");
            System.out.println(filepath.toString());
            File destfile = new File(filepath.toString());
//            File destfile = new File("COSC635_P2_DataReceived.txt");
            FileOutputStream fos = new FileOutputStream(destfile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            DatagramSocket ds = new DatagramSocket(8888);// open port to listen
            byte[] receive = new byte[4096];
            ByteBuffer buff = ByteBuffer.wrap(receive);
            DatagramPacket DpReceive = null;
            int lastPacketReceived = 0;
            String currentMessageSet = "";

            while (true) {
                System.out.println("Server is awaiting packets...");
                DpReceive = new DatagramPacket(receive, receive.length); // create appropriate sized data packet
                ds.receive(DpReceive);// retrieve data
                String msg = new String(DpReceive.getData(), DpReceive.getOffset(), DpReceive.getLength());// to format the bytes back into strings
                int currentSeqNum = DpReceive.getData()[0];
                int lastPacket = DpReceive.getData()[1];
                System.out.println("Received currentSeqNum is " + currentSeqNum);
                System.out.println(currentSeqNum + " " + lastPacketReceived);

                if (currentSeqNum == lastPacketReceived + 1) {//if this is the next packet, then append to string
                    currentMessageSet = currentMessageSet.concat(msg.substring(2));//append the text without the sequence number
                    lastPacketReceived = currentSeqNum;

                    if ((currentSeqNum + 1) % 4 == 0 && currentSeqNum != 0 || lastPacket == -1) {//time to deal with ack ///////////////////////////////////
                        byte[] ackData = new byte[1024];
                        DatagramPacket ack = new DatagramPacket(ackData, ackData.length, DpReceive.getAddress(), DpReceive.getPort());
                        ds.send(ack);
                        System.out.println("Sent ack for current SeqNum " + currentSeqNum);
//                    System.out.println("currentMessageSet" + currentMessageSet);
                        bos.write(currentMessageSet.getBytes());
                        if (lastPacket == -1) { // finished, so close connection
                            ds.close();
                            return;
                        }
                        currentMessageSet = "";
                    }
                } else {
                    System.out.println("Missed a packet, deleting current round");
                    currentMessageSet = "";
                    lastPacketReceived = goBackN(currentSeqNum);
                }
//            System.out.println("after round "+ currentSeqNum +"currentMessageSet is " + currentMessageSet);
                buff.clear();
                buff.rewind(); //reset buffer
            }
        }
    }
}
