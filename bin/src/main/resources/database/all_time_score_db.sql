CREATE TABLE all_time_score(

    user_id INT PRIMARY KEY,
    total_points INT NOT NULL DEFAULT 0, 
    cww INT NOT NULL DEFAULT 0, -- cww= correct worldcup winner
    cg INT NOT NULL DEFAULT 0, -- cg = correct guesse
    cgd INT NOT NULL DEFAULT 0, -- ctd = correct goal difference
    cw INT NOT NULL DEFAULT 0, -- cw = correct winner

    FOREIGN KEY (user_id) REFERENCES users(user_id)
);