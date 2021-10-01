package com.chilterra.gateway.persistence.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Data
@ToString(exclude = {"gateway"})
@Entity
@Table(name = "gateway_device")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "gateway_id")
    private Gateway gateway;
    private String vendor;
    private Date createdDate = new Date();
    @Enumerated(EnumType.STRING)
    private DeviceStatusEnum status;
}
