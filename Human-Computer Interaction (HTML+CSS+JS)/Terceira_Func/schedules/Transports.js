function getValue() {
    var value = sessionStorage.getItem("inputSchedules");
    if(value.length > 23) {
        value =  value.substr(0, 21) + "...";
    }
    document.getElementById("testInput").innerHTML = value;

}

function isTrain() {
    sessionStorage.setItem("transport","Train");

}

function isPlane() {
    sessionStorage.setItem("transport","Airplane");
}

function isBus() {
    sessionStorage.setItem("transport","Bus");

}
function isSubway() {
    sessionStorage.setItem("transport","Subway");

}