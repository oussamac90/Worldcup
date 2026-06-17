package com.goalkeeperdash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * GoalKeeper Dash backend entry point.
 *
 * <p>The application is organized into four internal modules under
 * {@code com.goalkeeperdash}: {@code user}, {@code leaderboard}, {@code game}
 * and {@code backoffice}, plus a shared {@code common} package. They share one
 * deployable jar but communicate only through service interfaces.
 */
@SpringBootApplication
@EnableScheduling
public class GoalkeeperDashApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoalkeeperDashApplication.class, args);
    }
}
