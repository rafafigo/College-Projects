function timeLoad() {
    var times = new Date();

    if(sessionStorage.getItem("minutesIncrease") !== null){
        minutes = sessionStorage.getItem("minutesIncrease");
        times = new Date(times.getTime() + minutes*60000);
    }
    
    var date = times.toDateString();
    document.getElementById("time_lock").innerHTML = (times.getHours() < 10 ? '0' : '') + times.getHours() + ":" + (times.getMinutes() < 10 ? '0' : '') + times.getMinutes();
    document.getElementById("date_lock").innerHTML = date.substring(0, date.indexOf(" ")) + "," + date.substring(date.indexOf(" "), date.lastIndexOf(" "));
    setTimeout(timeLoad, 1000);

}

function unlockAcess() {
    event.preventDefault();

    if (sessionStorage.getItem("iLockiGo") == 0 || sessionStorage.getItem("iLockiGo") === null) {
        sessionStorage.setItem("iLockiGo", 1);
        window.location.href = "Menu.html";

    } else {
        window.history.back();
    }
}