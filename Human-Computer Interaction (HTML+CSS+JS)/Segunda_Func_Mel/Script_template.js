var seconds = 0;
function timeLoad() {
    var temperature = [11, 12, 13, 14, 16, 17,18, 19, 20];
    var times = new Date();
    
    if(sessionStorage.getItem("minutesIncrease") !== null){
        minutes = sessionStorage.getItem("minutesIncrease");
        times = new Date(times.getTime() + minutes*60000);
    }

    document.getElementById("time").innerHTML = (times.getHours() < 10 ? '0' : '') + times.getHours() + ":" + (times.getMinutes() < 10 ? '0' : '') + times.getMinutes();
    document.getElementById("temp").innerHTML = "&ensp;" + temperature[(times.getHours()%8)] + "ºC Lisbon";
    document.getElementById("time").style.fontWeight = "bold"; 
    videoDuration();
    setTimeout(timeLoad, 1000);
}

function temp() {
    var newTemp = document.createElement("b");
    newTemp.setAttribute("id","temp");

    var timeEl = document.getElementById("time");  
    timeEl.parentNode.insertBefore(newTemp, timeEl.nextSibling);

}

function videoDuration() {
    var currentDuration = sessionStorage.getItem("currentDuration");

    if(sessionStorage.getItem("videoDuration") !== null && sessionStorage.getItem("secondsPassed") == 60 ) { 
            sessionStorage.setItem("currentDuration",++currentDuration);
            sessionStorage.setItem("secondsPassed", 0);
            seconds = 0;   
    }
    sessionStorage.setItem("secondsPassed", ++seconds);
}

function helpModalOn() {
    document.getElementById("help_mod").style.display = "block";
    document.getElementById("back").setAttribute('disabled', 'disabled');
    document.getElementById("home").setAttribute('disabled', 'disabled');
    document.getElementById("lock").setAttribute('disabled', 'disabled');
}

function helpModalOff() {
    document.getElementById("help_mod").style.display = "none";
    document.getElementById("back").removeAttribute('disabled');
    document.getElementById("home").removeAttribute('disabled');
    document.getElementById("lock").removeAttribute('disabled');
}

function mapD() {
    if( sessionStorage.getItem("videoDuration") !== null)
        document.getElementById("mapD").href = "maps/Rootpart2.html";
    else
        document.getElementById("mapD").href  = "maps/Maps.html";
}