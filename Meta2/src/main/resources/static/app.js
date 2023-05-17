//===========================================================================
// WebSockets
//===========================================================================

var stompClient = null;
var intervalId = null;

function setConnected(connected) {
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#admininformations").html("");
}

function connect() {
    var socket = new SockJS("/my-websocket");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log("Connected: " + frame);
        stompClient.subscribe("/topic/admin", function (adminInfo) {
            var parsedAdminInfo = JSON.parse(adminInfo.body);
            showAdminInfo(parsedAdminInfo);
        });
    }, function (error) {
        console.log("Error connecting to WebSocket:", error);
    });
}


function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
    clearInterval(intervalId);
}

function fetchAdminInfo() {
    $.ajax({
        url: "/admin/info",
        type: "GET",
        success: function (response) {
            sendAdminInfo(response);
        },
        error: function (xhr, status, error) {
            console.log("Error fetching admin info:", error);
        }
    });
}

function sendAdminInfo(response) {
    var adminInfo = {
        numDownloads: response.numDownloads,
        numActiveBarrels: response.numActiveBarrels,
        mostSearchedItems: response.mostSearchedItems
    };
    console.log(adminInfo);

    if (stompClient && stompClient.connected) { // Check if stompClient is not null and connected
        stompClient.send("/app/admin", {}, JSON.stringify(adminInfo));
    } else {
        console.log("WebSocket connection is not established or has been closed.");
    }
}

function showAdminInfo(adminInfo) {
    $("#admininformations").html(
        "<tr><td><b>Number of Downloads:</b></td><td>" +
        adminInfo.numActiveBarrels +
        "</td></tr>" +
        "<tr><td><b>Number of Active Barrels:</b></td><td>" +
        adminInfo.numDownloads +
        "</td></tr>" +
        "<tr><td><b>Most Searched Items:</b></td><td>" +
        adminInfo.mostSearchedItems +
        "</td></tr>"
    )
    ;}

$(function () {
    $("form").on("submit", function (e) {
        e.preventDefault();
    });
    $("#connect").click(function () {
        connect();
    });
    $("#disconnect").click(function () {
        disconnect();
    });
});

//===========================================================================
// Client
//===========================================================================

function logout() {
    if (confirm("Are you sure you want to log out?")) {
        $.ajax({
            url: "/logout",
            type: "POST",
            success: function (response) {
                if (response === "logged-off") {
                    openPopup("Success", "Logged out successfully.");
                    setTimeout(function () {
                        window.location.href = "/guest";
                    }, 3000);
                } else if (response === "is-guest") {
                    openPopup("Error", "You are already logged out.");
                    setTimeout(function () {
                        window.location.href = "/guest";
                    }, 3000);
                } else {
                    console.log("Unknown response:", response);
                }
            },
            error: function (xhr, status, error) {
                console.log("Error logging out:", error);
            }
        });
    }
}

//===========================================================================
// Popups
//===========================================================================

function openPopup(title, message) {
    document.getElementById("popup").style.display = "block";
    document.getElementById("popupTitle").innerText = title;
    document.getElementById("popupMessage").innerText = message;
}

function closePopup() {
    document.getElementById("popup").style.display = "none";
}