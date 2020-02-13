'use strict';

class Component extends THREE.Object3D {

    constructor() {
        super();
    }

    negWireframe() {

        this.children.forEach(function (element) {
            element.material.wireframe = !element.material.wireframe;
        });
    }
}


class Base extends Component {

    constructor() {
        super();

        this.top = new Cube([25, 2, 25], [0, -1, 0]);

        this.wheels = {
            TL: new Sphere(2, [-10.5, -4, -10.5]),
            TR: new Sphere(2, [10.5, -4, -10.5]),
            BL: new Sphere(2, [-10.5, -4, 10.5]),
            BR: new Sphere(2, [10.5, -4, 10.5])
        };

        this.pivot = new Calot(3, [0, Math.PI / 2], [0, 0, 0]);

        this.add(this.top);
        this.add(this.wheels.TL);
        this.add(this.wheels.TR);
        this.add(this.wheels.BL);
        this.add(this.wheels.BR);
        this.add(this.pivot);
    }

    pivotRot(angle) {
        const Axis = new THREE.Vector3(0, 1, 0);
        this.pivot.rotateOnWorldAxis(Axis, angle);
    }
    
    wheelsRotSide(angle) {
        this.wheels.TL.rotateZ(angle);
        this.wheels.TR.rotateZ(angle);
        this.wheels.BL.rotateZ(angle);
        this.wheels.BR.rotateZ(angle);
    }

    wheelsRotForward(angle) {
        this.wheels.TL.rotateX(angle);
        this.wheels.TR.rotateX(angle);
        this.wheels.BL.rotateX(angle);
        this.wheels.BR.rotateX(angle);
    }
}


class Arm extends Component {

    constructor() {
        super();

        this.arm = new Cube([2, 15, 2], [0, 10, 0]);

        this.forearm = new Cube([15, 2, 2], [9, 19, 0]);

        this.joints = {
            elbow: new Sphere(2, [0, 19, 0]),
            wrist: new Sphere(2, [18, 19, 0])
        };

        this.palm = new Cube([1, 5, 1.5], [20, 19, 0]);

        this.fingers = {
            top: new Cube([5, 0.5, 0.5], [22.5, 20.25, 0]),
            bottom: new Cube([5, 0.5, 0.5], [22.5, 17.75, 0]),
        };

        this.add(this.arm);
        this.add(this.forearm);
        this.add(this.joints.elbow);
        this.add(this.joints.wrist);
        this.add(this.palm);
        this.add(this.fingers.top);
        this.add(this.fingers.bottom);
    }

    axisRotY(angle) {
        const Axis = new THREE.Vector3(0, 1, 0);
        this.rotateOnWorldAxis(Axis, angle);
    }
}


class Target extends Component {

    constructor() {
        super();

        this.base = new Cube([4, 25, 4], [30, 6.5, 0]);

        this.target = new Torus(2, 0.5, [30, 21.5, 0]);
        
        this.add(this.base);
        this.add(this.target)
    }
}
