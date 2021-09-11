function timeLoad() {

    var times = new Date();
    var date = times.toDateString();
    document.getElementById("time_lock").innerHTML = (times.getHours() < 10 ? '0' : '') + times.getHours() + ":" + (times.getMinutes() < 10 ? '0' : '') + times.getMinutes();
    document.getElementById("date_lock").innerHTML = date.substring(0, date.indexOf(" ")) + "," + date.substring(date.indexOf(" "), date.lastIndexOf(" "));
    setTimeout(dataLoad, 1000);

}
