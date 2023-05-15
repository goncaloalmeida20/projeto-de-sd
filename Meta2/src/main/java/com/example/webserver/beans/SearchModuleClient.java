package com.example.webserver.beans;

import com.example.webserver.the_data.AdminInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class SearchModuleClient {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedDelay = 5000) // Schedule the method to run every 5 seconds
    public void sendRequestToServer() {
        // Send a request to the local server
        // Retrieve the number of downloads and barrels active

        // Assuming you have a method to retrieve the admin information from the local server
        AdminInfo adminInfo = retrieveAdminInfoFromServer();

        // Send the admin information to the subscribed clients
        messagingTemplate.convertAndSend("/topic/admin-info", adminInfo);
    }

    private AdminInfo retrieveAdminInfoFromServer() {
        //TODO: Implement the logic to connect to the local server and retrieve the admin information via RMI

        // Placeholder implementation
        AdminInfo adminInfo = new AdminInfo();
        adminInfo.setNumDownloads(2);
        adminInfo.setNumActiveBarrels(3);
        adminInfo.setMostSearchedItems(new ArrayList<>());

        return adminInfo;
    }
}
