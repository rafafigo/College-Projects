var ts_stop = {
    0: ["1232", "Oriente", "#f3a683"],

    1: ["1324", "Rossio", "#f7d794"],
    
    2: ["4532", "Carcavelos", "#778beb"],
    
    3: ["7823", "Cascais", "#e77f67"],

    4: ["4357", "Oeiras", "#cf6a87"],

    5: ["3421", "Queluz", "#34ace0"],

    6: ["6785", "Cacém", "#f8a5c2"],

    7: ["9876", "Estoril", "#63cdda"],

    8: ["8342", "Belem", "#ea8685"],

    9: ["4523", "Belas", "#33d9b2"],

}

var pl_stop = {
    
    0: ["4523", "Aero. Alentejo", "#f3a683"],

    1: ["3943","Aero. Lisboa", "#f7d794"],

    2: ["2122", "Aero. Minho", "#778beb"],
    
    3: ["4532", "Aero. Porto", "#e77f67"],
    
    4: ["7823", "Aero. Algarve", "#cf6a87"],

    5: ["4357", "Aero. Braga", "#34ace0"],

    6: ["3421", "Aero. Leiria", "#f8a5c2"],

    7: ["6785", "Aero. Setúbal", "#63cdda"],

    8: ["9876", "Aero. Faro", "#ea8685"],

    9: ["8342", "Aero. Bragança", "#33d9b2"],

}

var b_stop = {

    0: ["4523", "R. do Tejo", "#f3a683"],

    1: ["1232","R. D.Pedro V", "#f7d794"],

    2: ["1324", "R. Náufragos", "#778beb"],

    3: ["4532", "Av. Reis", "#e77f67"],
    
    4: ["7823", "R. Borboleta", "#cf6a87"],

    5: ["4357", "Pr. Viados", "#34ace0"],

    6: ["3421", "Av. Luis II", "#f8a5c2"],

    7: ["6785", "R. Leitões", "#63cdda"],

    8: ["9876", "Av. Liberdade", "#ea8685"],

    9: ["8342", "R. Carlos Pais", "#33d9b2"],

}

function transportType() {
    var transport = sessionStorage.getItem("transport");
    document.getElementById("title").innerHTML = transport;

    if(sessionStorage.getItem("stopId1") != null) {
        getStops();
        return;
    }
    switch(transport) {
        case "Airplane": 
            document.getElementById("logo").src = "../images/plane.png";
            setStops(pl_stop);
            break;
        case "Train":
            document.getElementById("logo").src = "../images/train.svg";
            setStops(ts_stop);
            break;
        case "Subway":
            document.getElementById("logo").src = "../images/subway.png";
            setStops(ts_stop);
            break;
        case "Bus":
            document.getElementById("logo").src = "../images/bus.png";
            setStops(b_stop);
            break;
    }
}

function setStops(object) {
    var times = new Date();
    var start;
    var random; 
    if(sessionStorage.getItem("minutesIncrease") !== null){
        minutes = sessionStorage.getItem("minutesIncrease");
        times = new Date(times.getTime() + minutes*60000);
    }
    start = times.getSeconds()%10;

    
    for(i=start; i<start+8;i++){
        random = times.getMilliseconds()%10;
        times = new Date(times.getTime() + random*60000);
        document.getElementById("stopId"+ (i-start+1)).innerHTML = object[i%10][0];
        document.getElementById("stopName"+ (i-start+1)).innerHTML = object[i%10][1];
        document.getElementById("stopId"+ (i-start+1)).style.backgroundColor = object[i%10][2];
        document.getElementById("time"+ (i-start+1)).style.backgroundColor = object[i%10][2];
        document.getElementById("share" + (i-start+1)).style.borderColor = object[i%10][2];
        document.getElementById("directions" + (i-start+1)).style.borderColor = object[i%10][2];
        document.getElementById("time"+ (i-start+1)).innerHTML =  (times.getHours() < 10 ? '0' : '') + times.getHours() + ":" + (times.getMinutes() < 10 ? '0' : '') + times.getMinutes();
    }
}

function shareModal(id) {
    var destiny = document.getElementById("stopName"+id).innerHTML;
    var tiime = document.getElementById("time"+id).innerHTML;
    var i=1;
    var stopId;
    var stopName;
    var itime;
    var color;
    sessionStorage.setItem("fromShare",destiny);
    sessionStorage.setItem("timeShare",tiime);
    for(;i<9;i++) {
        stopName = document.getElementById("stopName"+i).innerHTML;
        itime = document.getElementById("time"+i).innerHTML;
        stopId = document.getElementById("stopId"+i).innerHTML;
        color = document.getElementById("stopId"+i).style.backgroundColor;

        sessionStorage.setItem("time"+i,itime);
        sessionStorage.setItem("stopId"+i,stopId);
        sessionStorage.setItem("stopName"+i,stopName);
        sessionStorage.setItem("stopColor"+i,color);
    }
}

function getStops() {
    var i=1;
    var color;

    for(;i<9;i++) {
        document.getElementById("stopId"+ i).innerHTML = sessionStorage.getItem("stopId"+i);
        document.getElementById("stopName" + i).innerHTML = sessionStorage.getItem("stopName"+i);
        document.getElementById("time"+ i).innerHTML = sessionStorage.getItem("time"+i);

        color = sessionStorage.getItem("stopColor"+i);
        document.getElementById("stopId"+ i).style.backgroundColor = color;
        document.getElementById("time"+ i).style.backgroundColor = color;
        document.getElementById("share" + i).style.borderColor = color;
        document.getElementById("directions" + i).style.borderColor = color;

        sessionStorage.removeItem("stopId"+i);
        sessionStorage.removeItem("stopName"+i);
        sessionStorage.removeItem("time"+i);
        sessionStorage.removeItem("stopColor"+i);
    }
}

function directionsModal(id) {
    var times = new Date();
    var nRoot = times.getSeconds() % 7;
    var destiny = document.getElementById("stopName"+id).innerHTML;
    sessionStorage.setItem("inputMaps",destiny);
    sessionStorage.setItem("backPrev","../schedules/Transport.html");
    if(sessionStorage.getItem("videoDuration") !== null) {
        document.getElementById("dir_mod").style.display = "block";
        document.getElementById("back").setAttribute('disabled', 'disabled');
        document.getElementById("home").setAttribute('disabled', 'disabled');
        document.getElementById("lock").setAttribute('disabled', 'disabled');
    } else {
        shareModal(id);
        window.location = "../maps/Rootpart1.html";
        sessionStorage.setItem("nRoot",++nRoot);
        window.location = "../maps/Rootpart1.html";
    }
}

function noModal() {
    document.getElementById("dir_mod").style.display = "none";
    document.getElementById("back").removeAttribute('disabled');
    document.getElementById("home").removeAttribute('disabled');
    document.getElementById("lock").removeAttribute('disabled');
}

function yesModal() {
    var times = new Date();
    var nRoot = times.getMinutes() % 7;
    sessionStorage.removeItem("videoDuration");
    sessionStorage.removeItem("currentDuration");

    shareModal(1);
    sessionStorage.setItem("nRoot",++nRoot);
    window.location = "../maps/Rootpart1.html";
}
