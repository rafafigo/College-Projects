'use strict';


function radians(degrees) {
    return degrees * Math.PI / 180;
}


class Component extends THREE.Object3D {

    constructor(position) {
        super();
        this.origin = position;
        this.position.set(this.origin[0], this.origin[1], this.origin[2]);
    }

    reset() {
        this.rotation.set(0, 0, 0);
        this.children.forEach(function (element) {
            element.reset();
        });
    }

    changeMaterial() {
        this.children.forEach(function (element) {
            element.changeMaterial();
        });
    }

    changeWireframe() {
        this.children.forEach(function (element) {
            element.changeWireframe();
        });
    }
}

class ChessBoard extends Component {

    constructor(width, thickness, position) {
        super(position);

        let woodImg = "Images/wood.jpg";

        let images = [
            woodImg, woodImg, "Images/chessBoard.jpg",
            woodImg, woodImg, woodImg,
        ];

        this.board = new Cube([width, thickness, width], [0, 0, 0], images);

        this.add(this.board);
    }
}


class Ball extends Component {

    constructor(radius, distance, position) {
        super(position);

        let images = ["Images/monalisa.jpg"];

        this.radius = radius;
        this.ball = new Sphere(radius, [0, 0, distance], images);

        this.deltaV = {
            min: 0,
            max: 15
        };
        this.velocity = this.deltaV.min;
        this.moving = false;

        this.add(this.ball);
    }

    reset() {
        this.moving = false;
        this.velocity = 0;
        super.reset();
    }

    setVelocity(deltaTime) {

        if (this.moving && this.velocity !== this.deltaV.max) {

            if (this.velocity + deltaTime < this.deltaV.max) {
                this.velocity += deltaTime;

            } else {
                this.velocity = this.deltaV.max;
            }
        } else if (!this.moving && this.velocity !== this.deltaV.min) {

            if (this.velocity - deltaTime > this.deltaV.min) {
                this.velocity -= deltaTime;

            } else {
                this.velocity = this.deltaV.min;
            }
        }
    }

    rotateOrigin(angle) {

        angle *= this.velocity;

        this.rotation.z -= angle;
        this.rotation.y += angle * this.radius / this.ball.position.z;
    }
}


class Dice extends Component {

    constructor(size, position) {
        super(position);

        let images = [
            "Images/1.png", "Images/2.png", "Images/3.png",
            "Images/4.png", "Images/5.png", "Images/6.png"
        ];

        this.dice = new Cube([size, size, size], [0, 0, 0], images);

        this.dice.rotation.z = Math.PI / 4;
        this.dice.rotation.x = Math.atan(Math.SQRT2 / 2);

        this.add(this.dice);
    }

    rotateItself(angle) {
        this.rotateY(angle);
    }
}


class PauseWall extends THREE.Object3D {

    constructor(width, height, position) {
        super();

        let woodImg = "Images/pauseBack.jpg";

        let images = [
            woodImg, woodImg, "Images/pause.png",
            woodImg, woodImg, woodImg
        ];

        this.wall = new Cube([width, 10, height], position, images);
        this.wall.rotateY(Math.PI / 4);
        this.wall.rotateX(Math.PI / 2 - Math.atan(Math.SQRT2 / 2));
        this.wall.translateY(65);

        this.add(this.wall);
    }
}
