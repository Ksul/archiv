require("babel-polyfill");
var context = require.context('./src/test/js/specs', true, /-spec\.js$/);
context.keys().forEach(context)