package practice.newbalance.domain.item;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import practice.newbalance.common.ErrorCode;
import practice.newbalance.common.exception.CustomException;

@Entity @Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_opt")
public class ProductOption {

    @Id
    @GeneratedValue
    @Column(name = "product_opt_id")
    private Long id;

    private String color;
    private String size;
    private int quantity;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;


    //비즈니스 로직
    public int addStock(int count){
        return this.quantity += count;
    }

    public int removeStock(int count){
        int result = this.quantity - count;
        if(result < 0){
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.OUT_OF_STOCK);
        }
        this.quantity = result;
        return result;
    }

}
