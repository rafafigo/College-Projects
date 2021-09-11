function openT(){
    isOpened("time1","isOpen1",11,22);
    isOpened("time2","isOpen2",12,23);
    isOpened("time3","isOpen3",11,15);
    isOpened("time4","isOpen4",9,22);
    isOpened("time5","isOpen5",15,22);
    isOpened("time6","isOpen6",10,15);
    isOpened("time7","isOpen7",15,22);
    isOpened("time8","isOpen8",20,23);
    isOpened("time9","isOpen9",12,22);
}
function setRestaurant1() {
    sessionStorage.setItem("restaurant","1");
}

function setRestaurant2() {
    sessionStorage.setItem("restaurant","2");
}

function setRestaurant3() {
    sessionStorage.setItem("restaurant","3");
}

function setRestaurant4() {
    sessionStorage.setItem("restaurant","4");
}

function setRestaurant5() {
    sessionStorage.setItem("restaurant","5");
}

function setRestaurant6() {
    sessionStorage.setItem("restaurant","6");
}

function setRestaurant7() {
    sessionStorage.setItem("restaurant","7");
}

function setRestaurant8() {
    sessionStorage.setItem("restaurant","8");
}

function setRestaurant9() {
    sessionStorage.setItem("restaurant","9");
}

function isOpened(open,imgOpen, hour1, hour2){
    var times = new Date();
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

