'use strict';


class Solid extends THREE.Mesh {

    constructor(geometry, matColor, position) {

        let material = new THREE.MeshBasicMaterial({ color: matColor, wireframe: false});

        super(geometry, material);

        this.position.set(position[0], position[1], position[2]);
    }
}


class Cube extends Solid {

    constructor(size, position) {

        let geometry = new THREE.CubeGeometry(size[0], size[1], size[2], 0, 0, 0);

        super(geometry, 0x1A59B9, position);
    }
}


class Cylinder extends Solid {

    constructor(radius, height, position) {

        let geometry = new THREE.CylinderGeometry(radius[0], radius[1], height, 0, 0, true);

        super(geometry, 0x800080, position);

    }
}


class Sphere extends Solid {

    constructor(radius, position) {

        let geometry = new THREE.SphereGeometry(radius, 30, 30);

        super(geometry, 0xFF0000, position);
    }
}
