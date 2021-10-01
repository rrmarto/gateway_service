package com.chilterra.gateway.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table
public class Gateway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;
    @Column(unique = true)
    private String serialNumber;
    private String name;
    private String ipV4;
    @JsonManagedReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "gateway", orphanRemoval = true)
    private List<Device> devices;
}
