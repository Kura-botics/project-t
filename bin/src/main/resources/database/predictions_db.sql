CREATE TABLE predictions(
    
    prediction_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    match_id INT NOT NULL,
    prediction_goal_home INT,
    prediction_goal_away INT,
    points_earned INT DEFAULT 0,

    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (match_id) REFERENCES match(match_id)

);