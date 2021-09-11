function getValue() {
    var value = sessionStorage.getItem("inputMaps");
    var nRoot = sessionStorage.getItem("nRoot");
    if(value.length > 23) {
        value =  value.substr(0, 21) + "...";
    }
    document.getElementById("backL").href =     sessionStorage.getItem("backPrev");
    document.getElementById("testInput").innerHTML = value;

    document.body.style.backgroundImage = "url('videos/" + nRoot + "/" + nRoot + ".3.jpg')";

    document.body.style.backgroundSize = "18.54pc 18.54pc";
    document.getElementById("info").style.backgroundColor = "rgb(130,130,130)";
}
