const handlebars = require('handlebars');

let template = '{{constructor.length}}';
let templateScript = handlebars.compile(template);
console.log(templateScript({}));
