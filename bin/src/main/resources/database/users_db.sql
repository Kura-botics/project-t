CREATE TABLE users (
    
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    passwort_salt VARCHAR(255) NOT NULL
);