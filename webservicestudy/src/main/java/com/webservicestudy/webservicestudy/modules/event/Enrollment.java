package com.webservicestudy.webservicestudy.modules.event;

import com.webservicestudy.webservicestudy.modules.account.Account;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

// event를 땡겨오면서 서브그래프로 event의 study로 땡겨옴
@NamedEntityGraph(
        name = "Enrollment.withEventAndStudy",
        attributeNodes = {
                @NamedAttributeNode(value = "event", subgraph = "study")
        },
        subgraphs = @NamedSubgraph(name = "study", attributeNodes = @NamedAttributeNode("study"))
)
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;

    private boolean attended;

}
