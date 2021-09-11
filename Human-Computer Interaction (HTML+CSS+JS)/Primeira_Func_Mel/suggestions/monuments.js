function openT(){
    isOpened("time1","isOpen1",10,22);
    isOpened("time2","isOpen2",9,22);
    isOpened("time3","isOpen3",8,22);
    isOpened("time4","isOpen4",11,22);
    isOpened("time5","isOpen5",11,23);
    isOpened("time6","isOpen6",10,23);
    isOpened("time7","isOpen7",10,19);
    isOpened("time8","isOpen8",10,19);
}

function setMonuments1() {
    sessionStorage.setItem("monument","1");
}

function setMonuments2() {
    sessionStorage.setItem("monument","2");
}

function setMonuments3() {
    sessionStorage.setItem("monument","3");
}

function setMonuments4() {
    sessionStorage.setItem("monument","4");
}

function setMonuments5() {
    sessionStorage.setItem("monument","5");
}

function setMonuments6() {
    sessionStorage.setItem("monument","6");
}

function setMonuments7() {
    sessionStorage.setItem("monument","7");
}

function setMonuments8() {
    sessionStorage.setItem("monument","8");
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
