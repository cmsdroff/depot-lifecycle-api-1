package depotlifecycle;

import com.fasterxml.jackson.annotation.JsonView;
import depotlifecycle.domain.InsuranceCoverage;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Lob;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@JsonView
@NoArgsConstructor
@Schema(description = "A confirmation that a gate completed successfully and the necessary information to perform a damage estimate if required.", requiredProperties = {"adviceNumber", "currentInspectionCriteria"})
@Introspected
public class GateResponse {
    @Schema(pattern = "^[A-Z0-9]{3}[0-9]{3}$", description = "indicator code for this response", example = "TRI521", required = false, maxLength = 6)
    String code;

    @Schema(description = "A descriptive message concerning this gate in", required = false, example = "Info TRI521 - Unit has been gated-in but is not off-hired")
    String message;

    @Schema(required = true, description = "either the submitted advice number for the gate record or the adjusted one", example = "AHAMG000000", maxLength = 16)
    String adviceNumber;

    @Schema(description = "the customer reference for the unit; typically an internal customer identifier or contract code", maxLength = 35, example = "MAEX", required = false)
    String customerReference;

    @Schema(description = "the transaction reference for this activity", maxLength = 35, example = "74454D", required = false)
    String transactionReference;

    @Schema(description = "The applicable insurance coverage for damage estimate purposes", required = false)
    InsuranceCoverage insuranceCoverage;

    @Schema(description = "the exchange rate to convert billed currency to the local currency for damage estimate totals", required = false, type = "number", format = "double", example = "0.8133")
    BigDecimal currentExchangeRate;

    @Schema(description = "comments pertaining to this gate record", example = "['ALL CLEANING MUST BE CODED TO \"O\" FOR OWNER.']")
    @Lob
    @ElementCollection
    @CollectionTable
    @LazyCollection(LazyCollectionOption.FALSE)
    List<String> comments;

    @Schema(description = "the last reported grade or category standard this unit", required = true, example = "IICL", maxLength = 10)
    String currentInspectionCriteria;
}
