function openT(){
    isOpened("time1","isOpen1",6,23);
    isOpened("time2","isOpen2",5,23);
    isOpened("time3","isOpen3",6,22);
    isOpened("time4","isOpen4",8,22);
    isOpened("time5","isOpen5",11,22);

}

function setPark1() {
    sessionStorage.setItem("park","1");
}

function setPark2() {
    sessionStorage.setItem("park","2");
}

function setPark3() {
    sessionStorage.setItem("park","3");
}

function setPark4() {
    sessionStorage.setItem("park","4");
}

function setPark5() {
    sessionStorage.setItem("park","5");
}

function isOpened(open,imgOpen, hour1, hour2){
    var times = new Date();

    if(sessionStorage.getItem("minutesIncrease") !== null){
        minutes = sessionStorage.getItem("minutesIncrease");
        times = new Date(times.getTime() + minutes*60000);
    }
    
    var actualHour = times.getHours();
    if(actualHour >= hour1 && actualHour < hour2) {
        document.getElementById(open).innerHTML = "Open";
        document.getElementById(imgOpen).src = "../images/open.png";
    }
    else {
        document.getElementById(open).innerHTML = "Closed";
        document.getElementById(imgOpen).src = "../images/closed.svg";
    }
}
