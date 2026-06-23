# 2026 Worldcup betting game

A spring boot web application made for mobile-first use. Created for a family based betting game during the FIFA world cup 2026.

## Features
* Live data via external api provided by https://www.football-data.org/
* User friendly interface (made for old people who dont understand the internet)
* Simple point system (exact, goal difference, tendency)
* Worldcup winner prediction
* Responsive scoreboard

## Tech-stack
* **Backend**: Java 21, Spring Boot 3, Java Security
* **Frontend**: Thymeleaf, TailwindCSS, Vanilla JS
* **Database**: PostgreSQL (Docker)

## Important notes:
This project was made in a short time to be live as soon as possible for the already running world cup. Not all features are fully implemented yet and some of the code is not the cleanest code made in history. Some TODOs are still open. 
The frontend is written in German.

## Local start
1. clone this repo
2. adjust application.properies with new database access and use them in the docker-compose.yml as well
3. Start the docker container docker compose up postgres-server -d
4. Go to /src/.../service/FootballApiService.java and insert your own API_KEY to get access to the actual live data
5. Execute ./mvnw spring-boot:run

## AI usage note
Google Gemini was mainly used to create the frontend and only for research in smaller instances in the backend logic
