function timeLoad() {
    var times = new Date();
    document.getElementById("time").innerHTML = (times.getHours() < 10 ? '0' : '') + times.getHours() + ":" + (times.getMinutes() < 10 ? '0' : '') + times.getMinutes();
    setTimeout(timeLoad, 1000);

}

function helpModalOn() {
    console.log("ola");
    document.getElementById("help_mod").style.display = "block";
}

function helpModalOff() {
    document.getElementById("help_mod").style.display = "none";
}