'use strict';


class Component extends THREE.Object3D {

    constructor() {
        super();
    }

    changeShadow() {

        this.children.forEach(function (element) {
            element.changeShadow();
        });
    }

    changeMaterial () {

        this.children.forEach(function (element) {
            element.changeMaterial();
        });
    }
}


class Paint extends Component {

    constructor(position) {
        super();

        this.cylinderRad = 0.8;
        this.cubeSide = 4 - 2 * this.cylinderRad / Math.SQRT2;
        this.tickness = 0.001;

        this.add(new Cube([48, 36, this.tickness],
            [0, 0, 0.1], 0x808080));

        let pos = [-24, -18, 0.2];

        for (let lin = 0; lin < 9; lin++) {
            for (let col = 0; col < 12; col++) {
                if (col !== 0 && lin !== 0)
                    this.createCylinder(pos);
                this.createCube([pos[0] + 2, pos[1] + 2, pos[2]]);
                pos[0] += 4;
            }
            pos[0] = -24;
            pos[1] += 4;
        }

        this.add(new Cube([48, 2, 1], [0, -17, 0.5], 0x808b97));
        this.add(new Cube([48, 2, 1], [0, 17, 0.5], 0x808b97));
        this.add(new Cube([2, 36, 1], [-23, 0, 0.5], 0x808b97));
        this.add(new Cube([2, 36, 1], [23, 0, 0.5], 0x808b97));

        this.position.set(position[0], position[1], position[2]);
    }

    createCylinder(position) {

        let cylinder = new Cylinder(this.cylinderRad, this.tickness,
            position, 0xFFFFFF);
        cylinder.rotateX(Math.PI / 2);
        this.add(cylinder);
    }

    createCube(position) {

        this.add(new Cube([this.cubeSide, this.cubeSide, this.tickness],
            position, 0x000000));
    }
}


class Patten extends Component {

    constructor() {
        super();

        this.base = new Cylinder(6, 40, [-25, -27.5, -25], 0x808b97);
        let ico = new Icosahedron([-25, -7.5, -25]);

        let normaXZ = Math.sqrt(ico.minVertex.x**2 + ico.minVertex.z**2);
        let angle = Math.atan(normaXZ / ico.minVertex.y);

        let axis = new THREE.Vector3(-ico.minVertex.z, 0, ico.minVertex.x).normalize();
        ico.rotateOnAxis(axis, angle);

        let normaXYZ = Math.sqrt(ico.minVertex.x**2 + ico.minVertex.y**2 + ico.minVertex.z**2);
        ico.position.y += normaXYZ;
        this.icosahedron = ico;
        
        this.add(this.base);
        this.add(this.icosahedron);
    }
}


class Walls extends Component {

    constructor() {
        super();

        this.base = new Cube([100, 5, 100], [0, -50, 0], 0x494949);
        this.wall = new Cube([100, 100, 5], [0, 2.5, -47.5], 0xf5efe3);

        this.add(this.base);
        this.add(this.wall);
    }
}


function radians(degrees) {
    return degrees * Math.PI / 180;
}


class Lamp extends Component {

    constructor(position, targetPos) {
        super();

        this.base = new Cone(4, 12, [0, -6, 0], 0x000000);
        this.lamp = new Sphere(3.8, [0, -13, 0], 0xFFA500);
        this.add(this.base);
        this.add(this.lamp);
        this.position.set(position[0], position[1] - 13, position[2]);
        let oldDirection = new THREE.Vector3(0, -1, 0);
        let newDirection = new THREE.Vector3(
            targetPos.x - this.position.x,
            targetPos.y - this.position.y,
            targetPos.z - this.position.z
        );
        let axis = new THREE.Vector3(newDirection.z, 0, -newDirection.x);
        axis.normalize();
        this.rotateOnAxis(axis, -oldDirection.angleTo(newDirection));
    }
}


class SpotLight extends Component {

    constructor(position, target, intensity, distance) {
        super();
        this.lamp = new Lamp(position, target.position);
        this.light = new THREE.SpotLight(0xFFFFFF, intensity, distance, radians(20));
        this.light.position.set(position[0], position[1], position[2]);
        this.light.target = target;
        this.add(this.lamp);
        this.add(this.light);
        this.light.target.updateMatrixWorld();
    }
}
