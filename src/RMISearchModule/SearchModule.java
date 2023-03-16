package RMISearchModule;

import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import URLQueue.URLQueueStarter;
import URLQueue.URLQueue_I;
import IndexStorageBarrels.SearchIf;
import IndexStorageBarrels.SearchServer;
import RMIClient.ClientInterface;
import classes.Page;

public class SearchModule {
    static HashMap<String, Thread> threadMap = new HashMap<String, Thread>();

    public static void main(String[] args) throws InterruptedException, NotBoundException{
        try (DatagramSocket aSocket = new DatagramSocket(ClientInterface.UDPPORT)) {
            while(true){
                byte[] buffer = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String s = new String(request.getData(), 0, request.getLength());

                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();
                String clientId = clientAddress.getHostAddress() + ":" + clientPort;
                Thread clientThread = threadMap.get(clientId);

                AtomicBoolean close_client_t = new AtomicBoolean(false);

                // Verify if already exists a thread associated with the client that made the request
                if (clientThread == null || !clientThread.isAlive()) {
                    clientThread = new Thread(() -> {try {
                        close_client_t.set(runRequest(s, aSocket, request, false));
                    } catch (NotBoundException e) {
                        System.out.println("NBE: " + e.getMessage());
                    }});
                    threadMap.put(clientId, clientThread);
                    clientThread.start();
                } else {
                    close_client_t.set(runRequest(s, aSocket, request, true));
                }
                if(close_client_t.get()){
                    clientThread.interrupt();
                }
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }

    public static boolean runRequest(String s, DatagramSocket aSocket, DatagramPacket request, Boolean c_on) throws NotBoundException{
        try{
            SearchIf si = (SearchIf) LocateRegistry.getRegistry(SearchServer.PORT0).lookup(SearchServer.arg0);

            String[] types = {"login", "status", "url_list", "index_url", "search", "search_response", "search_pages", "admin", "logout"};
            int type = 0;

            String[] s_splitted = s.split("[|;]");

            for(int i = 0; i < s_splitted.length; i++){
                s_splitted[i] = s_splitted[i].trim();
            }

            // type | login ; username | tintin ; password | unicorn
            if (s_splitted[1].equals(types[0])){
                String msg;
                if(c_on) { msg = "type | status ; logged | on ; msg | Already logged on\n";}
                else { msg = "type | status ; logged | on ; msg | Welcome to the app\n";}

                byte[] m = msg.getBytes();
                DatagramPacket reply = new DatagramPacket(m, m.length, request.getAddress(), request.getPort());
                aSocket.send(reply);
            }
            // type | index_url ; url | www.not_uc.pt
            else if (s_splitted[1].equals(types[3])){
                String url = s_splitted[3];
                URLQueue_I uqi = (URLQueue_I) LocateRegistry.getRegistry(URLQueueStarter.URLQUEUE_PORT).lookup(URLQueueStarter.URLQUEUE_NAME);
                uqi.addURL(url);
            }
            // type | search ; term_count | 2 ; term_0 | Portugal ; term_1 | Espanha ; page | 1
            else if (s_splitted[1].equals(types[4])){
                type = 4;
                int term_count = Integer.parseInt(s_splitted[3]);
                String[] terms = new String[term_count];
                for (int i = 0; i < term_count; i++){
                    terms[i] = s_splitted[5 + i * 2];
                }

                ArrayList<Page> pages = si.search(terms, Integer.parseInt(s_splitted[s_splitted.length - 1]));

                int item_count = pages.size();
                String msg = "type | search_url_list ; item_count | " + item_count;
                for(int i = 0; i < item_count; i++){
                    msg += "; item_" + i + "_title | " + pages.get(i).title +
                            "; item_" + i + "_url | " + pages.get(i).url +
                            "; item_" + i + "_citation" + pages.get(i).citation;
                }
                byte[] m = msg.getBytes();
                DatagramPacket reply = new DatagramPacket(m, m.length, request.getAddress(), request.getPort());
                aSocket.send(reply);
            }
            // type | search_pages ; url | www.not_uc.pt ; page | 1
            else if (s_splitted[1].equals(types[6])){
                type = 6;
                ArrayList<Page> pages = si.search_pages(s_splitted[3], Integer.parseInt(s_splitted[5]));
                int item_count = pages.size();
                String msg = "type | search_pages_url_list ; item_count | " + item_count;
                for(int i = 0; i < item_count; i++){
                    msg += "; item_" + i + "_url | " + pages.get(i).url;
                }
                byte[] m = msg.getBytes();
                DatagramPacket reply = new DatagramPacket(m, m.length, request.getAddress(), request.getPort());
                aSocket.send(reply);
            }
            // type | admin
            else if (s_splitted[1].equals(types[7])){
                // TODO: GET THAT INFORMATION
            }
            // type | logout
            else if (s_splitted[1].equals(types[8])){
                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();
                String clientId = clientAddress.getHostAddress() + ":" + clientPort;
                Thread clientThread = threadMap.remove(clientId);

                String msg = "type | status ; logged | off ; msg | Bye!!\n";
                byte[] m = msg.getBytes();
                DatagramPacket reply = new DatagramPacket(m, m.length, request.getAddress(), request.getPort());
                aSocket.send(reply);
                return true;
            }
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
        return false;
    }
}