package depotlifecycle.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonView
@NoArgsConstructor
@Entity
@Table
@Schema(description = "groups similar units on a release", requiredProperties = {"customer", "contract", "equipment", "grade", "quantity"})
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id"})
@Introspected
public class ReleaseDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    Long id;

    @Schema(description = "The customer for the contract on this detail.", required = true)
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    Party customer;

    @Schema(description = "the contract code for the given shipping containers", required = true, example = "CNCX05-100000", maxLength = 16)
    @Column(nullable = false, length = 16)
    String contract;

    @Schema(description = "the equipment type ISO code or an internal code if one does not exist for the given shipping containers", required = true, example = "22G1", maxLength = 10)
    @Column(nullable = false, length = 10)
    String equipment;

    @Schema(description = "the current grade of the unit", required = true, example = "IICL", maxLength = 10)
    @Column(nullable = false, length = 10)
    String grade;

    @Schema(description = "an indicator for the upgrades applied to units on this detail.\n\n`FG` - Food grade\n\n`ML` - Malt\n\n`DB` - Dairy Board\n\n`EV` - Evian\n\n`WH` - Whiskey\n\n`SU` - Sugar\n\n`CF` - Coffee\n\n`TB` - Tobacco\n\n`MC` - Milk cartons\n\n`MP` - Milk powder\n\n`AM` - Ammunition\n\n`CH` - Cotton/Hay\n\n`TE` - Tea\n\n`FT` - Flexitank", allowableValues = {"FG", "ML", "DB", "EV", "WH", "SU", "CF", "TB", "MC", "MP", "AM", "CH", "TE", "FT"}, required = false, example = "AM", maxLength = 2)
    @Column(length = 2)
    String upgradeType;

    @Schema(description = "the specific units for this release if defined, if not, assumed blanket (any unit matching criteria can be tied up to the quantity limit of this detail)")
    @OneToMany(orphanRemoval = true, cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    List<ReleaseUnit> units = new ArrayList<>();

    @Schema(description = "additional criteria beyond the required properties of this detail to further restrict units.  i.e. <= 2003 manufacture year. ")
    @OneToMany(orphanRemoval = true, cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    List<ReleaseDetailCriteria> criteria = new ArrayList<>();

    @Schema(description = "comments pertaining to this unit for the intended recipient of this message", example = "['An example detail level comment.']")
    @Lob
    @ElementCollection
    @CollectionTable
    @LazyCollection(LazyCollectionOption.FALSE)
    List<String> comments;

    @Schema(description = "the number of shipping containers assigned to this detail", required = true, minimum = "0", example = "1")
    @Column(nullable = false)
    Integer quantity;

    @Schema(description = "indicator if mechanical equipment must be tested prior to lease out")
    @Column
    Boolean preTripInspectionRequired;

    @Schema(example = "-23", description = "the reefer setpoint / desired temperature")
    @Column
    Integer desiredTemperature;

    @Schema(description = "if the equipment has fresh air ventilation, the rate of the fresh air ventilation", example = "90 CBM", maxLength = 10)
    @Column(length = 10)
    String ventilation;
}
