'use strict';


class Solid extends THREE.Mesh {

    constructor(geometry, matColor, position) {

        let material = new THREE.MeshBasicMaterial({ color: matColor, wireframe: true });

        super(geometry, material);

        this.position.set(position[0], position[1], position[2]);
    }
}


class Cube extends Solid {

    constructor(size, position) {

        let geometry = new THREE.CubeGeometry(size[0], size[1], size[2], size[0], size[1], size[2]);

        super(geometry, 0x00B3FF, position);
    }
}

class Sphere extends Solid {

    constructor(radius, position) {

        let geometry = new THREE.SphereGeometry(radius, 10, 10);

        super(geometry, 0xFF0000, position);
    }
}


class Calot extends Solid {

    constructor(radius, thetaSL, position) {

        let geometry = new THREE.SphereGeometry(radius, 10, 10, 0, 2 * Math.PI, thetaSL[0], thetaSL[1]);

        super(geometry, 0xFF0000, position);
    }
}


class Torus extends Solid {

    constructor(radius, tube, position) {
        
        let geometry = new THREE.TorusGeometry(radius, tube, 15, 15);

        super(geometry, 0xFF0000, position);
    }
}
