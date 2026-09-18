package com.naima.square_games.DAO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class GameTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String ownerId;
    public String name;
    public boolean removed;
    public Integer x;
    public Integer y;

    // Constructeur sans argument indispensable pour JPA/Hibernate
    public GameTokenEntity(){}
}
