var timeOut;

function getValue() {
    var nRoot = sessionStorage.getItem("nRoot");
    var vid = document.getElementById("video");

    vid.innerHTML = "<source id='Sourcev' src='videos/" + nRoot + "/" + nRoot + ".1.mp4'></source>";

    vid.addEventListener('ended',arrived,false);

    vid.onloadedmetadata = function() {
        var times = new Date();
        var spaceLeft = Math.trunc(vid.duration);
        var arrivedTime;
        if(sessionStorage.getItem("videoDuration") === null) {
            sessionStorage.setItem("spaceLeft",spaceLeft*100);
            sessionStorage.setItem("videoDuration",vid.duration);
            sessionStorage.setItem("currentDuration", 0);
            sessionStorage.setItem("secondsPassed", 0);
        }
        vid.currentTime = sessionStorage.getItem("currentDuration");
        arrivedTime = vid.duration - vid.currentTime;

        if(sessionStorage.getItem("minutesIncrease") === null) {
            sessionStorage.setItem("minutesIncrease", 0);
            times = new Date(times.getTime() + (arrivedTime)*60000);
        } else {
            
            arrivedTime += parseInt(sessionStorage.getItem("minutesIncrease"));
            times = new Date(times.getTime() + (arrivedTime)*60000);
        }
        if(vid.currentTime != vid.duration){
            document.getElementById("test2").innerHTML = (times.getHours() < 10 ? '0' : '') + times.getHours() + ":" + (times.getMinutes() < 10 ? '0' : '') + times.getMinutes();
            timeleft();
        }
    };

}

function timeleft() {
    var spaceLeft = sessionStorage.getItem("spaceLeft")
    var minutesIncrease = sessionStorage.getItem("minutesIncrease");
    var newSpaceleft = spaceLeft-100;

    var currentDuration = sessionStorage.getItem("currentDuration");
    
    sessionStorage.setItem("currentDuration",++currentDuration);

    sessionStorage.setItem("spaceLeft",newSpaceleft);
    if (newSpaceleft >= 0)
        sessionStorage.setItem("minutesIncrease", ++minutesIncrease);

    if( spaceLeft/1000 >= 1) {
        document.getElementById("test").innerHTML = (spaceLeft/1000).toFixed(1) + " km &#8226; "; 
    } 
    else
        document.getElementById("test").innerHTML = Math.max(0, (spaceLeft/1000).toFixed(1)) + " m &#8226;";

    timeOut = setTimeout(timeleft, 1000);
}

function arrived(e) {
    clearTimeout(timeOut);
    var elem1 = document.getElementById("test2");
    elem1.parentElement.removeChild(elem1);

    document.getElementById("test").innerHTML = "You have arrived!";

}

function cancelTrip() {
    sessionStorage.removeItem("videoDuration");
    sessionStorage.removeItem("currentDuration"); 
}