function rest() {
    var restaurant = sessionStorage.getItem("restaurant");
    switch(restaurant) {
    case "1":
        restaurant1();
        break;
    case "2":
        restaurant2();
        break;
    case "3":
        restaurant3();
        break;
    case "4":
        restaurant4();
        break;
    case "5":
        restaurant5();
        break;
    case "6":
        restaurant6();
        break;
    case "7":
        restaurant7();
        break;
    case "8":
        restaurant8();
        break;
    case "9":
        restaurant9();
        break;
    }
}

function restaurant1() {
    var name = "Jamie's Italian ";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/restaurant1.png";
    document.getElementById("desc").innerHTML = "Rua Príncipe Real" + "<br>" + "Price for two: 20€" + "<br>" + "Kid-Friendly" +"<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    document.getElementById("contact").innerHTML = "Contact: +351 9273245654";
    isOpened(11,22);
    
}

function restaurant2() {
    var name = "The Green Affair";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/restaurant2.png";
    document.getElementById("desc").innerHTML = "Rua Duque Ávila" + "<br>" + "Price for two: 50€" + "<br>" + "Seaview" +"<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    document.getElementById("contact").innerHTML = "Contact: +351 927357684";
    isOpened(12,23);
    
}

function restaurant3() {
    var name = "Zero Zero";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/restaurant3.png";
    document.getElementById("desc").innerHTML = "Praça da Alameda" + "<br>" + "Price for two: 15€" + "<br>" + "Kid-Friendly" +"<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/halfStar.png";
    document.getElementById("contact").innerHTML = "Contact: +351 9138438213";
    isOpened(11,15);
}


function restaurant4() {
    var name = "Boa-Bao";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/restaurant4.png";
    document.getElementById("desc").innerHTML = "Largo de Pinheiro" + "<br>" + "Price for two: 10€" + "<br>" + "Group meal" +"<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 9133428363";
    isOpened(9,22);
    
}

function restaurant5() {
    var name = "Contrabando";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/restaurant5.png";
    document.getElementById("desc").innerHTML = "Praça 24 de Julho" + "<br>" + "Price for two: 25€" + "<br>" + "Kid-Friendly" +"<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/halfStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 925204278";
    isOpened(15,22);
    
}

function restaurant6() {
    var name = "Hamburgueria Portuguesa";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/restaurant6.png";
    document.getElementById("desc").innerHTML = "Largo Conde Barão" + "<br>" + "Price for two: 20€" + "<br>" + "Group meal" +"<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/halfStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 925604278";
    isOpened(10,15);
    
}

function restaurant7() {
    var name = "Sea Me";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/restaurant7.png";
    document.getElementById("desc").innerHTML = "Rua do Loreto" + "<br>" + "Price for two: 25€" + "<br>" + "Seaview" +"<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/emptyStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 925604277";
    isOpened(15,22);
    
}

function restaurant8() {
    var name = "Nómada";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/restaurant8.png";
    document.getElementById("desc").innerHTML = "Avenida Visconde" + "<br>" + "Price for two: 35€" + "<br>" + "Group meal" +"<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/emptyStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    document.getElementById("contact").innerHTML = "Contact: +351 915644377";
    isOpened(20,23);
    
}

function restaurant9() {
    var name = "Mez Cais LX";
    if (name.length > 26) {
        document.getElementById("title").style.marginTop = "0.4pc";
    }
    document.getElementById("title").innerHTML = name;
    document.getElementById("restImg").src = "../../images/restaurant9.png";
    document.getElementById("desc").innerHTML = "Rua Rodrigues" + "<br>" + "Price for two: 20€" + "<br>" + "Group meal" +"<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/halfStar.png";
    document.getElementById("star4").src = "../../images/emptyStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Closed ";
    document.getElementById("openImg").src = "../../images/closed.svg";
    document.getElementById("contact").innerHTML = "Contact: +351 915675377";
    isOpened(12,22);
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
