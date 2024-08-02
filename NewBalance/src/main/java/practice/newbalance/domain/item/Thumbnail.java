package practice.newbalance.domain.item;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_thumbnails")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Thumbnail {

    @Id @GeneratedValue
    @Column(name = "thumbnail_id")
    private Long id;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;

    public Thumbnail(String thumbnailUrl, Product product) {
        this.thumbnailUrl = thumbnailUrl;
        this.product = product;
    }

    public void updateUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

}
