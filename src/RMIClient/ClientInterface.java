package RMIClient;

import java.net.*;
import java.util.Scanner;
import java.io.*;

public class ClientInterface{
    public static int UDPPORT = 6789;
    private static final Scanner scanner = new Scanner(System.in);
    static String msg, hostname;
    static DatagramSocket mySocket;

    private static String readString(){
        String str;
        do{
            str = scanner.nextLine();
        }while(str.isEmpty());
        return str;
    }

    private static int readInt(){
        if(scanner.hasNextInt()){
            int i = scanner.nextInt();
            if (i <= 0) return -1;
            return i;
        }
        scanner.nextLine();
        return -1;
    }

    public static void main(String[] args){
        // argumentos da linha de comando: hostname
        if(args.length == 0){
            System.out.println("java UDPClient hostname");
            System.exit(0);
        }

        try (DatagramSocket aSocket = new DatagramSocket()) {
            mySocket = aSocket;
            hostname = args[0];
            int op;
            do{
                msg = null;
                System.out.println("Client Menu:");
                System.out.println("1 - Fazer login");
                System.out.println("2 - Indexar novo URL");
                System.out.println("3 - Pesquisar páginas que contenham um conjunto de termos");
                System.out.println("4 - Consultar lista de páginas com ligação para uma página específica");
                System.out.println("5 - Consultar página de administração");
                System.out.println("6 - Sair do programa");
                System.out.print("Option: ");
                op = readInt();
                switch(op) {
                    case 1 -> login();
                    case 2 -> indexUrl();
                    case 3 -> search();
                    case 4 -> searchPages();
                    case 5 -> msg = "type | admin";
                    case 6 -> msg = "type | logout";
                    default -> System.out.println("Invalid option!");
                }
            } while(op != 6);
        }catch (SocketException e){
            System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){
            System.out.println("IO_1: " + e.getMessage());
        }
    }

    private static void send() throws IOException {
        //System.out.println(msg);
        byte[] m = msg.getBytes();

        InetAddress aHost = InetAddress.getByName(hostname);
        DatagramPacket request = new DatagramPacket(m,m.length,aHost,UDPPORT);
        mySocket.send(request);
    }

    private static String[] receive() throws IOException {
        byte[] buffer = new byte[0];
        //aSocket.setSoTimeout(10000);

        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        mySocket.receive(reply);

        String[] res_splitted = new String(reply.getData()).split("[|;]");

        for(int i = 0; i < res_splitted.length; i++){
            res_splitted[i] = res_splitted[i].trim();
        }

        return res_splitted;
    }

    private static void login() throws IOException {
        String username, password;
        System.out.print("Username: ");
        username = readString();
        if(username == null) { System.out.println("Invalid username!"); }
        else{
            System.out.print("Password: ");
            password = readString();
            if(password == null) { System.out.println("Invalid password!"); }
            msg = "type | login ; username | " + username + " ; password | " + password;
            send();

            String[] res = receive(); // ex.: type | status ; logged | on ; msg | Welcome to the app
            System.out.println(res[5]);
        }
        System.out.println();
    }

    private static void indexUrl() throws IOException {
        String url;
        System.out.print("\nUrl to index: ");
        url = readString();
        if(url == null) { System.out.println("Invalid url!"); }
        else {
            msg = "type | index_url ; url | " + url;
            send();
        }
    }

    private static void search() throws IOException {
        int term_count, n_page = 0, count = 1;
        String[] terms;
        System.out.print("\nNumber of terms to search: ");
        term_count = readInt();
        if(term_count <= 0) { System.out.println("Invalid number!"); }
        else {
            terms = new String[term_count];
            for(int i = 0; i < term_count; i++){
                System.out.print("Term " + i + ": ");
                terms[i] = readString();
                if (terms[i] == null) { System.out.println("Invalid term!"); n_page = -1; break; }
            }
            if(n_page == 0){
                System.out.print("Number of the page to show: ");
                n_page = readInt();
                if (n_page < 0) { System.out.println("Invalid number!"); }
                else {
                    msg = "type | search ; term_count | " + term_count + " ; term_0 | ";
                    for(; count < term_count; count++){
                        msg += terms[count - 1] + " ; term_" + count + " | ";
                    }
                    msg += terms[count - 1] + " ; page | " + n_page;
                }
            }
            send();

            String[] res = receive();
            int item_count = Integer.parseInt(res[3]);
            System.out.print("Pages that contain the terms ");
            for(int i = 0 ; i < term_count; i++){
                System.out.print(terms[i]);
            }
            System.out.println(" - Page " + n_page);
            for (int i = 0; i < item_count; i++) {
                System.out.println("Title of the page" + res[5 + i * 6]);
                System.out.println("Complete Url: " + res[7 + i * 6]);
                System.out.println("Short citation: " + res[9 + i * 6] + '\n');
            }
            System.out.println();
        }
    }

    private static void searchPages() throws IOException {
        String url;
        int n_page;
        System.out.print("\nUrl: ");
        url = readString();
        if(url == null) { System.out.println("Invalid url!"); }
        else {
            System.out.print("Number of the page to show: ");
            n_page = readInt();
            if (n_page < 0) { System.out.println("Invalid number!"); }
            else {
                msg = "type | search_pages ; url | " + url + " ; page | " + n_page;
            }
            send();

            String[] res = receive();
            int item_count = Integer.parseInt(res[3]);
            System.out.print("Pages that have a link to " + url);
            System.out.println(" - Page " + n_page);
            for (int i = 0; i < item_count; i++) {
                System.out.println("Url " + (i+1) + ": " + res[5 + i * 2]);
            }
            System.out.println();
        }
    }
}