function setBackground() {
    document.body.style.backgroundImage = "url(../images/backtransports.png)";
    document.body.style.backgroundSize = "18.54pc 14pc";
    document.body.style.backgroundPositionY = "4.5pc";
    document.body.style.backgroundRepeat = "no-repeat";
    document.getElementById("info").style.backgroundColor = "rgb(130,130,130)";
}

function putValue() {
    sessionStorage.setItem("inputSchedules", document.getElementById("testInput").value);
}