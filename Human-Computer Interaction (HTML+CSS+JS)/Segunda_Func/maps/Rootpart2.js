function getValue() {
    var nRoot = sessionStorage.getItem("nRoot");
    var vid = document.getElementById("video");

    vid.innerHTML = "<source id='Sourcev' src='videos/" + nRoot + "/" + nRoot + ".1.mp4'></source>";

    vid.addEventListener('ended',arrived,false);

    vid.onloadedmetadata = function() {
        var times = new Date();

        spaceLeft = Math.trunc(vid.duration);
        sessionStorage.setItem("spaceLeft",spaceLeft*100);

        if(sessionStorage.getItem("minutesIncrease") === null) {
            sessionStorage.setItem("minutesIncrease", 0);
            times = new Date(times.getTime() + (spaceLeft)*60000);
        }
        else {
            
            spaceLeft += parseInt(sessionStorage.getItem("minutesIncrease"));
            times = new Date(times.getTime() + (spaceLeft)*60000);
        }
        
        document.getElementById("test2").innerHTML = (times.getHours() < 10 ? '0' : '') + times.getHours() + ":" + (times.getMinutes() < 10 ? '0' : '') + times.getMinutes();
        timeleft();
    };

}

function timeleft() {
    var spaceLeft = sessionStorage.getItem("spaceLeft")
    var minutesIncrease = sessionStorage.getItem("minutesIncrease");
    var newSpaceleft = spaceLeft-100;
    var vidDuration = sessionStorage.getItem("videoDuration");
    var times = new Date();

    times = new Date(times.getTime() + vidDuration*60000);

    if(newSpaceleft >=0){

        sessionStorage.setItem("spaceLeft",newSpaceleft);
        sessionStorage.setItem("minutesIncrease", ++minutesIncrease);
        if( spaceLeft/1000 >= 1) {
            document.getElementById("test").innerHTML = (spaceLeft/1000).toFixed(1) + " km &#8226; "; 
        } 
        else
            document.getElementById("test").innerHTML = spaceLeft + " m &#8226;";
    }
    setTimeout(timeleft, 1000);
}

function arrived(e) {
    var elem1 = document.getElementById("test2");
    var elem2 = document.getElementById("cancel");
    var elem3 = document.getElementById("help");
    elem1.parentElement.removeChild(elem1);
    elem2.parentElement.removeChild(elem2);
    elem3.parentElement.removeChild(elem3);
    document.getElementById("backL").href = "Maps.html";
    document.getElementById("test").innerHTML = "You have arrived!";
}