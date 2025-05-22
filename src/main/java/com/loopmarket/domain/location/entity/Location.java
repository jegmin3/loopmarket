package com.loopmarket.domain.location.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "location")
public class Location {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long locationId;

  private String roadAddress;
  private String jibunAddress;
  private String postalCode;
  private String extraDetail;
  private Double latitude;
  private Double longitude;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
