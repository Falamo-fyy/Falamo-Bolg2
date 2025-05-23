package com.example.mytool.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Getter
@Setter
@Table(name = "role")
public class Role implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, unique = true, length = 20)
    private String name;
    
    @Column(length = 100)
    private String description;

    @ManyToMany(mappedBy = "roles")
    @JsonBackReference
    private Set<User> users;
} 