'use strict';
/*
    Nº 86923 Sara Machado
    Nº 90770 Rafael Figueiredo
    Nº 90774 Ricardo Grade
 */

var clock, currCamera, cameras, scene, renderer, robot, target;

var activeKeyMap = {
    65: false, 83: false,   /* 'a', 's' */
    81: false, 87: false,   /* 'q', 'w' */
    37: false, 39: false,   /* Left, Right */
    38: false, 40: false,   /* Up, Down */
    52: false   /* '4' */
};

function getKey(keyCode) {

    return keyCode > 96 ? keyCode - 32 : keyCode;
}

function createScene() {

    scene = new THREE.Scene();
   
    scene.add(robot);
    scene.add(target);
}


function onResize() {

    renderer.setSize(window.innerWidth, window.innerHeight);

    cameras.forEach(function(element) {
        element.resize();
    });
}


function onKeyDown(event) {

     const keyCode = getKey(event.keyCode);

    /* '1' */
    if (keyCode === 49 && currCamera !== cameras[0]) {
        currCamera = cameras[0];
    }
    /* '2' */
    else if (keyCode === 50 && currCamera !== cameras[1]) {
        currCamera = cameras[1];
    }
    /* '3' */
    else if (keyCode === 51 && currCamera !== cameras[2]) {
        currCamera = cameras[2];
    }
    /* '4' */
    else if (keyCode === 52 && !activeKeyMap[52]) {
        robot.negWireframe();
        target.negWireframe();
    }

    /* Robot Interaction */
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


function robotTranslate(deltaTime) {

    const DiagonalMotion = ((activeKeyMap[38] !== activeKeyMap[40]) &&
                            (activeKeyMap[37] !== activeKeyMap[39]));

    const RobotSpeed = DiagonalMotion ? deltaTime * 30 / Math.SQRT2 : deltaTime * 30;
    const WheelAngle = deltaTime * 15;

    /* Left */
    if (activeKeyMap[37] && !activeKeyMap[39]) {
        robot.translateSide(-RobotSpeed, WheelAngle);
    }
    /* Right */
    if (activeKeyMap[39] && !activeKeyMap[37]) {
        robot.translateSide(RobotSpeed, -WheelAngle);
    }
    /* Up */
    if (activeKeyMap[38] && !activeKeyMap[40]) {
        robot.translateForward(-RobotSpeed, -WheelAngle);
    }
    /* Down */
    if (activeKeyMap[40] && !activeKeyMap[38]) {
        robot.translateForward(RobotSpeed, WheelAngle);
    }
}


function robotAnimate(deltaTime) {

    const ArmAngle = deltaTime * 4;

    /* 'a' */
    if (activeKeyMap[65] && !activeKeyMap[83]) {
        robot.armRot(ArmAngle);
    }
    /* 's' */
    if (activeKeyMap[83] && !activeKeyMap[65]) {
        robot.armRot(-ArmAngle);
    }
    /* 'q' */
    if (activeKeyMap[81] && !activeKeyMap[87]) {
        robot.armMov(ArmAngle);
    }
    /* 'w' */
    if (activeKeyMap[87] && !activeKeyMap[81]) {
        robot.armMov(-ArmAngle);
    }
}


function render() {

    renderer.render(scene, currCamera);
}


function animate() {

    let deltaTime = clock.getDelta();

    robotTranslate(deltaTime);
    robotAnimate(deltaTime);

    render();
    requestAnimationFrame(animate);
}


function init() {

    renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setSize(window.innerWidth, window.innerHeight);

    document.body.appendChild(renderer.domElement);

    clock = new THREE.Clock(true);

    robot = new Robot();
    target = new Target();
    createScene();

    cameras = [
        new CameraOrtho(100, scene.position, [0, 100, 0]),   /* Top */
        new CameraOrtho(100, scene.position, [0, 0, 100]),   /* Frontal */
        new CameraOrtho(100, scene.position, [-100, 0, 0])   /* Lateral */
    ];

    currCamera = cameras[0];

    animate();

    window.addEventListener("resize", onResize);
    window.addEventListener("keydown", onKeyDown);
    window.addEventListener("keyup", onKeyUp);
}
