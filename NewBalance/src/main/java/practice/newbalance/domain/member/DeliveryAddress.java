package practice.newbalance.domain.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import practice.newbalance.domain.item.Order;
import practice.newbalance.dto.member.DeliveryAddressDto;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "DeliveryAddr")
public class
DeliveryAddress {
    @Id @GeneratedValue
    private Long id;

    @JoinColumn(name = "member_id")
    private Long memberId;

    private String recipient;

    private String destination;

    private String recipientNumber;

    private String otherNumber;

    private String zipCode;

    private String address;

    private String detailAddress;

    private Boolean defaultYN;

    @JsonIgnore
    @OneToMany(mappedBy = "deliveryAddress")
    private List<Order> order = new ArrayList<>();

    public DeliveryAddressDto toDTO(){
        return DeliveryAddressDto.builder()
                .id(id)
                .recipient(recipient)
                .destination(destination)
                .recipientNumber(recipientNumber)
                .zipCode(zipCode)
                .address(address)
                .detailAddress(detailAddress)
                .defaultYN(defaultYN)
                .build();
    }
}
