CREATE TABLE matches (
    
    match_id INT PRIMARY KEY AUTO_INCREMENT,
    home_team CHAR(3) NOT NULL,
    away_team CHAR(3) NOT NULL,
    goals_home TINYINT DEFAULT 0,
    goals_away TINYINT DEFAULT 0,
    status TINYINT DEFAULT 0 
    -- 0 = UPCOMING, 1 = LIVE, 2 = FINISHED, 3 = kaputt
);