function park() {
    var park = sessionStorage.getItem("park");
    switch(park) {
    case "1":
        park1();
        break;
    case "2":
        park2();
        break;
    case "3":
        park3();
        break;
    case "4":
        park4();
        break;
    case "5":
        park5();
        break;
    case "6":
        park6();
        break;
    case "7":
        park7();
        break;
    case "8":
        park8();
        break;
    case "9":
        park9();
        break;
    }
}

function park1() {
    var name = "Parque Florestal de Monsanto";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/p1.jpg";
    document.getElementById("desc").innerHTML = "Garden with trees and a playground"+ "<br>" + "Size: 26 hectares" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    isOpened(6,23);
}

function park2() {
    var name = "Quinta das Conchas e dos Lilases";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/p2.jpg";
    document.getElementById("desc").innerHTML = "Peaceful park with rails and coffee" + "<br>" + "Size: 20 hectares" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    isOpened(5,23);
}

function park3() {
    var name = "Parque Eduardo VII";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/p3.jpg";
    document.getElementById("desc").innerHTML = "Park with paths and central lawns" + "<br>" + "Size: 25 hectares" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/halfStar.png";
    document.getElementById("open").innerHTML = "Closed ";
    document.getElementById("openImg").src = "../../images/closed.svg";
    isOpened(6,22);
}


function park4() {
    var name = "Parque José Gomes Ferreira";
    if (name.length > 25) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/p4.jpg";
    document.getElementById("desc").innerHTML = "Old park very well preserved" + "<br>" + "Size: 22 hectares" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    isOpened(8,22);
}

function park5() {
    var name = "Parque Bensaúde";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/p5.jpg";
    document.getElementById("desc").innerHTML = " Green space with picnic tables"+ "<br>" + "Size: 17 hectares" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/halfStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    isOpened(11,22);
}

function isOpened(hour1,hour2) {
    var times = new Date();
    actualHour = times.getHours(); 

    if(actualHour >= hour1 && actualHour < hour2) {
        document.getElementById("open").innerHTML = "Open ";
        document.getElementById("openImg").src = "../../images/open.png";
        document.getElementById("openTime").innerHTML = " (" + hour1 + ":00 - " + hour2 + ":00) ";
    }
    else {
        document.getElementById("open").innerHTML = "Closed ";
        document.getElementById("openImg").src = "../../images/closed.svg";
        document.getElementById("openTime").innerHTML = " (" + hour1 + ":00 - " + hour2 + ":00)";
    }
}