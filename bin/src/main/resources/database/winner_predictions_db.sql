CREATE TABLE winner_predictions(

    winner_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    team CHAR(3) NOT NULL,

    FOREIGN KEY (user_id) REFERENCES users(user_id)
;)