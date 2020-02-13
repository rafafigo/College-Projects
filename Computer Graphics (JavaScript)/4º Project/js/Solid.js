'use strict';


function getBasicMats(images) {

    let materials = [];
    for (let i = 0; i < images.length; i++) {
        materials.push(new THREE.MeshBasicMaterial({
            map: new THREE.TextureLoader().load(images[i]),
            wireframe: false
        }));
    }
    return materials;
}


function getPhongMats(images) {
    let materials = [];
    for (let i = 0; i < images.length; i++) {
        materials.push(new THREE.MeshPhongMaterial({
            map: new THREE.TextureLoader().load(images[i]),
            bumpMap: new THREE.TextureLoader().load(images[i]),
            wireframe: false
        }));
    }
    return materials;
}


class Solid extends THREE.Mesh {

    constructor(geometry, position, images) {

        let materials = [getBasicMats(images), getPhongMats(images)];
        super(geometry, materials[0]);

        this.materials = materials;
        this.currMaterial = 0;

        this.position.set(position[0], position[1], position[2]);
    }

    reset() {
        if (this.currMaterial) {
            this.changeMaterial();
        }
        if (this.material[0].wireframe) {
            this.changeWireframe();
        }
    }

    changeMaterial() {
        this.currMaterial ^= 1;
        this.material = this.materials[this.currMaterial];
    }

    changeWireframe() {

        for (let i = 0; i < this.materials.length; i++) {
            this.materials[i].forEach(function (material) {
                material.wireframe = !material.wireframe;
            });
        }
    }
}


class Cube extends Solid {

    constructor(size, position, images) {

        let geometry = new THREE.CubeGeometry(size[0], size[1], size[2], size[0]/2, size[1]/2, size[2]/2);

        super(geometry, position, images);
    }
}


class Sphere extends Solid {

    constructor(radius, position, images) {

        let geometry = new THREE.SphereGeometry(radius, radius*2.5, radius*2.5);

        super(geometry, position, images);
    }
}
