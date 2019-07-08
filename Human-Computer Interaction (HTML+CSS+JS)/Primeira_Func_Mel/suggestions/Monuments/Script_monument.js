function mon() {
    var monument = sessionStorage.getItem("monument");
    switch(monument) {
    case "1":
        monument1();
        break;
    case "2":
        monument2();
        break;
    case "3":
        monument3();
        break;
    case "4":
        monument4();
        break;
    case "5":
        monument5();
        break;
    case "6":
        monument6();
        break;
    case "7":
        monument7();
        break;
    case "8":
        monument8();
        break;
    }
}

function monument1() {
    var name = "Torre de Belém";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/m1.jpg";
    document.getElementById("desc").innerHTML = "Avenida Brasília" + "<br>" + "Price: free"+ "<br>" + "With rooftop terrace "+ "<br>" +"Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 965634377";
    isOpened(10,22);
}

function monument2() {
    var name = "Mosteiro dos Jerónimos";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/m2.jpg";
    document.getElementById("desc").innerHTML = "Praça do Império" + "<br>" + "Price: 2€"+ "<br>" + "Manueline style"+ "<br>" +"Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 965634377";
    isOpened(9,22);

}

function monument3() {
    var name = "Castelo de São Jorge";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/m3.jpg";
    document.getElementById("desc").innerHTML = "Rua Cruz Castelo" + "<br>" + "Price: 3€"+ "<br>" + "Moorish castle"+ "<br>" +"Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/halfStar.png";
    document.getElementById("open").innerHTML = "Closed ";
    document.getElementById("openImg").src = "../../images/closed.svg";
    document.getElementById("contact").innerHTML = "Contact: +351 962643377";
    isOpened(8,22);
}


function monument4() {
    var name = "Elevador de Santa Justa";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/m4.jpg";
    document.getElementById("desc").innerHTML = "Rua do Ouro" + "<br>" + "Price: 4€"+ "<br>" + "Cast-iron elevator"+ "<br>" +"Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 915623377";
    isOpened(11,22);
    
}

function monument5() {
    var name = "Oceanário de Lisboa";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/m5.jpg";
    document.getElementById("desc").innerHTML = "Avenida D. Carlos I" + "<br>" + "Price: 18€"+ "<br>" + "Modern aquarium"+ "<br>" +"Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/halfStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 965634377";
    isOpened(11,23);    
}

function monument6() {
    var name = "Padrão dos Descobrimentos";
    if (name.length > 24) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/m6.jpg";
    document.getElementById("desc").innerHTML = "Avenida Brasília" + "<br>" + "Price: 8€"+ "<br>" + "Near Tagus River"+ "<br>" +"Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/halfStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 9356334377";
    isOpened(10,23);
    
}

function monument7() {
    var name = "Santuário Nacional de Cristo Rei";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/m7.jpg";
    document.getElementById("desc").innerHTML = "Almada" + "<br>" + "Price: 6€"+ "<br>" + "Catholic Monument"+ "<br>" +"Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/emptyStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 925634377";
    isOpened(10,19);
    
}

function monument8() {
    var name = "Sé de Lisboa";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/m8.png";
    document.getElementById("desc").innerHTML = "Lago da Sé" + "<br>" + "Price: free"+ "<br>" + "Roman church"+ "<br>" +"Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/emptyStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 923634377";
    isOpened(10,19);    
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