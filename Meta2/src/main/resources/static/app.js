var stompClient = null;

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
