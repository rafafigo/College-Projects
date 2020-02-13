'use strict';
/*
    Nº 86923 Sara Machado
    Nº 90770 Rafael Figueiredo
    Nº 90774 Ricardo Grade
 */

var currCamera, cameras, scene, renderer;
var directLight, spotLights;
var paint, patten, walls;

var activeKeyMap = {
    81: false, 87: false, 69: false,   /* 'q', 'w', 'e' */
    49: false, 50: false, 51: false, 52: false,   /* '1', '2', '3', '4' */
    53: false, 54: false   /* '5', '6' */
};


function getKey(keyCode) {
    return keyCode > 96 ? keyCode - 32 : keyCode;
}


function createScene() {

    scene = new THREE.Scene();

    scene.add(directLight);
    scene.add(directLight.target);
    scene.add(patten);
    scene.add(paint);
    scene.add(spotLights.IL);
    scene.add(spotLights.IR);
    scene.add(spotLights.QL);
    scene.add(spotLights.QR);
}


function onResize() {

    renderer.setSize(window.innerWidth, window.innerHeight);

    cameras.forEach(function(element) {
        element.resize();
    });
}

function onKeyCamera(keyCode) {

    /* '5' */
    if (keyCode === 53 && currCamera !== cameras[0]) {
        currCamera = cameras[0];
    }
    /* '6' */
    else if (keyCode === 54 && currCamera !== cameras[1]) {
        currCamera = cameras[1];
    }
}


function onKeyLight(keyCode) {

    /* 'q' */
    if (keyCode === 81 && !activeKeyMap[keyCode]) {
        directLight.visible = !directLight.visible;
    }
    /* '1' */
    else if (keyCode === 49 && !activeKeyMap[keyCode]) {
        spotLights.IL.light.visible = !spotLights.IL.light.visible;
    }
    /* '2' */
    else if (keyCode === 50 && !activeKeyMap[keyCode]) {
        spotLights.IR.light.visible = !spotLights.IR.light.visible;
    }
    /* '3' */
    if (keyCode === 51 && !activeKeyMap[keyCode]) {
        spotLights.QL.light.visible = !spotLights.QL.light.visible;
    }
    /* '4' */
    else if (keyCode === 52 && !activeKeyMap[keyCode]) {
        spotLights.QR.light.visible = !spotLights.QR.light.visible;
    }
}


function onKeyMaterial(keyCode) {

    /* 'w' */
    if (keyCode === 87 && !activeKeyMap[keyCode]) {
        walls.changeMaterial();
        patten.changeMaterial();
        paint.changeMaterial();
    }
    /* 'e' */
    else if (keyCode === 69 && !activeKeyMap[keyCode]) {
        walls.changeShadow();
        patten.changeShadow();
        paint.changeShadow();
    }
}


function onKeyDown(event) {

     const keyCode = getKey(event.keyCode);

     onKeyLight(keyCode);
     onKeyMaterial(keyCode);
     onKeyCamera(keyCode);

    if (keyCode in activeKeyMap) {
        activeKeyMap[keyCode] = true;
    }
}


function onKeyUp(event) {

    const keyCode = getKey(event.keyCode);

    if (keyCode in activeKeyMap) {
        activeKeyMap[keyCode] = false;
    }
}


function render() {
    renderer.render(scene, currCamera);
}


function animate() {
    render();
    requestAnimationFrame(animate);
}


function createObjects() {

    walls = new Walls();
    patten = new Patten();
    paint = new Paint([24, 20.5, -45]);

    directLight = new THREE.DirectionalLight(0xFFFFFF, 1.25);
    directLight.position.set(100, 100, 100);
    directLight.target = walls;

    spotLights = {
        IL: new SpotLight([-35, -34.5, 35], patten.icosahedron, 2, 85),
        IR: new SpotLight([-15, -34.5, 35], paint, 1, 150),
        QL: new SpotLight([16, -34.5, 35], patten.icosahedron, 2, 90),
        QR: new SpotLight([32, -34.5, 35], paint, 1, 150)
    };
}


function init() {

    renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setSize(window.innerWidth, window.innerHeight);

    document.body.appendChild(renderer.domElement);

    createObjects();
    createScene();

    cameras = [
        new PerspectiveCam(scene.position, [80, 47, 125]),
        new OrthographicCam(50, paint.position, [paint.position.x, paint.position.y, 100])
    ];

    currCamera = cameras[0];
    animate();

    window.addEventListener("resize", onResize);
    window.addEventListener("keydown", onKeyDown);
    window.addEventListener("keyup", onKeyUp);
}
