'use strict';
/*
    Nº 86923 Sara Machado
    Nº 90770 Rafael Figueiredo
    Nº 90774 Ricardo Grade
 */

var camera, scene, renderer;
var directLight, pointLight;
var board, dice, ball, pauseWall;
var clock, deltaScale;
var activeKeyMap = {
    76: false, 87: false,   /* 'l', 'w' */
    68: false, 80: false,   /* 'd', 'p' */
    66: false, 83: false, 82: false /* 'b' , 's', 'r' */
};


function getKey(keyCode) {
    return keyCode > 96 ? keyCode - 32 : keyCode;
}


function createScene() {

    scene = new THREE.Scene();

    scene.add(directLight);
    scene.add(pointLight);
    scene.add(board);
    scene.add(dice);
    scene.add(ball);
    scene.add(pauseWall);
}


function onResize() {

    renderer.setSize(window.innerWidth, window.innerHeight);
    camera.resize();
}


function onKeyMoving(keyCode) {
    /* 'b' */
    if (keyCode === 66 && !activeKeyMap[keyCode]) {
       ball.moving = !ball.moving;
    }
    /* 's' */
    else if (keyCode === 83 && !activeKeyMap[keyCode]) {
        deltaScale ^= 1;
        pauseWall.visible = !pauseWall.visible;
    }
    /* 'r' */
    else if (keyCode === 82 && !activeKeyMap[keyCode] && !deltaScale) {
        board.reset();
        ball.reset();
        dice.reset();
        deltaScale = 1;
        pauseWall.visible = false;
    }
}

function onKeyMaterial(keyCode) {

    /* 'l' */
    if (keyCode === 76 && !activeKeyMap[keyCode]) {
        board.changeMaterial();
        dice.changeMaterial();
        ball.changeMaterial();
    }
    /* 'w' */
    else if (keyCode === 87 && !activeKeyMap[keyCode]) {
        board.changeWireframe();
        dice.changeWireframe();
        ball.changeWireframe();
    }
}

function onKeyLight(keyCode) {

    /* 'd' */
    if (keyCode === 68 && !activeKeyMap[keyCode]) {
        directLight.visible = !directLight.visible;
    }
    /* 'p' */
    if (keyCode === 80 && !activeKeyMap[keyCode]) {
        pointLight.visible = !pointLight.visible;
    }
}


function onKeyDown(event) {

     const keyCode = getKey(event.keyCode);

     onKeyMoving(keyCode);
     onKeyMaterial(keyCode);
     onKeyLight(keyCode);

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
    renderer.render(scene, camera);
}


function animate() {
    let deltaTime = clock.getDelta() * deltaScale;

    ball.setVelocity(deltaTime);
    ball.rotateOrigin(deltaTime);
    dice.rotateItself(deltaTime);
    render();
    requestAnimationFrame(animate);
}


function createObjects() {

    let boardWidth = 100;
    let boardTick = boardWidth / 10;

    let diceSZ = 10;
    let diceY = diceSZ * Math.sqrt(3) / 2;

    let ballRadius = 5;
    let ballDist = 30;

    let widthPause = 130;
    let heightPause = 80;

    let intensity = 1.25;

    board = new ChessBoard(boardWidth, boardTick, [0, -boardTick / 2, 0]);
    dice = new Dice(diceSZ, [0, diceY, 0]);
    ball = new Ball(ballRadius, ballDist, [0, ballRadius, 0]);
    pauseWall = new PauseWall(widthPause, heightPause, [0, 0, 0]);
    pauseWall.visible = false;

    directLight = new THREE.DirectionalLight(0xFFFFFF, intensity / 2);
    directLight.position.set(20, 30, 20);
    directLight.target = board;

    pointLight = new THREE.PointLight(0xFFFFFF, intensity);
    pointLight.position.set(20, 30, 20);
}


function init() {

    renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setSize(window.innerWidth, window.innerHeight);

    document.body.appendChild(renderer.domElement);

    createObjects();
    createScene();

    camera = new PerspectiveCam(scene.position, [80, 80, 80]);
    clock = new THREE.Clock(true);
    deltaScale = 1;
    animate();

    window.addEventListener("resize", onResize);
    window.addEventListener("keydown", onKeyDown);
    window.addEventListener("keyup", onKeyUp);
}
