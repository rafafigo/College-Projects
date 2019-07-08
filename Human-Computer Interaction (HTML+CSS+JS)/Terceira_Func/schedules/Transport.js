var ts_stop = {
    0: ["1232","Oriente"],

    1: ["1324", "Rossio"],
    
    2: ["4532", "Carcavelos"],
    
    3: ["7823", "Cascais"],

    4: ["4357", "Oeiras"],

    5: ["3421", "Queluz"],

    6: ["6785", "Cacém"],

    7: ["9876", "Estoril"],

    8: ["8342", "Belem"],

    9: ["4523", "Belas"],

}

var pl_stop = {
    
    0: ["4523", "Aero. Alentejo"],

    1: ["3943","Aero. Lisboa"],

    2: ["2122", "Aero. Minho"],
    
    3: ["4532", "Aero. Porto"],
    
    4: ["7823", "Aero. Algarve"],

    5: ["4357", "Aero. Braga"],

    6: ["3421", "Aero. Leiria"],

    7: ["6785", "Aero. Setúbal"],

    8: ["9876", "Aero. Faro"],

    9: ["8342", "Aero. Bragança"],

}

var b_stop = {

    0: ["4523", "R. do Tejo"],

    1: ["1232","R. D.Pedro V"],

    2: ["1324", "R. Náufragos"],

    3: ["4532", "Av. Reis"],
    
    4: ["7823", "R. Borboleta"],

    5: ["4357", "Pr. Viados"],

    6: ["3421", "Av. Luis II"],

    7: ["6785", "R. Leitões"],

    8: ["9876", "Av. Liberdade"],

    9: ["8342", "R. Carlos Pais"],

}

function transportType() {
    var transport = sessionStorage.getItem("transport");
    document.getElementById("title").innerHTML = transport;

    if(sessionStorage.getItem("stopId1") != null) {
        console.log("hey");
        getStops();
        return;
    }
    switch(transport) {
        case "Airplane": 
            setStops(pl_stop);
            break;
        case "Train":
        case "Subway":
            setStops(ts_stop);
            break;
        case "Bus":
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
    sessionStorage.setItem("fromShare",destiny);
    sessionStorage.setItem("timeShare",tiime);
    for(;i<9;i++) {
        stopName = document.getElementById("stopName"+id).innerHTML;
        itime = document.getElementById("time"+i).innerHTML;
        stopId = document.getElementById("stopId"+i).innerHTML;
        sessionStorage.setItem("time"+i,itime);
        sessionStorage.setItem("stopId"+i,stopId);
        sessionStorage.setItem("stopName"+i,stopName);
    }
}

function getStops() {
    var i=1;

    for(;i<9;i++) {
        document.getElementById("stopId"+ i).innerHTML = sessionStorage.getItem("stopId"+i);
        document.getElementById("stopName" + i).innerHTML = sessionStorage.getItem("stopName"+i);
        document.getElementById("time"+ i).innerHTML = sessionStorage.getItem("time"+i);
        sessionStorage.removeItem("stopId"+i);
        sessionStorage.removeItem("stopName"+i);
        sessionStorage.removeItem("time"+i);
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

    sessionStorage.setItem("nRoot",++nRoot);
    window.location = "../maps/Rootpart1.html";
}
