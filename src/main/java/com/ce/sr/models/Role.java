package com.ce.sr.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "roles")
public class Role {

  @Id
  private String id;
  private ERole name;

  public Role(ERole name) {
    this.name = name;
  }
}
