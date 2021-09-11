'use strict';
/*
    Nº 86923 Sara Machado
    Nº 90770 Rafael Figueiredo
    Nº 90774 Ricardo Grade
 */

var clock, currCamera, cameras, scene, renderer, cannons, currCannon, bullets, barrier;

var activeKeyMap = {
    32: false, 37: false, 39: false,    /* Space, Left, Right */
    82: false   /* 'r' */
};


function getKey(keyCode) {
    return keyCode > 96 ? keyCode - 32 : keyCode;
}


function createScene() {

    scene = new THREE.Scene();

    scene.add(barrier);
    cannons.forEach(function (cannon) {
        scene.add(cannon);
    });
    bullets.forEach(function (bullet) {
        scene.add(bullet);
    });
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

    /* '2' */
    } else if (keyCode === 50 && currCamera !== cameras[1]) {
        currCamera = cameras[1];

    /* '3' */
    } else if (keyCode === 51 && cameras[2].bullet && currCamera !== cameras[2]) {
        currCamera = cameras[2];

    } else if (keyCode === 81 && currCannon !== cannons[0])  {
         currCannon.unselect();
         currCannon = cannons[0];
         currCannon.select()

    } else if (keyCode === 87 && currCannon !== cannons[1]) {
         currCannon.unselect();
         currCannon = cannons[1];
         currCannon.select()

    } else if (keyCode === 69 && currCannon !== cannons[2]) {
         currCannon.unselect();
         currCannon = cannons[2];
         currCannon.select();
    }

    if (keyCode === 32 && !activeKeyMap[32]) {
        let bullet = new Bullet(currCannon, null, random(75, 150));

        if (!bullets[0].axisHelper.visible) {
            bullet.axisHelper.visible = false;
        }

        bullets.push(bullet);
        cameras[2].setBullet(bullet);
        scene.add(bullet);
    }

    if (keyCode === 82 && !activeKeyMap[82]) {
        bullets.forEach(function (bullet) {
            bullet.axisHelper.visible = !bullet.axisHelper.visible;
        });
    }

    /* Cannon Interaction */
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


function cannonRotate(deltaTime) {

    const CannonAngle = deltaTime * 3;

    /* Left */
    if (activeKeyMap[37] && !activeKeyMap[39]) {
        currCannon.rotate(CannonAngle);
    }
    /* Right */
    if (activeKeyMap[39] && !activeKeyMap[37]) {
        currCannon.rotate(-CannonAngle);
    }
}


function createRandomBullets() {
    let newBullet;
    let collides;

    for (let i = random(10, 20); i >= 0; i--) {
        collides = false;
        newBullet = new Bullet(null, [random(-115, 65), 0, random(-64, 64)], 0);

        for (let t = 0; t < bullets.length; t++) {
            if (newBullet.hasCollBullet(bullets[t])) {
                collides = true;
                break;
            }
        }

        if (!collides)
            bullets.push(newBullet);
    }
}


function render() {

    renderer.render(scene, currCamera);
}


function animate() {

    let deltaTime = clock.getDelta();

    cannonRotate(deltaTime);

    for (let i = 0; i < bullets.length; i++) {
        bullets[i].translate(-deltaTime);
        barrier.collisionBullet(bullets[i], deltaTime);

        for (let t = i + 1; t < bullets.length; t++) {
            bullets[i].colBullet(bullets[t], deltaTime);
        }
    }
    cameras[2].update();
    render();
    requestAnimationFrame(animate);
}


function random(min, max) {
    return min + Math.random() * (max + 1 - min);
}


function init() {

    renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setSize(window.innerWidth, window.innerHeight);

    document.body.appendChild(renderer.domElement);

    clock = new THREE.Clock(true);

    cannons = [
        new Cannon(-window.innerHeight / 50),
        new Cannon(0),
        new Cannon(window.innerHeight / 50)
    ];

    currCannon = cannons[1];
    currCannon.select();

    bullets = [];
    createRandomBullets();
    barrier = new Barrier();

    createScene();
    cameras = [
        new OrthographicCam(200, scene.position, [0, 100, 0]),  /* Top */
        new PerspectiveCam(scene.position, [175, 60, 0]),   /* Frontal */
        new BulletCam()
    ];

    currCamera = cameras[0];
    animate();

    setInterval(function() {
        bullets.forEach(function(bullet) {
            bullet.slowDown(0.75);})}, 500);

    window.addEventListener("resize", onResize);
    window.addEventListener("keydown", onKeyDown);
    window.addEventListener("keyup", onKeyUp);
}
