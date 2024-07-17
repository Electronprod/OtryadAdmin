package ru.electronprod.OtryadAdmin.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "squads")
@Data
public class Squad {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int squad_id;
  @Column
  private String squadname;
  @Column(unique = true)
  private int commander; // Связь с таблицей User
}