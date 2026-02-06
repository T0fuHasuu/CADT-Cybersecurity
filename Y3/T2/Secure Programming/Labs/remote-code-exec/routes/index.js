const { executionAsyncId } = require("async_hooks");

module.exports = (db) => {
    const bcrypt = require("bcryptjs");
    const router = require("express").Router();
    const jwt = require("jsonwebtoken");
    const jsrender = require("jsrender");
    const _ = require("lodash");
    const { exec } = require('child_process');
    const AuthMiddleware = require("../middleware/AuthMiddleware");
    const { tokenKey } = require("../utils/authorization");
    const User = require("../utils/user");

    const response = (data) => ({ message: data });

    router.get("/", AuthMiddleware, async (req, res) => {
        try {
            const users = await db.Users.findAll();
            res.render("index", { users: users });
        } catch (error) {
            console.error(error);
            res.status(500).send("Something went wrong!");
        }
    });

    router.get("/register", async (req, res) => {
        res.render("register");
    });

    router.post("/register", async (req, res) => {
        try {
            const username = req.body.username;
            const password = req.body.password;

            if (!username || !password) {
                return res
                    .status(400)
                    .send(response("Username and password are required"));
            }

            const existingUser = await db.Users.findOne({
                where: { username: username },
            });
            if (existingUser) {
                return res
                    .status(400)
                    .send(response("Username already exists"));
            }

            await db.Users.create({
                username: username,
                password: bcrypt.hashSync(password)
            }).then(() => {
                res.send(response("User registered successfully"));
            });
        } catch (error) {
            console.error(error);
            res.status(500).send({
                error: "Something went wrong!",
            });
        }
    });

    router.get("/login", async (req, res) => {
        res.render("login");
    });

    router.post("/login", async (req, res) => {
        try {
            const username = req.body.username;
            const password = req.body.password;

            if (!username || !password) {
                return res
                    .status(400)
                    .send(response("Username and password are required"));
            }

            const user = await db.Users.findOne({
                where: { username: username },
            });
            if (!user) {
                return res
                    .status(400)
                    .send(response("Invalid username or password"));
            }

            const validPassword = bcrypt.compareSync(password, user.password);
            if (!validPassword) {
                return res
                    .status(400)
                    .send(response("Invalid username or password"));
            }

            const token = jwt.sign({ username: user.username }, tokenKey, {
                expiresIn: "1h",
            });

            res.cookie("session", token);

            return res.status(200).send(response("Logged in successfully"));
        } catch (error) {
            console.error(error);
            res.status(500).send({
                error: "Something went wrong!",
            });
        }
    });

    // update user profile
    router.post("/update", AuthMiddleware, async (req, res) => {
        try {
            const sessionCookie = req.cookies.session;
            const username = jwt.verify(sessionCookie, tokenKey).username;

            // sanitize to avoid command injection
            if (req.body.deviceIP){
                if (req.body.deviceIP.match(/[^a-zA-Z0-9\.]/)) {
                    return res.status(400).send(response("Invalid Characters in DeviceIP!"));
                }
            }

            // create User object
            let userObject = new User(username);
            await userObject.init();

            // sanitize keys to avoid prototype pollution
            let sanitizedObject = {};
            for (const property in req.body) {
                if (property.includes('__proto__')) { continue; }
    
                sanitizedObject[property] = req.body[property];
            }

            // merge User object with updated properties
            _.merge(userObject, sanitizedObject);

            // update DB
            await userObject.writeToDB();

            return res.status(200).send(response("Successfully updated User!"));

        } catch (error) {
            console.error(error);
            res.status(500).send({
                error: "Something went wrong!",
            });
        }
    });

    // ping device IP
    router.get("/ping", AuthMiddleware, async (req, res) => {
        try {
            const sessionCookie = req.cookies.session;
            const username = jwt.verify(sessionCookie, tokenKey).username;

            // create User object
            let userObject = new User(username);
            await userObject.init();

            if (!userObject.deviceIP) {
                return res.status(400).send(response("Please configure your device IP first!"));
            }

            exec(`ping -c 1 ${userObject.deviceIP}`, (error, stdout, stderr) => {
                return res.render("ping", { ping_result: stdout.replace(/\n/g, "<br/>") + stderr.replace(/\n/g, "<br/>") });
            });

        } catch (error) {
            console.error(error);
            res.status(500).send({
                error: "Something went wrong!",
            });
        }
    });

    router.get("/logout", async (req, res) => {
        res.clearCookie("session");
        return res.redirect("/");
    });

    return router;
};
