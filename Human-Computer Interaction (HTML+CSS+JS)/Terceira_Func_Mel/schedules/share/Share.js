function shareR() {
    var to = sessionStorage.getItem("inputSchedules");
    var time = sessionStorage.getItem("timeShare");
    var from = sessionStorage.getItem("fromShare");
    var transport = sessionStorage.getItem("transport");
    document.getElementById("desc").innerHTML =  "<br>" + "<b>To: </b> " + to + "<br>" + "<b>From: </b> " +from + "<br>" + "<b>By: </b>"+ transport + "<br>" + "<b>At: </b>" + time;
    switch(transport) {
        case "Bus":
            document.getElementById("transportType").src = "../../images/bus.png";
        break;
        case "Airplane":
            document.getElementById("transportType").src = "../../images/plane.png";
        break;
        case "Train":
            document.getElementById("transportType").src = "../../images/train.svg";
        break;
        case "Subway":
            document.getElementById("transportType").src = "../../images/subway.png";
        break;
    }
}

function noModal() {
    document.getElementById("modal_info").innerHTML = "Not Shared!";
    document.getElementById("modal_id").style.display = "block";
    document.getElementById("back").setAttribute('disabled', 'disabled');
    document.getElementById("home").setAttribute('disabled', 'disabled');
    document.getElementById("lock").setAttribute('disabled', 'disabled');
}

function yesModal() {
    document.getElementById("modal_info").innerHTML = "Successfully Shared!";
    document.getElementById("modal_id").style.display = "block";
    document.getElementById("back").setAttribute('disabled', 'disabled');
    document.getElementById("home").setAttribute('disabled', 'disabled');
    document.getElementById("lock").setAttribute('disabled', 'disabled');
}