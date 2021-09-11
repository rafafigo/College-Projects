'use strict';


function radians(degrees) {

    return degrees * Math.PI / 180;
}


class Robot extends THREE.Group {

    constructor() {
        super();

        this.base = new Base();
        this.arm = new Arm();
        
        this.armAngleZ = 0;

        this.add(this.base);
        this.add(this.arm);
    }

    armRot(angle) {
        this.base.pivotRot(angle);
        this.arm.axisRotY(angle);
    }

    armMov(angle) {

        if (Math.abs(this.armAngleZ + angle) > radians(50)) {
            return;
        }

        this.arm.rotateZ(angle);
        this.armAngleZ += angle;
    }

    translateSide(step, angle) {
        this.position.x += step;
        this.base.wheelsRotSide(angle);
    }

    translateForward(step, angle) {
        this.position.z += step;
        this.base.wheelsRotForward(angle);
    }

    negWireframe() {
        this.base.negWireframe();
        this.arm.negWireframe();
    }
}
