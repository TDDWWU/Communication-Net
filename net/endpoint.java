
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
public class endpoint extends Node {
    static final int DEFAULT_SRC_PORT = 50000;
    static final int DEFAULT_DST_PORT = 50001;

    String Endpoint = "endpoint2";
    InetSocketAddress dstAddress;


    /**
     * Constructor
     * <p>
     * Attempts to create socket at given port and create an InetSocketAddress for
     * the destinations
     */

    endpoint(int port) {
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

    public static List<String> bytesToList(byte[] bytes) {
        try {
            String joinedString = new String(bytes, "UTF-8");
            return Arrays.asList(joinedString.split(","));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] listToBytes(List<String> list) {
        StringJoiner joiner = new StringJoiner(",");
        for (String s : list) {
            joiner.add(s);
        }
        try {
            return joiner.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * Assume that incoming packets contain a String and print the string.
     */
    public synchronized void onReceipt(DatagramPacket packet) {
        FileInfoContent fcontent;
        String Header;
        DatagramPacket node_packet;


        try {
            PacketContent content = PacketContent.fromDatagramPacket(packet);

            if (content.getType() == PacketContent.FILEINFO) {
              
                Map<String, String> packetMap = stringToMap(((FileInfoContent) content).getHeader());
                List<String> pathlist = bytesToList(((FileInfoContent) content).getpacket());


                if (packetMap.get("packet type").equals("rout")) {
                    System.out.println("receive=================");
                    for (String s : pathlist) {
                        System.out.println(s);
                    }
                    if (packetMap.get("dst").equals(Endpoint)) {
                        HashMap<String, String> HeaderMap = new HashMap<>();
                        List<String> path = new ArrayList<>();
                        HeaderMap.put("ID", Endpoint);
                        HeaderMap.put("packet type", "answer");
                        String path_dst = pathlist.get(pathlist.size() - 1);
                        path.addAll(pathlist);
                        path.add(Endpoint);
                        System.out.println("receive the message from Endpoint1");
                        byte[] requestBytes = listToBytes(path);
                        int request_size = requestBytes.length;
                        Header = mapToString(HeaderMap);
                        fcontent = new FileInfoContent(Header, request_size, requestBytes);

                        System.out.println("Sending packet "); // Send packet with file name and length
                        node_packet = fcontent.toDatagramPacket();
                        dstAddress = new InetSocketAddress(path_dst, DEFAULT_DST_PORT);
                        node_packet.setSocketAddress(dstAddress);
                        socket.send(node_packet);
                        System.out.println("Packet sent to: " + dstAddress);

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
        this.wait();
    }

    /**
     * Test method
     * <p>
     * Sends a packet to a given address
     */

    public static void main(String[] args) {
        try {
            (new endpoint(DEFAULT_SRC_PORT)).start();
            System.out.println("Program completed");
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }
}

