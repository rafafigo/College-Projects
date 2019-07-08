function root() {
    var value = sessionStorage.getItem("inputMaps");
    var nRoot = sessionStorage.getItem("nRoot");
    var from =  ["Avenida da liberdade", "Colégio Militar", "Rua das Amoreiras", "Trindade dos Pombais", "Rossio", "Rua de Queluz baixa", "Rua Londrina"];
    var valueCut = value.split(" ");
    var leng = 0;

    if(value.length > 23) {
        value =  value.substr(0, 21) + "...";
    }

    for (var i=0; i<valueCut.length; i++) {
        if(valueCut[i].length > 12) {
            value =  value.substr(0, leng+10) + "...";
            break;
        }
        leng += valueCut[i].length;
    }
    document.getElementById("restImg").src = "../videos/" + nRoot + "/" + nRoot + ".3.jpg";
    document.getElementById("desc").innerHTML = "<b> From: </b> " +from[nRoot-1] + "<br> <b>To:</b> " + value;

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

