'use strict';

const friction = 1.5;

function radians(degrees) {
    return degrees * Math.PI / 180;
}


function distance(pos1, pos2) {
    return Math.sqrt((pos1.x - pos2.x) ** 2 + (pos1.y -pos2.y) ** 2 + (pos1.z - pos2.z) ** 2);
}


class Cannon extends THREE.Object3D {

    constructor(posZ) {
        super();
        this.angle = Math.atan(posZ / -100);
        this.height = 40;

        this.add(new Cylinder([3, 6], this.height, [0, 0, 0]));
        this.position.set(120, 4, posZ);

        this.rotateZ(Math.PI / 2);
        this.rotateX(this.angle);
    }

    rotate(angle) {

        if (Math.abs(this.angle + angle) > radians(25)) {
            return;
        }

        this.rotateX(angle);
        this.angle += angle;
    }

    select() {
        this.children.forEach(function (element) {
            element.material.color.set(0x00FF00);
        });
    }

    unselect() {
        this.children.forEach(function (element) {
            element.material.color.set(0x800080);
        });
    }
}


class Bullet extends THREE.Object3D {

    constructor(cannon, position, velocity) {
        super();
        this.radius = 5;
        this.velocity = velocity;
        this.bullet = new Sphere(this.radius, [0, 0, 0]);
        this.axisHelper = new THREE.AxesHelper(10);
        this.bullet.add(this.axisHelper);
        this.add(this.bullet);

        if (cannon) {
            this.position.set(cannon.position.x, 0, cannon.position.z);
            this.direction = new THREE.Vector3(Math.cos(-cannon.angle), 0, Math.sin(-cannon.angle));
            this.direction.normalize();
            this.translateOnAxis(this.direction, -cannon.height / 2);

        } else {
            this.position.set(position[0], position[1], position[2]);
            this.direction = new THREE.Vector3(1, 0, 0);
        }
    }

    translate(deltaTime) {
        if (this.velocity > 0) {
            const distance = (deltaTime * this.velocity);
            this.translateOnAxis(this.direction, distance);
            const axis = {x: -this.direction.z, y: 0, z: this.direction.x};
            this.bullet.rotateOnAxis(axis, distance / this.radius);
        }
    }

    slowDown(deltaTime) {

        if (this.velocity - friction * deltaTime <= 0) {
            this.velocity = 0;
        }
        else {
            this.velocity -= friction * deltaTime;
        }
    }

    hasCollBullet(bullet) {
        return distance(this.position, bullet.position) < (this.radius + bullet.radius);
    }

    colBullet(bullet, deltaTime) {

        let dist = distance(this.position, bullet.position);

        if (dist < (this.radius + bullet.radius)) {

            const deltaV = 1000 * deltaTime;

            const alpha = this.direction.angleTo(bullet.direction);

            let angle = (Math.PI + alpha) / 2;
            let dirX = this.direction.x;
            let dirZ = this.direction.z;

            /* Applying Rotation Matrix to the Direction Vector */
            this.direction.x = Math.cos(angle) * dirX + Math.sin(angle) * dirZ;
            this.direction.z = Math.cos(angle) * dirZ - Math.sin(angle) * dirX;

            this.translateOnAxis(this.direction, dist / 2 - this.radius);

            angle *= -1;
            dirX = bullet.direction.x;
            dirZ = bullet.direction.z;

            /* Applying Rotation Matrix to the Direction Vector */
            bullet.direction.x = Math.cos(angle) * dirX + Math.sin(angle) * dirZ;
            bullet.direction.z = Math.cos(angle) * dirZ - Math.sin(angle) * dirX;

            bullet.translateOnAxis(bullet.direction, dist / 2 - bullet.radius);

            if (this.velocity > bullet.velocity) {
                this.slowDown(deltaV);
                bullet.velocity += deltaV * friction;

            } else {
                this.velocity += deltaV * friction;
                bullet.slowDown(deltaV);
            }
        }
    }
}


class Barrier extends THREE.Object3D {

    constructor() {
        super();
        this.thickness = 2;

        this.walls = {
            top: new Cube([200, 20, this.thickness], [-25, 5, -75 + this.thickness / 2]),
            left: new Cube([this.thickness, 20, 150], [-125 - this.thickness / 2, 5, 0]),
            bottom: new Cube( [200, 20, this.thickness], [-25, 5, 75 - this.thickness / 2])
        };
        this.add(this.walls.top);
        this.add(this.walls.left);
        this.add(this.walls.bottom);
    }

    collisionBullet(bullet, deltaTime) {
        const deltaV = 500 * deltaTime;

        let difBWTop = this.walls.top.position.z + this.thickness / 2 - (bullet.position.z - bullet.radius);
        let difBWLeft = this.walls.left.position.x + this.thickness / 2 - (bullet.position.x - bullet.radius);
        let difBWBottom = bullet.position.z + bullet.radius - (this.walls.bottom.position.z - this.thickness / 2);

        if (difBWLeft > 0 && bullet.position.z > -75 && bullet.position.z < 75) {
            bullet.translateOnAxis(bullet.direction, difBWLeft);
            bullet.direction.x *= -1;
            bullet.slowDown(deltaV)
        }

        if (difBWTop > 0 && bullet.position.x > -125 && bullet.position.x < 75) {
            bullet.translateOnAxis(bullet.direction, difBWTop);
            bullet.direction.z *= -1;
            bullet.slowDown(deltaV);
        }

        if (difBWBottom > 0 && bullet.position.x > -125 && bullet.position.x < 75) {
            bullet.translateOnAxis(bullet.direction, difBWBottom);
            bullet.direction.z *= -1;
            bullet.slowDown(deltaV);
        }
    }
}
