var exec = require("cordova/exec");

exports.checkLiveness = function (arg0, success, error) {
  exec(success, error, "FaceoffLivenessPlugin", "checkLiveness", [arg0]);
};
