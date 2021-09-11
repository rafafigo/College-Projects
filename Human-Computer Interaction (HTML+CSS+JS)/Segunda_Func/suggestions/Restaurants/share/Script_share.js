function rest() {
    var share = sessionStorage.getItem("restaurant");
    switch(share) {
        case "1":
            share1();
            break;
        case "2":
            share2();
            break;
        case "3":
            share3();
            break;
        case "4":
            share4();
            break;
        case "5":
            share5();
            break;
        case "6":
            share6();
            break;
        case "7":
            share7();
            break;
        case "8":
            share8();
            break;
        case "9":
            share9();
            break;
        default:
            console.log(share);
    }
}

function share1() {
    document.getElementById("backl").href = "../Restaurant1.html";
    document.getElementById("restImg").src = "../../../images/restaurant1.png";
    document.getElementById("desc").innerHTML = "Jamie's Italian" + "<br>" + "Rua Príncipe Real" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../../images/star.png";
    document.getElementById("star2").src = "../../../images/star.png";
    document.getElementById("star3").src = "../../../images/star.png";
    document.getElementById("star4").src = "../../../images/star.png";
    document.getElementById("star5").src = "../../../images/star.png";
}


function share2() {
    document.getElementById("backl").href = "../Restaurant1.html";
    document.getElementById("restImg").src = "../../../images/restaurant2.png";
    document.getElementById("desc").innerHTML = "The Green Affair" + "<br>" + "Rua Duque Ávila" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../../images/star.png";
    document.getElementById("star2").src = "../../../images/star.png";
    document.getElementById("star3").src = "../../../images/star.png";
    document.getElementById("star4").src = "../../../images/star.png";
    document.getElementById("star5").src = "../../../images/star.png";  
}

function share3() {
    document.getElementById("backl").href = "../Restaurant1.html";
    document.getElementById("restImg").src = "../../../images/restaurant3.png";
    document.getElementById("desc").innerHTML = "Zero Zero" + "<br>" + "Praça da Alameda" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../../images/star.png";
    document.getElementById("star2").src = "../../../images/star.png";
    document.getElementById("star3").src = "../../../images/star.png";
    document.getElementById("star4").src = "../../../images/star.png";
    document.getElementById("star5").src = "../../../images/halfStar.png";
}


function share4() {
    document.getElementById("backl").href = "../Restaurant1.html";
    document.getElementById("restImg").src = "../../../images/restaurant4.png";
    document.getElementById("desc").innerHTML = "Boa-Bao" + "<br>" + "Largo de Pinheiro" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../../images/star.png";
    document.getElementById("star2").src = "../../../images/star.png";
    document.getElementById("star3").src = "../../../images/star.png";
    document.getElementById("star4").src = "../../../images/star.png";
    document.getElementById("star5").src = "../../../images/emptyStar.png";
}

function share5() {
    document.getElementById("backl").href = "../Restaurant1.html";
    document.getElementById("restImg").src = "../../../images/restaurant5.png";
    document.getElementById("desc").innerHTML = "Contrabando" + "<br>" + "Praça 24 de Julho" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../../images/star.png";
    document.getElementById("star2").src = "../../../images/star.png";
    document.getElementById("star3").src = "../../../images/star.png";
    document.getElementById("star4").src = "../../../images/halfStar.png";
    document.getElementById("star5").src = "../../../images/emptyStar.png";
}

function share6() {
    document.getElementById("backl").href = "../Restaurant1.html";
    document.getElementById("restImg").src = "../../../images/restaurant6.png";
    document.getElementById("desc").innerHTML = "Hamburgueria Portuguesa" + "<br>" + "Largo Conde Barão" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../../images/star.png";
    document.getElementById("star2").src = "../../../images/star.png";
    document.getElementById("star3").src = "../../../images/star.png";
    document.getElementById("star4").src = "../../../images/halfStar.png";
    document.getElementById("star5").src = "../../../images/emptyStar.png";
}

function share7() {
    document.getElementById("backl").href = "../Restaurant1.html";
    document.getElementById("restImg").src = "../../../images/restaurant7.png";
    document.getElementById("desc").innerHTML = "Sea Me" + "<br>" + "Rua do Loreto" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../../images/star.png";
    document.getElementById("star2").src = "../../../images/star.png";
    document.getElementById("star3").src = "../../../images/star.png";
    document.getElementById("star4").src = "../../../images/emptyStar.png";
    document.getElementById("star5").src = "../../../images/emptyStar.png";
    
}

function share8() {
    document.getElementById("backl").href = "../Restaurant1.html";
    document.getElementById("restImg").src = "../../../images/restaurant8.png";
    document.getElementById("desc").innerHTML = "Nómada" + "<br>" + "Avenida Visconde" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../../images/star.png";
    document.getElementById("star2").src = "../../../images/star.png";
    document.getElementById("star3").src = "../../../images/star.png";
    document.getElementById("star4").src = "../../../images/emptyStar.png";
    document.getElementById("star5").src = "../../../images/emptyStar.png";
}

function share9() {
    document.getElementById("backl").href = "../Restaurant1.html";
    document.getElementById("restImg").src = "../../../images/restaurant9.png";
    document.getElementById("desc").innerHTML = "Mez Cais LX" + "<br>" + "Rua Rodrigues" + "<br>" + "Rating";
    document.getElementById("star1").src = "../../../images/star.png";
    document.getElementById("star2").src = "../../../images/star.png";
    document.getElementById("star3").src = "../../../images/halfStar.png";
    document.getElementById("star4").src = "../../../images/emptyStar.png";
    document.getElementById("star5").src = "../../../images/emptyStar.png";
}



function noModal() {
    document.getElementById("modal_info").innerHTML = "Not Shared!";
    document.getElementById("modal_id").style.display = "block";
    document.getElementById("back").setAttribute('disabled', 'disabled');
    document.getElementById("home").setAttribute('disabled', 'disabled');
    document.getElementById("lock").setAttribute('disabled', 'disabled');
}

function yesModal() {
    document.getElementById("modal_info").innerHTML = "Successfully Shared!";
    document.getElementById("modal_id").style.display = "block";
    document.getElementById("back").setAttribute('disabled', 'disabled');
    document.getElementById("home").setAttribute('disabled', 'disabled');
    document.getElementById("lock").setAttribute('disabled', 'disabled');
}

