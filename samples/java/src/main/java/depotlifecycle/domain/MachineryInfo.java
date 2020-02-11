package depotlifecycle.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@JsonView
@NoArgsConstructor
@Entity
@Table
@Schema(description = "details the cooling machinery attached to a shipping container")
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id"})
@Introspected
public class MachineryInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    Long id;

    @Schema(description = "The cooling machinery manufacturer", maxLength = 50, required = false, example = "Carrier Transicold")
    @Column(length = 50, nullable = true)
    String manufacturer;

    @Schema(description = "The machinery model name", maxLength = 50, required = false, example = "ThinLine")
    @Column(length = 50, nullable = true)
    String modelName;

    @Schema(description = "The machinery model number", maxLength = 50, required = false, example = "69NT40-541-301")
    @Column(length = 50, nullable = true)
    String modelNumber;
}
