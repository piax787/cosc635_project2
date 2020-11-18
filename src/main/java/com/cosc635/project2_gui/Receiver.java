/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cosc635.project2_gui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.stage.Stage;

public class Receiver {

    static int goBackN(int lostSeqNum) {
//        each step adds one because we are 1 based indexing
        if (lostSeqNum % 4 == 0) {
            return lostSeqNum - 4 + 1;
        } else {
            return lostSeqNum - (lostSeqNum % 4) + 1;
        }
    }

    public static void start() throws IOException {
        String currentDirPath = System.getProperty("user.dir");
        Path filepath = Paths.get(currentDirPath, "src", "main", "java", "com", "cosc635", "project2_gui", "COSC635_P2_DataReceived.txt");
        System.out.println(filepath.toString());
        File destfile = new File(filepath.toString());
//        File destfile = new File("COSC635_P2_DataReceived.txt");
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
                    bos.write(currentMessageSet.getBytes());// We can add this later so we don't need to continue rewriting
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
            System.out.println("after round " + currentSeqNum + "currentMessageSet is " + currentMessageSet);
            buff.clear();
            buff.rewind(); //reset buffer
        }
    }
};
