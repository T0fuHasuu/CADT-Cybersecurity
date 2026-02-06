const crypto = require("crypto");

const tokenKey = crypto
    .randomBytes(64 / 2)
    .toString("hex")
    .slice(0, 64);

module.exports = { tokenKey };