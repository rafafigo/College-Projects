function chooseRoote() {
    var times = new Date();
    var nRoot = times.getMinutes() % 7;
    sessionStorage.setItem("nRoot",++nRoot);
    sessionStorage.setItem("backPrev","Maps.html");

    document.body.style.backgroundImage = "url('videos/" + nRoot + "/" + nRoot + ".2.jpg')";
    document.body.style.backgroundSize = "18.54pc 18.54pc";
    document.getElementById("info").style.backgroundColor = "rgb(130,130,130)";

}

function putValue() {
    sessionStorage.setItem("inputMaps", document.getElementById("testInput").value);
}