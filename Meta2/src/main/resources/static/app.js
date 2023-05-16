var stompClient = null;
var intervalId = null; // Variable to hold the interval ID

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#messages").html("");
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
        // Start the interval to fetch admin info every 1 second
        intervalId = setInterval(fetchAdminInfo, 1000);
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

function logout() {
    if (confirm("Are you sure you want to log out?")) {
        alert("Logged out successfully.");
        window.location.href = "/logout";
    }
}

function fetchAdminInfo() {
    $.ajax({
        url: "/admin/info",
        type: "GET",
        success: function (response) {
            showAdminInfo(response);
        },
        error: function (xhr, status, error) {
            console.log("Error fetching admin info:", error);
        }
    });
}

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
