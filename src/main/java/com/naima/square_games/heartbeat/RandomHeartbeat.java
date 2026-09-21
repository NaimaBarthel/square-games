package com.naima.square_games.heartbeat;

import org.springframework.stereotype.Service;

@Service
public class RandomHeartbeat implements HeartbeatSensor{

    @Override
    public int get() {
        return (int) (Math.random() * (230 - 40 + 1)) + 40;
    }
}
