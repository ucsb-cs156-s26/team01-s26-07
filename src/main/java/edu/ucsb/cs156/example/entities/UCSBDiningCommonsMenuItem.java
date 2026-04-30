package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** This is a JPA entity that represents a UCSBDiningCommonsMenuItem */
@Data
@AllArgsConstructor
@Builder
@Entity(name = "ucsbdiningcommonsmenuitem")
public class UCSBDiningCommonsMenuItem {

  /** No-args constructor required by JPA. */
  public UCSBDiningCommonsMenuItem() {}

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String diningCommonsCode;
  private String name;
  private String station;
}
