
function bar() {
    var bar = sessionStorage.getItem("bar");
    var times = new Date();
    var nRoot = times.getMinutes() % 7;
    sessionStorage.setItem("backPrev","../suggestions/Bars/Bar.html");
    sessionStorage.setItem("nRoot",++nRoot);

    switch(bar) {
        case "1":
            bar1();
            break;
        case "2":
            bar2();
            break;
        case "3":
            bar3();
            break;
        case "4":
            bar4();
            break;
        case "5":
            bar5();
            break;
        case "6":
            bar6();
            break;
        case "7":
            bar7();
            break;
        case "8":
            bar8();
            break;
    }
}

function bar1() {
    var name = "Bar TR3S";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/b1.jpg";
    document.getElementById("desc").innerHTML = "Rua Ruben Leitão" + "<br>" + "Great cocktails" + "<br>" + "Outdoor seating" + "<br>"+ "Rating";
    sessionStorage.setItem("inputMaps", "Rua Ruben Leitão");
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 915675377";
    isOpened(19,27);
}

function bar2() {
    var name = "Matriz Pombalina";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/b2.jpg";
    document.getElementById("desc").innerHTML = "Rua Trinas" + "<br>" + "Big cocktail menu" + "<br>" + "Jazz-soul backdrop" + "<br>"+ "Rating";
    sessionStorage.setItem("inputMaps", "Rua Trinas");
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 915685577";
    isOpened(20,25);
}

function bar3() {
    var name = "Foxtrot";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/b3.jpg";
    document.getElementById("desc").innerHTML = "Avenida Teresa" + "<br>" + "Late-night food" + "<br>" + "Outdoor seating" + "<br>"+ "Rating";
    sessionStorage.setItem("inputMaps", "Avenida Teresa");
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/halfStar.png";
    document.getElementById("open").innerHTML = "Closed ";
    document.getElementById("openImg").src = "../../images/closed.svg";
    document.getElementById("contact").innerHTML = "Contact: +351 935632577";
    isOpened(22,27);
}


function bar4() {
    var name = "Bruxa Beer Bar";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/b4.jpg";
    document.getElementById("desc").innerHTML = "Rua São Mamede" + "<br>" + "Casual" + "<br>" + "Kid-friendly" + "<br>"+ "Rating";
    sessionStorage.setItem("inputMaps", "Rua São Mamede");
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 9256845577";
    isOpened(23,27);
}

function bar5() {
    var name = "Shelter Bar";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/b5.jpg";
    document.getElementById("desc").innerHTML = "Rua da Palmeira" + "<br>" + "Great cocktails" + "<br>" + "Casual" + "<br>"+ "Rating";
    sessionStorage.setItem("inputMaps", "Rua da Palmeira");
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/halfStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 9245485577";
    isOpened(21,28);
}

function bar6() {
    var name = "Pavilhão Chinês";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/b6.jpg";
    document.getElementById("desc").innerHTML = "Rua D. Pedro V" + "<br>" + "Decorated like a museum" + "<br>"+ "Rating";
    sessionStorage.setItem("inputMaps", "Rua D. Pedro V");
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/halfStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 965635577";
    isOpened(22,26);
}

function bar7() {
    var name = "Templários Bar";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/b7.jpg";
    document.getElementById("desc").innerHTML = "Rua Flores do Lima" + "<br>" + "Relaxed club setting" + "<br>"+ "Rating";
    sessionStorage.setItem("inputMaps", "Rua Flores do Lima");
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/emptyStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 915653577";
    isOpened(22,25);
}

function bar8() {
    var name = "Sky Bar";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/b8.jpg";
    document.getElementById("desc").innerHTML = "Avenida D. João II" + "<br>" + "Great cocktails" + "<br>" + "Kid-friendly" + "<br>"+ "Rating";
    sessionStorage.setItem("inputMaps", "Avenida D. João II");
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/emptyStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 915685577";
    isOpened(19,25);
}

function isOpened(hour1,hour2) {
    var times = new Date();
    
    if(sessionStorage.getItem("minutesIncrease") !== null){
        minutes = sessionStorage.getItem("minutesIncrease");
        times = new Date(times.getTime() + minutes*60000);
    }

    actualHour = times.getHours(); 

    if(actualHour >= hour1 && actualHour < hour2) {
        document.getElementById("open").innerHTML = "Open ";
        document.getElementById("openImg").src = "../../images/open.png";
        document.getElementById("openTime").innerHTML = " (" + hour1 + ":00 - " + (hour2-24) + ":00) ";
    }
    else {
        document.getElementById("open").innerHTML = "Closed ";
        document.getElementById("openImg").src = "../../images/closed.svg";
        document.getElementById("openTime").innerHTML = "  (" + hour1 + ":00 - " + (hour2-24) + ":00)";
    }
}