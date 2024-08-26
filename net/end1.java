
import java.io.*;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import java.util.*;

/**
 * Client class
 * <p>
 * An instance accepts user input
 */

public class end1 extends Node {
    static final int DEFAULT_SRC_PORT = 50000;
    static final int DEFAULT_DST_PORT = 50001;
    static final String DEFAULT_DST_NODE = "node1";
    InetSocketAddress dstAddress;


    String packet_type;
    String dst;
    boolean global_status = true;
    int endpoint_count = 0;


    /**
     * Constructor
     * <p>
     * Attempts to create socket at given port and create an InetSocketAddress for
     * the destinations
     */

    end1(String dstHost, int dstPort, int srcPort) {
        try {
            dstAddress = new InetSocketAddress(dstHost, dstPort);
            socket = new DatagramSocket(srcPort);
            listener.go();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    // From ChatGPT
    public static String mapToString(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            stringBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(", ");
        }
        String result = stringBuilder.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }
    // From ChatGPT, Get file total size

    // From ChatGPT
    public static Map<String, String> stringToMap(String input) {
        Map<String, String> resultMap = new HashMap<>();

        String[] keyValuePairs = input.split(", ");
        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                resultMap.put(key, value);
            }
        }

        return resultMap;
    }

    /**
     * Assume that incoming packets contain a String and print the string.
     */

    public static List<String> stringToList(String str) {
        return Arrays.asList(str.split(","));
    }


    // From ChatGPT read the file number
    public static int count_frame(String folderPath) {
        File folder = new File(folderPath);
        int Count = 0;

        if (folder.exists() && folder.isDirectory()) {

            File[] files = folder.listFiles();

            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".png")) {

                    Count++;
                }
            }
        } else {
            System.out.println("Folder not found or is not a directory.");
        }

        return Count;
    }

    public static byte[] loadPNGImage(String folderPath, String targetFileName) {
        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {

            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().equalsIgnoreCase(targetFileName)) {
                        try {

                            FileInputStream fileInputStream = new FileInputStream(file);
                            byte[] pngByteArray = new byte[(int) file.length()];
                            fileInputStream.read(pngByteArray);
                            fileInputStream.close();

                            return pngByteArray;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }
            }
        }

        System.out.println("Target PNG image not found in the folder.");
        return null;
    }

    private static int calculateFolderSize(String folderName) {

        File folder = new File(folderName);
        int totalSize = 0;

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            for (File file : files) {
                if (file.isFile()) {
                    totalSize += file.length();
                }
            }
        } else {
            System.out.println("Folder not found or is not a directory.");
        }

        return totalSize;

    }

    public synchronized void onReceipt(DatagramPacket packet) {
        try {

            PacketContent content = PacketContent.fromDatagramPacket(packet);
            Map<String, String> packetMap = stringToMap(((FileInfoContent) content).getHeader());
            packet_type = packetMap.get("packet type");
            if (content.getType() == PacketContent.FILEINFO) {

                if (packet_type.equals("answer")) {
                    System.out.println("the "+ dst + "receive!");
                    dst = packetMap.get("dst");
                    String path = packetMap.get("path");
                    List<String> final_path = stringToList(path);
                    List<String> ans = new ArrayList<>();
                    for (String s : final_path) {
                        ans.add(s);
                    }
                    ans.add("endpoint1");
                    Collections.reverse(ans);
                    System.out.print("the " + dst + " receive the message" +
                            "\nThe path is: " + " [ " + ans + "  ]" + "\n");
                    Thread.sleep(1000);
                    global_status = true;
                    this.notify();
                }else if(packet_type.equals("cancel_request")){
                    System.out.println("receive the stuff");
                    String request = packetMap.get("request");
                    if(request.equals("true")){
                        System.out.println("Already delete the path ");
                    }else {
                        System.out.println("path saved in Router");
                    }
                    global_status = true;
                    this.notify();
                }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sender Method
     */

    public synchronized void start() throws Exception {
        while (true) {
            global_status = true;
            HashMap<String, String> HeaderMap = new HashMap<>();
            FileInfoContent fcontent;
            String Header;
            String foldername = null;
            DatagramPacket packet;
            HeaderMap.put("packet type", "rout");
            HeaderMap.put("ID", "endpoint1");
            Scanner scanner = new Scanner(System.in);


            System.out.println("please type the endpoint name to send the message type the number to get the path :"
                    + "\n1.exit" + "\n2. endpoint2 " + "\n3. endpoint3" + "\n4. endpoint4 " + "\n5. endpoint5");

            System.out.println("please type 1-5: ");
            String signal = scanner.nextLine();
            if(HeaderMap.containsKey("dst")) {
                HeaderMap.remove("dst");
            }
                if (signal.equals("2")) {
                    HeaderMap.put("dst", "endpoint2");
                } else if (signal.equals("3")) {
                    HeaderMap.put("dst", "endpoint3");
                } else if (signal.equals("4")) {
                    HeaderMap.put("dst", "endpoint4");
                } else if (signal.equals("5")) {
                    HeaderMap.put("dst", "endpoint5");
                } else if (signal.equals("1")) {
                    System.out.print("the program stopped");
                    break;
                }



            System.out.println("please type the frame you want to send to: \n1. 20 frame\n2.600frames");
            String name = scanner.nextLine();
            if (name.equals("1")) {
                foldername = "20";
            } else if (name.equals("2")) {
                foldername = "600";
            }

            if (global_status == false) {

                this.wait();

            }

            int folder_size = calculateFolderSize(foldername);
            String Filesize = String.valueOf(folder_size);
            HeaderMap.put("file total size", Filesize);

            int frame_size = count_frame(foldername);
            String framesize = String.valueOf(frame_size);
            HeaderMap.put("frame number", framesize);

            int totalFrames = count_frame(foldername);
            int sentFrames = 0;
            int frameNumber = 0;



            String fileName = String.format("frame%03d.png", frameNumber);
        if (sentFrames != 0) {
            System.out.println(fileName); // output：frame001.png
        }
            while (sentFrames < totalFrames) {
                try {

                    frameNumber++;

                    sentFrames++;
                    String sendframe = String.valueOf(sentFrames);
                    HeaderMap.put("current", sendframe);
                    fileName = String.format("frame%03d.png", frameNumber);
                    byte[] pngFileName = loadPNGImage(foldername, fileName);
                    int length = pngFileName.length;

                    HeaderMap.put("PNG name", fileName);

                    Header = mapToString(HeaderMap);
                    System.out.println("File size: " + folder_size);
                    fcontent = new FileInfoContent(Header, length, pngFileName);

                    System.out.println("Sending packet: " + fileName + " The Size is: " + length);


                    packet = fcontent.toDatagramPacket();
                    packet.setSocketAddress(dstAddress);
                    socket.send(packet);
                    System.out.println("Packet sent to: " + dstAddress);
                    this.wait();


                } catch (Exception e) {
                    e.printStackTrace();
                }

                global_status = false;
            }

        }

    }


    /**
     * Test method
     * <p>
     * Sends a packet to a given address
     */

    public static void main(String[] args) {
        try {
            (new end1(DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_SRC_PORT)).start();
            System.out.println("Program completed");
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }
}
