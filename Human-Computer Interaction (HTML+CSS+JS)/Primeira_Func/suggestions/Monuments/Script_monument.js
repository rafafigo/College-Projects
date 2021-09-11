function mon() {
    var monument = localStorage.getItem("monument");
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
    document.getElementById("restImg").src = "../../images/m1.jpg";
    document.getElementById("desc").innerHTML = "Torre de Belém" + "<br>" + "Avenida Brasília" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function monument2() {
    document.getElementById("restImg").src = "../../images/m2.jpg";
    document.getElementById("desc").innerHTML = "Mosteiro dos Jerónimos" + "<br>" + "Praça do Império" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function monument3() {
    document.getElementById("restImg").src = "../../images/m3.jpg";
    document.getElementById("desc").innerHTML = "Castelo de São Jorge" + "<br>" + "Rua de Santa Cruz do Castelo" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/halfStar.png";
    document.getElementById("open").innerHTML = "Closed ";
    document.getElementById("openImg").src = "../../images/closed.svg";
}


function monument4() {
    document.getElementById("restImg").src = "../../images/m4.jpg";
    document.getElementById("desc").innerHTML = "Elevador de Santa Justa" + "<br>" + "Rua do Ouro" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function monument5() {
    document.getElementById("restImg").src = "../../images/m5.jpg";
    document.getElementById("desc").innerHTML = "Oceanário de Lisboa" + "<br>" + "Avenida D. Carlos I" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/halfStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function monument6() {
    document.getElementById("restImg").src = "../../images/m6.jpg";
    document.getElementById("desc").innerHTML = "Padrão dos Descobrimentos" + "<br>" + "Avenida Brasília" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/halfStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function monument7() {
    document.getElementById("restImg").src = "../../images/m7.jpg";
    document.getElementById("desc").innerHTML = "Santuário Nacional de Cristo Rei" + "<br>" + "Almada" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/emptyStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function monument8() {
    document.getElementById("restImg").src = "../../images/m8.png";
    document.getElementById("desc").innerHTML = "Sé de Lisboa" + "<br>" + "Lago da Sé" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/emptyStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}