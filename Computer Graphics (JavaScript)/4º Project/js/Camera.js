'use strict';


class PerspectiveCam extends THREE.PerspectiveCamera {

    constructor(scenePos, position) {

        super(70, window.innerWidth / window.innerHeight, 1, 1000);

        this.position.set(position[0], position[1], position[2]);
        this.lookAt(scenePos);
    }

    resize() {

        if (window.innerWidth === 0 || window.innerHeight === 0) {
            return;
        }
        this.aspect = window.innerWidth / window.innerHeight;
        this.updateProjectionMatrix();
    }
}
