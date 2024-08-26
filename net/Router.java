
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.*;


public class Router extends Node {
    static final int DEFAULT_PORT = 50001;
    static final int DEFAULT_DST_PORT = 50000;

    InetSocketAddress dstAddress;


    String status;

    String dst_status;
    String Header;

    HashMap<String, String> dstMap = new HashMap<>();
    List<String> IPlist = new ArrayList<>();
    HashMap<String, String> toMap = new HashMap<>();
    HashMap<String, String> backMap = new HashMap<>();
    int count = 0;

    /*
     *
     */
    String previous;
    String packet_type; // video / message / routmessage
    int frame_size;
    String ID;

    String dst;       //destination for the broadcast message

    String router_name = "node1";

    String current_frame_number;
    String total_frame_number;
    byte[] frame;
    byte[] response;
    boolean global_status = false;
    int message_size;
    String pathget;
    String PNG_name;
    String request;

    Router(int port) {
        try {
            socket = new DatagramSocket(port);

            IPlist.add("192.19.0.255");
            IPlist.add("192.20.1.255");
            listener.go();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> stringToList(String str) {
        return Arrays.asList(str.split(","));
    }

    public static String listToString(List<String> list) {
        StringJoiner joiner = new StringJoiner(",");
        for (String s : list) {
            joiner.add(s);
        }
        return joiner.toString();
    }
    // From ChatGPT read the file number

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


    public synchronized void onReceipt(DatagramPacket packet) {


        try {
            PacketContent content = PacketContent.fromDatagramPacket(packet);
            if (content.getType() == PacketContent.FILEINFO) {

                Map<String, String> packetMap = stringToMap(((FileInfoContent) content).getHeader());
                // ---------------------- Header to map
                packet_type = packetMap.get("packet type");

                if (packet_type.equals("rout")) {
                    ID = packetMap.get("ID");
                    if (!ID.equals(router_name)) {
                        dst = packetMap.get("dst");
                        PNG_name = packetMap.get("PNG name");
                        ID = packetMap.get("ID");
                        status = packetMap.get("status");
                        dst_status = packetMap.get("dst_status");
                        current_frame_number = packetMap.get("current");
                        total_frame_number = packetMap.get("frame number");
                        System.out.println("--------Receive the packet---------");
                        System.out.println("receive the frame--> " + packetMap.get("PNG name"));
                        frame = ((FileInfoContent) content).getpacket();
                        if (!backMap.containsKey(router_name)) {
                            backMap.put(router_name, ID);
                            System.out.println("save the previous node");
                        }

                        global_status = true;
                        this.notify();
                    }
                } else if (packet_type.equals("answer")) { // if receive an answer content
                    packet_type = packetMap.get("packet type");
                    ID = packetMap.get("ID");
                    status = packetMap.get("status");
                    dst_status = packetMap.get("dst_status");
                    current_frame_number = packetMap.get("current");
                    total_frame_number = packetMap.get("frame number");
                    response = ((FileInfoContent) content).getpacket();
                    message_size = response.length;
                    if (!dstMap.containsKey(dst)) {
                        dstMap.put(dst, router_name);
                        System.out.println("save the dst details");
                    }
                    if (!toMap.containsKey(router_name)) {
                        toMap.put(router_name, ID);
                        System.out.println("path road saved!!!!");
                    }
                    pathget = packetMap.get("path");

                    System.out.println("receive the answer");

                    global_status = true;
                    this.notify();

                } else if (packet_type.equals("cancel_request")) {
                    System.out.println("get the request!!!!!!");
                    request  = packetMap.get("request");
                    previous = backMap.get(router_name);
                    HashMap<String,String> HeaderMap = new HashMap<>();

                    if(request.equals("true")){
                        dstMap.clear();
                        backMap.clear();
                        toMap.clear();
                        System.out.println("Map cleared");

                    }
                    HeaderMap.put("request","true");
                    HeaderMap.put("request","false");
                    HeaderMap.put("packet type","cancel_request");
                    String Hash = mapToString(HeaderMap);
                    FileInfoContent  response_content = new FileInfoContent(Hash, message_size, response);
                    DatagramPacket  response_packet = response_content.toDatagramPacket();
                    dstAddress = new InetSocketAddress(previous, 50000); // if exist dst in this subnet,send the message
                    response_packet.setSocketAddress(dstAddress);
                    System.out.println("the answer send to " + dstAddress);
                    socket.send(response_packet);

                }
            }
        }
        //if we get response

        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public synchronized void start() throws Exception {
        System.out.println("Waiting for contact");
        while (true) {
            DatagramPacket nodepacket = null;
            FileInfoContent fcontent;


            while (global_status == false) {
                this.wait();
            }
            if (packet_type.equals("rout")) {
                System.out.println("\n----------------------------");

                HashMap<String, String> HeaderMap = new HashMap<>();
                HeaderMap.put("ID", router_name);
                HeaderMap.put("packet type", "rout");
                HeaderMap.put("dst", dst);
                HeaderMap.put("current", current_frame_number);
                HeaderMap.put("frame number", total_frame_number);
                HeaderMap.put("PNG name",PNG_name);
                frame_size = frame.length;
                String Header = mapToString(HeaderMap);
                if (!dstMap.containsKey(dst)) {

                    for (String IP : IPlist) {
                        fcontent = new FileInfoContent(Header, frame_size, frame);
                        nodepacket = fcontent.toDatagramPacket();
                        if (IP.equals("192.19.0.255")) {
                            dstAddress = new InetSocketAddress(IP, DEFAULT_DST_PORT);
                        } else {
                            dstAddress = new InetSocketAddress(IP, 50001);
                        }
                        nodepacket.setSocketAddress(dstAddress);
                        System.out.println("packet send to" + IP);
                        socket.send(nodepacket);
                    }

                } else {

                    String dst_address = toMap.get(router_name);
                    fcontent = new FileInfoContent(Header, frame_size, frame);
                    nodepacket = fcontent.toDatagramPacket();
                    System.out.println("----%%%%%%%%%---- " + dst_address);
                    if (dst_address.startsWith("node")) {
                        dstAddress = new InetSocketAddress(dst_address, 50001);
                    } else if(dst_address.startsWith("endpoint")) {
                        dstAddress = new InetSocketAddress(dst_address, DEFAULT_DST_PORT);
                    }

                    nodepacket.setSocketAddress(dstAddress);
                    System.out.println("packet send to " + dstAddress);
                    socket.send(nodepacket);

                }
            } else if (packet_type.equals("answer")) {
                DatagramPacket response_packet;
                FileInfoContent response_content;
                HashMap<String, String> HeaderMap = new HashMap<>();
                List<String> path_learn = new ArrayList<>();
                List<String> path = stringToList(pathget);
                for (String s : path) {
                    path_learn.add(s);
                }
                path_learn.add(router_name);
                String path_road = listToString(path_learn);
                HeaderMap.put("path", path_road);
                HeaderMap.put("packet type", "answer");
                HeaderMap.put("ID", router_name);
                HeaderMap.put("dst", ID);
                String Header = mapToString(HeaderMap);
                response_content = new FileInfoContent(Header, message_size, response);
                response_packet = response_content.toDatagramPacket();

                previous = backMap.get(router_name);
                dstAddress = new InetSocketAddress(previous, 50000); // if exist dst in this subnet,send the message
                response_packet.setSocketAddress(dstAddress);
                System.out.println("the answer send to " + dstAddress);
                socket.send(response_packet);

            }
            global_status = false;
        }

    }


    /*
     *
     */
    public static void main(String[] args) {
        try {
            (new Router(DEFAULT_PORT)).start();
            System.out.println("Program completed");
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }
}