const db = require("../utils/database");

// custom User class
class User {
    constructor(username) {
        this.username = username;
    }
    
    // initialize User object from DB
    async init() {
        const dbUser = await db.Users.findOne({ where: { username: this.username }});

        if (!dbUser){ return; }

        // set all non-null properties
        for (const property in dbUser.dataValues) {
            if (!dbUser[property]) { continue; }

            this[property] = dbUser[property];
        } 
    }

    async writeToDB() {
        const dbUser = await db.Users.findOne({ where: {username: this.username} });
        
        // update all non-null properties
        for (const property in this) {
            if (!this[property]) { continue; }

            dbUser[property] = this[property];
        }

        await dbUser.save();
    }
}

module.exports = User;