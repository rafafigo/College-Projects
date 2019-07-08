function openT(){
    isOpened("time1","isOpen1",19,27);
    isOpened("time2","isOpen2",20,25);
    isOpened("time3","isOpen3",22,27);
    isOpened("time4","isOpen4",23,27);
    isOpened("time5","isOpen5",21,28);
    isOpened("time6","isOpen6",22,26);
    isOpened("time7","isOpen7",22,25);
    isOpened("time8","isOpen8",19,25);
}

function setBar1() {
    sessionStorage.setItem("bar","1");
}

function setBar2() {
    sessionStorage.setItem("bar","2");
}

function setBar3() {
    sessionStorage.setItem("bar","3");
}

function setBar4() {
    sessionStorage.setItem("bar","4");
}

function setBar5() {
    sessionStorage.setItem("bar","5");
}

function setBar6() {
    sessionStorage.setItem("bar","6");
}

function setBar7() {
    sessionStorage.setItem("bar","7");
}

function setBar8() {
    sessionStorage.setItem("bar","8");
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