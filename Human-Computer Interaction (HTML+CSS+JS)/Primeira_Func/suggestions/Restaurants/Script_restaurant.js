function rest() {
    var restaurant = localStorage.getItem("restaurant");
    console.log(restaurant);
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
    document.getElementById("restImg").src = "../../images/restaurant1.png";
    document.getElementById("desc").innerHTML = "Jamie's Italian" + "<br>" + "Praça do Príncipe Real" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function restaurant2() {
    document.getElementById("restImg").src = "../../images/restaurant2.png";
    document.getElementById("desc").innerHTML = "The Green Affair" + "<br>" + "Avenida Duque de Ávila" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function restaurant3() {
    document.getElementById("restImg").src = "../../images/restaurant3.png";
    document.getElementById("desc").innerHTML = "Zero Zero" + "<br>" + "Alameda dos Oceanos" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/halfStar.png";
    document.getElementById("open").innerHTML = "Closed ";
    document.getElementById("openImg").src = "../../images/closed.svg";
}


function restaurant4() {
    document.getElementById("restImg").src = "../../images/restaurant4.png";
    document.getElementById("desc").innerHTML = "Boa-Bao" + "<br>" + "Largo Rafael Bordalo Pinheiro" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function restaurant5() {
    document.getElementById("restImg").src = "../../images/restaurant5.png";
    document.getElementById("desc").innerHTML = "Contrabando" + "<br>" + "Avenida 24 de Julho" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/halfStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function restaurant6() {
    document.getElementById("restImg").src = "../../images/restaurant6.png";
    document.getElementById("desc").innerHTML = "Hamburgueria Portuguesa" + "<br>" + "Conde Barão" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/halfStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function restaurant7() {
    document.getElementById("restImg").src = "../../images/restaurant7.png";
    document.getElementById("desc").innerHTML = "Sea Me" + "<br>" + "Rua do Loreto" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/emptyStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function restaurant8() {
    document.getElementById("restImg").src = "../../images/restaurant8.png";
    document.getElementById("desc").innerHTML = "Nómada" + "<br>" + "Avenida Visconde Valmor" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/emptyStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function restaurant9() {
    document.getElementById("restImg").src = "../../images/restaurant9.png";
    document.getElementById("desc").innerHTML = "Mez Cais LX" + "<br>" + "Rua Rodrigues Faria" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/halfStar.png";
    document.getElementById("star4").src = "../../images/emptyStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    ocument.getElementById("open").innerHTML = "Closed ";
    document.getElementById("openImg").src = "../../images/closed.svg";
}
