function park() {
    var park = localStorage.getItem("park");
    console.log(park);
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
    document.getElementById("restImg").src = "../../images/p1.jpg";
    document.getElementById("desc").innerHTML = "Parque Florestal de Monsanto" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function park2() {
    document.getElementById("restImg").src = "../../images/p2.jpg";
    document.getElementById("desc").innerHTML = "Quinta das Conchas e dos Lilases" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/star.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function park3() {
    document.getElementById("restImg").src = "../../images/p3.jpg";
    document.getElementById("desc").innerHTML = "Parque Eduardo VII" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/halfStar.png";
    document.getElementById("open").innerHTML = "Closed ";
    document.getElementById("openImg").src = "../../images/closed.svg";
}


function park4() {
    document.getElementById("restImg").src = "../../images/p4.jpg";
    document.getElementById("desc").innerHTML = "Parque José Gomes Ferreira" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/star.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}

function park5() {
    document.getElementById("restImg").src = "../../images/p5.jpg";
    document.getElementById("desc").innerHTML = "Parque Bensaúde" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../images/star.png";
    document.getElementById("star2").src = "../../images/star.png";
    document.getElementById("star3").src = "../../images/star.png";
    document.getElementById("star4").src = "../../images/halfStar.png";
    document.getElementById("star5").src = "../../images/emptyStar.png";
    document.getElementById("open").innerHTML = "Open ";
    document.getElementById("openImg").src = "../../images/open.png";
    
}