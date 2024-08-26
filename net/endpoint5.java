
/**
 *
 */

import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import java.util.*;

/**
 * Client class
 * <p>
 * An instance accepts user input
 */
public class endpoint5 extends Node {
    static final int DEFAULT_SRC_PORT = 50000;
    static final int DEFAULT_DST_PORT = 50001;

    String Endpoint = "endpoint5";
    InetSocketAddress dstAddress;

    String packet_type;
    String total_frame_number;
    String current_frame_number;
    HashMap<String,String> pathMap = new HashMap<>();
    boolean global_status = false;
    boolean send_cancel_request = false;
    String path_dst;
    /**
     * Constructor
     * <p>
     * Attempts to create socket at given port and create an InetSocketAddress for
     * the destinations
     */

    endpoint5(int port) {
        try {
            socket = new DatagramSocket(port);


            listener.go();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    private static String mapToString(Map<String, String> map) {
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
    public synchronized void onReceipt(DatagramPacket packet) {



        try {
            PacketContent content = PacketContent.fromDatagramPacket(packet);

            if (content.getType() == PacketContent.FILEINFO) {
                Map<String, String> packetMap = stringToMap(((FileInfoContent) content).getHeader());
                packet_type = packetMap.get("packet type");
                total_frame_number = packetMap.get("frame number");
                current_frame_number = packetMap.get("current");
                int current_frame = Integer.parseInt(current_frame_number);
                int total_frame = Integer.parseInt(total_frame_number);
                if (packetMap.get("packet type").equals("rout")&& packetMap.get("dst").equals(Endpoint)) {
                    System.out.println("==================\nreceive\n=================");
                    System.out.println("receive the --> "+packetMap.get("PNG name"));
                    path_dst = packetMap.get("ID");

                    if(current_frame == total_frame){
                        HashMap<String,String> requestMap = new HashMap<>();
                        requestMap.put("ID",Endpoint);
                        requestMap.put("packet type","cancel_request");
                        System.out.println("You already received all the frames, you want to cancel the path or not? please type[y/n]");
                        Scanner request = new Scanner(System.in);
                        String request_string = request.nextLine();
                        if(request_string.equals("y")){
                            requestMap.put("request","true");
                            System.out.println("Send the request to the all the router and delete the path");
                            byte[] requestBytes = request_string.getBytes("UTF-8");
                            int request_size = requestBytes.length;
                            DatagramPacket node_packet ;
                            String Header = mapToString(requestMap);
                            FileInfoContent fcontent = new FileInfoContent(Header, request_size, requestBytes);
                            dstAddress = new InetSocketAddress(path_dst, DEFAULT_DST_PORT);
                            node_packet = fcontent.toDatagramPacket();
                            node_packet.setSocketAddress(dstAddress);
                            socket.send(node_packet);
                            System.out.println("Packet sent to: " + dstAddress);
                        }else{
                            requestMap.put("request","false");
                            System.out.println("the Router will save your path");
                            byte[] requestBytes = request_string.getBytes("UTF-8");
                            int request_size = requestBytes.length;

                            DatagramPacket node_packet ;
                            String Header = mapToString(requestMap);
                            FileInfoContent fcontent = new FileInfoContent(Header, request_size, requestBytes);
                            dstAddress = new InetSocketAddress(path_dst, DEFAULT_DST_PORT);
                            node_packet = fcontent.toDatagramPacket();
                            node_packet.setSocketAddress(dstAddress);
                            socket.send(node_packet);
                        }
                        global_status = false;

                    }else{
                        global_status = true;
                        this.notify();
                    }


                }

            } else {
                System.out.println("\n-----------------------\n");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public synchronized void start() throws Exception {
        FileInfoContent fcontent;
        DatagramPacket node_packet;
        while(true) {
            if(global_status == false){
                this.wait();
            }
            System.out.println("receive the message from Endpoint1");
            HashMap<String, String> HeaderMap = new HashMap<>();
            HeaderMap.put("ID", "endpoint5");
            HeaderMap.put("packet type", "answer");
            HeaderMap.put("path", "endpoint5");
            HeaderMap.put("dst",Endpoint);
            byte[] requestBytes = Endpoint.getBytes("UTF-8");
            int request_size = requestBytes.length;
            String Header = mapToString(HeaderMap);
            fcontent = new FileInfoContent(Header, request_size, requestBytes);

            System.out.println("Sending packet "); // Send packet with file name and length
            node_packet = fcontent.toDatagramPacket();
            dstAddress = new InetSocketAddress(path_dst, DEFAULT_DST_PORT);
            node_packet.setSocketAddress(dstAddress);
            socket.send(node_packet);
            System.out.println("Packet sent to: " + dstAddress);
            global_status = false;
            this.wait();
        }
    }

    /**
     * Test method
     * <p>
     * Sends a packet to a given address
     */

    public static void main(String[] args) {
        try {
            (new endpoint5(DEFAULT_SRC_PORT)).start();
            System.out.println("Program completed");
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }
}

