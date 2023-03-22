package com.jhs.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Data
@SequenceGenerator(
        name = "userstring_seq_generator"
        , sequenceName = "userstring_seq"
        , initialValue = 1
        , allocationSize = 1
)
@Entity(name = "userstring")
@Builder
@AllArgsConstructor
public class UserString {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userstring_seq_generator")
    private Long id;

    private String userstring;

    private LocalDateTime createddate;

    public UserString() {

    }
}
