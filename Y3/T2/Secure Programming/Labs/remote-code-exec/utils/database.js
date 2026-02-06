const Sequelize = require("sequelize");
const bcrypt = require("bcryptjs");

const adminPassword = process.env.adminpass;

const sequelize = new Sequelize({
    dialect: "sqlite",
    storage: "storage.Database",
    define: {
        timestamps: false,
    },
});

const Database = {};

Database.sequelize = sequelize;

Database.Users = sequelize.define("user", {
    id: {
        type: Sequelize.INTEGER,
        autoIncrement: true,
        primaryKey: true,
        allowNull: false,
        unique: true,
    },
    username: {
        type: Sequelize.STRING,
        allowNull: false,
        unique: true,
    },
    password: {
        type: Sequelize.STRING,
        allowNull: false,
    },
    deviceIP: {
        type: Sequelize.STRING,
        allowNull: true,
    }
});

Database.connect = async () => {
    try {
        await sequelize.authenticate();
    } catch (error) {
        console.error("Unable to connect to the database:", error);
    }
};

Database.create = async () => {
    try {
        await Database.Users.sync({ force: true });
        await Database.Users.create({
            username: "admin",
            password: bcrypt.hashSync(adminPassword, 10),
        });
    } catch (error) {
        console.error("Error creating table:", error);
    }
};

module.exports = Database;
