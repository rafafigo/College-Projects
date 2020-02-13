'use strict';


class OrthographicCam extends THREE.OrthographicCamera {

    constructor(viewSize, scenePos, position) {

        let aspectRatio = window.innerWidth / window.innerHeight;
        const viewWidth = viewSize * aspectRatio;

        super(-viewWidth / 2, viewWidth / 2, viewSize / 2, -viewSize / 2, 1, 1000);

        this.width = viewWidth;
        this.height = viewSize;

        this.viewSize = viewSize;

        this.position.set(position[0], position[1], position[2]);
        this.lookAt(scenePos);
    }

    resize() {

        if (window.innerWidth === 0 || window.innerHeight === 0) {
            return;
        }

        let aspectRatio = window.innerWidth / window.innerHeight;
        let viewWidth = this.viewSize * aspectRatio;

        this.right = viewWidth / 2;
        this.left = -this.right;

        this.top = this.viewSize / 2;
        this.bottom = -this.top;

        this.updateProjectionMatrix();
    }
}


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
