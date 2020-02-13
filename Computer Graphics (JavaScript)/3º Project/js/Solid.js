'use strict';

const goldN = (1 + Math.sqrt(5)) / 2;


class Solid extends THREE.Mesh {

    constructor(geometry, matColor, position) {

        let material = new THREE.MeshBasicMaterial({color: matColor});
        super(geometry, material);

        this.shadows = [
            new THREE.MeshPhongMaterial({color: matColor}),
            new THREE.MeshLambertMaterial({color: matColor})
        ];

        this.currShadow = 0;

        this.materials = [
            material,
            this.shadows[this.currShadow]
        ];

        this.currMaterial = 0;

        this.position.set(position[0], position[1], position[2]);
    }
    
    changeShadow(){
        this.currShadow ^= 1;
        this.materials[1] = this.shadows[this.currShadow];
        if (this.currMaterial) {
            this.material = this.materials[1];
        }
    }

    changeMaterial() {
        this.currMaterial ^= 1;
        this.material = this.materials[this.currMaterial];
    }
}


class Cube extends Solid {

    constructor(size, position, color) {

        let geometry = new THREE.CubeGeometry(size[0], size[1], size[2], 32, 32, 32);

        super(geometry, color, position);
    }
}


class Sphere extends Solid {

    constructor(radius, position, color) {

        let geometry = new THREE.SphereGeometry(radius, 32, 32);

        super(geometry, color, position);
    }
}


class Cylinder extends Solid {

    constructor(radius, height, position, color) {

        let geometry = new THREE.CylinderGeometry(radius, radius, height, 32, 32, false);

        super(geometry, color, position);
    }
}


class Cone extends Solid {

    constructor(radius, height, position, color) {

        let geometry = new THREE.ConeGeometry(radius, height);

        super(geometry, color, position);
    }
}


class IcosahedronGeometry extends THREE.Geometry {

    constructor() {
        super();
        this.pushVertexes();
        this.pushFaces();
    }

    pushVertexes() {

        let vertexes = [
            new THREE.Vector3(-1, goldN, 0),
            new THREE.Vector3(0, -1, goldN),
            new THREE.Vector3(goldN, 0, -1)
        ];

        let indexes = [
            ["x", "y"],
            ["y", "z"],
            ["z", "x"]
        ];

        for (let v = 0; v < vertexes.length; v++) {
            for (let i = 0; i < 2; i++) {
                for (let t = 0; t < 2; t++) {
                    let scalar = 5 + Math.random()/2;
                    let vector = new THREE.Vector3(
                        vertexes[v].x * scalar,
                        vertexes[v].y * scalar,
                        vertexes[v].z * scalar
                    );
                    if (!this.minVertex || vector.y < this.minVertex.y) {
                        this.minVertex = vector;
                    }
                    this.vertices.push(vector);
                    vertexes[v][indexes[v][0]] *= -1;
                }
                vertexes[v][indexes[v][1]] *= -1;
            }
        }
        this.computeVertexNormals();
    }

    pushFaces() {

        let indexes = [
            [0, 11, 5], [0, 5, 1], [0, 1, 7], [0, 7, 10], [0, 10, 11],
            [1, 5, 9], [5, 11, 4], [11, 10, 2], [10, 7, 6], [7, 1, 8],
            [3, 9, 4], [3, 4, 2], [3, 2, 6], [3, 6, 8], [3, 8, 9],
            [4, 9, 5], [2, 4, 11], [6, 2, 10], [8, 6, 7], [9, 8, 1]
        ];

        for (let k = 0; k < indexes.length; k++) {
            this.faces.push(new THREE.Face3(indexes[k][0], indexes[k][1], indexes[k][2]));
        }
        this.computeFaceNormals();
    }
}


class Icosahedron extends Solid {

    constructor(position) {

        let geometry = new IcosahedronGeometry();

        super(geometry, 0xea8a8a, position);

        this.minVertex = geometry.minVertex;
    }
}
