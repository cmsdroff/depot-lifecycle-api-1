package depotlifecycle;

import depotlifecycle.domain.InsuranceCoverage;
import depotlifecycle.domain.Party;
import depotlifecycle.domain.Redelivery;
import depotlifecycle.domain.RedeliveryDetail;
import depotlifecycle.domain.RedeliveryUnit;
import depotlifecycle.domain.Release;
import depotlifecycle.domain.ReleaseDetail;
import depotlifecycle.domain.ReleaseUnit;
import depotlifecycle.repositories.PartyRepository;
import depotlifecycle.repositories.RedeliveryRepository;
import depotlifecycle.repositories.ReleaseRepository;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.event.annotation.EventListener;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

@OpenAPIDefinition(
    info = @Info(
        title = "Depot Life Cycle",
        version = "2.2.4",
        description = "# Purpose\n\n" +
            " A depot centric API for managing the interchange activity & repair lifecycle of a shipping container.  The API is expected to be used by Customers, Depots, and Owners to facilitate real time communication between systems instead of traditional EDI files.\n" +
            "\n\n\n" +
            " # Overview\n\n" +
            " The depot lifecycle API is a RESTful API.  It's requests & responses are loosely based upon traditional EDI files.  For code definitions, explanations, or traditional EDI definitions refer to [IICL TB 002, February 2003](https://www.iicl.org/iiclforms/assets/File/public/bulletins/TB002_EDIS_February_2003.pdf).\n" +
            "\n\n\n" +
            " # JSON\n\n" +
            " The requests and responses of this API are formatted according to the [JSON](https://www.json.org/) standard.\n\n" +
            " ## Null vs Absent\n\n" +
            " For all types other than arrays, nullable and absent are equivalent by this specification.  For example, if a non-array attribute is marked optional it may be excluded from the JSON or it may be explicitly set to null.  However, to keep request sizes small, it is recommended to prefer excluding attributes rather than sending an explicit null.\n\n" +
            " For arrays, null should be avoided.  For example, if the array is empty or there are no values expected for the array then either the field can be excluded from the JSON or an empty array should be transmitted.\n\n" +
            " ## Default Values\n\n" +
            " Anywhere a default value is defined in this API, if null is transmitted, it is assumed the default value will be used.\n\n" +
            " ## Forward Compatibility\n\n" +
            " To maintain compatibility with future versions of this specification, it is recommended to never fail on unknown properties in requests or responses.\n\n" +
            "\n\n\n" +
            " # Reference\n\n" +
            " This API is documented by the [OpenAPI](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md) specification.  This page was initially created by the [Swagger-Editor](https://github.com/swagger-api/swagger-editor) tool and later autogenerated via the [sample java application](https://github.com/IICL-API/depot-lifecycle-api/tree/main/samples/java) in the GitHub repository.  The *yaml* specification file can be found [here](depot-lifecycle-openapi.yaml).\n" +
            "\n\n\n" +
            " # Implementation Expectations\n\n" +
            " Not all parties are required or expected to implement every feature. Any feature not implemented should return a http status code of `501`.\n" +
            "\n\n\n" +
            " # APIs to Implement\n\n" +
            " While anyone is free to implement all or a selected set of the APIs listed here, the typical use case would be for Depot to Lessor communication.  For the Depot, they would call the Lessor's system using the following APIs:\n" +
            " 1. POST - create a gate record \n" +
            " 2. PUT - update a gate record \n" +
            " 3. POST - create an estimate revision \n" +
            " 4. PUT - marks a shipping container repaired \n\n" +
            " These APIs allow the Depot to report the initial gate in movement, issue gate data corrections (i.e. the damage status or activity date), participate in the estimate process (i.e. creation, surveys, and customer approval), notify when a shipping container is repaired, and create a gate out for leaving the depot.\n\n" +
            "\n\n\n" +
            " # Depot Identifiers\n\n" +
            " Depot or facility identifiers, henceforth referred to as `EDI Address`, `CompanyId`, or `Party Id` in this document, are maintained by The Bureau International des Containers (BIC) and officially known as the BIC Facility Code. For codes and more information please refer to [https://www.bic-code.org/bic-facility-codes](https://www.bic-code.org/bic-facility-codes/).\n" +
            "\n\n\n" +
            " # Deprecation\n\n" +
            " If this API version were to be discontinued, a minimum of 6 months time would pass before it's removal.\n" +
            "\n\n\n" +
            " # Change Log\n\n" +
            " * 2.2.1\n\n" +
            "    - Estimate Photo support for line items and the overall estimate (header).\n\n" +
            " * 2.2.2\n\n" +
            "    - Estimate Line Items are optional.\n\n" +
            "    - Error Responses optionally support multiple messages via a details field.\n\n" +
            "    - Various improvements for BETA APIs.\n\n" +
            " * 2.2.3\n\n" +
            "    - Party model (optional):\n\n" +
            "        - Latitude & Longitude\n\n" +
            "        - Physical Address\n\n" +
            "        - Contact Phone, Fax, & Phone Number fields\n\n" +
            "    - Insurance Coverage model:\n\n" +
            "         - Add indicator for insurance coverage applicable to CTL scenarios\n\n" +
            "    - Release & Redelivery models - Status field\n\n" +
            "    - Redelivery model - Add field to store estimate recipient emails\n\n" +
            "    - RedeliveryUnit model - Add cargo # to identify type of cargo by UN or other standard numbers\n\n" +
            "    - RedeliveryUnit model - Add technical bulletins field\n\n" +
            "    - Release search - Add search flag to find candidate units\n\n" +
            " * 2.2.4\n\n" +
            "    - Added separate section for shared models to documentation (show the request based version where possible to avoid confusion).\n\n" +
            "    - Documented suggested security schemes for easier adoption with API importing tools.\n\n" +
            "    - Clarified best practices for JSON produced or consumed by this API.\n\n" +
            "        - Clarified Default Value handling.\n\n" +
            "        - Clarified Null vs Absent Value handling.\n\n" +
            "        - Documented forward compatability best practices.\n\n" +
            "    - Fixed type definitions for query parameters - `string` will now show instead of `object`.\n\n" +
            "    - Fixed type definitions for path parameters - `string` will now show instead of `object`.\n\n" +
            "    - Fixed minimum length documentation for damage location code on Estimate Line Item.\n\n" +
            "    - Add `type` to Gate Status API.\n\n" +
            "    - Change `currentInspectionCriteria` on Gate Status & Create APIs to optional since Estimates are not always required.\n\n" +
            "    - Add `gateCheck` option to Redelivery & Release search APIs, defaulted so previous query behavior is the same.\n\n" +
            "    - Estimate Photo Upload Proposal:\n\n" +
            "        - For all photo APIs, assume a default status of `BEFORE` so that it is not required.\n\n" +
            "        - Estimate Photo Upload endpoint\n\n" +
            "        - Added identifier field, 'id', to EstimateAllocation to use to upload photos\n\n" +
            "\n\n\n" +
            " # Security & Authentication\n\n" +
            " To ensure secure communication, all endpoints of this API should use the https protocol instead of http.  Authentication methods will differ between systems, but two popular methods are JSON Web Tokens and Static Tokens.  Examples for both of these follow.\n" +
            "\n" +
            "## JSON Web Token\n\n" +
            " [JSON Web Token, or JWT](https://jwt.io/), is used for stateless authentication.  Since all requests are sent using https, tokens are not encrypted.  Tokens follow the [RFC 6750 Bearer Token](https://tools.ietf.org/html/rfc6750) format.\n" +
            "\n" +
            "### 1. Example JWT Token\n\n" +
            " ```\n" +
            "\n" +
            " {\n" +
            "\n" +
            " \"username\": \"jdoe\",\n" +
            "\n" +
            " \"roles\": [\n" +
            "\n" +
            " \"ROLE_GATE_CREATE\",\n" +
            "\n" +
            " \"ROLE_GATE_UPDATE\"\n" +
            "\n" +
            " ],\n" +
            "\n" +
            " \"email\":\"j.doe@example.com\",\n" +
            "\n" +
            " \"token_type\":\"Bearer\",\n" +
            "\n" +
            " \"access_token\":\"eyJhbGciOiJIUzI1NiJ9...\",\n" +
            "\n" +
            " \"expires_in\":3600,        \n" +
            "\n" +
            " \"refresh_token\":\"eyJhbGciOiJIUzI1NiJ9...\"\n" +
            "\n" +
            " }\n" +
            "\n" +
            " ```\n" +
            "\n\n\n\n\n" +
            " An access_token is provided for authentication to API endpoints and a refresh_token is provided to generate a new access_token when one expires.\n\n" +
            "\n\n\n\n" +
            " ### 2. Obtaining an access token\n" +
            "\n" +
            " Issuing a POST request to `/api/login` with a username and password payload will cause a JWT token to be issued in the response.\n" +
            "\n" +
            " ```\n" +
            "\n" +
            " POST /api/login HTTP/1.1\n" +
            "\n" +
            " Content-Type: text/plain; charset=utf-8\n" +
            "\n" +
            " Host: www.example.com\n" +
            "\n\n\n\n\n" +
            " {\n" +
            "\n" +
            " \"username\": \"jdoe\",\n" +
            "\n" +
            " \"password\": \"jdoepassword\"\n" +
            "\n" +
            " }\n" +
            "\n" +
            " ```\n" +
            "\n\n\n\n\n" +
            " ### 3. Refreshing an expired token\n" +
            "\n" +
            " Tokens created by invoking the login endpoint expire after a determined period.  Issuing a POST request to `/oauth/access_token` with: the refresh_token from the JWT token previously issued and a grant_type of refresh_token will reissue a new JWT token.\n" +
            "\n" +
            " ```\n" +
            "\n" +
            " POST /oauth/access_token HTTP/1.1\n" +
            "\n" +
            " Host: www.example.com\n" +
            "\n" +
            " Content-Type: application/x-www-form-urlencoded\n" +
            "\n\n\n\n\n" +
            " grant_type=refresh_token&refresh_token=eyJhbGciOiJIUzI1NiJ9...\n" +
            "\n" +
            " ```\n" +
            "\n\n\n\n\n" +
            " ### 4. Checking if a token is valid\n" +
            "\n" +
            " Any token can be checked if it's still valid by issuing a POST request to `/api/validate`.\n" +
            "\n" +
            " ```\n" +
            "\n" +
            " GET /api/validate HTTP/1.1\n" +
            "\n" +
            " Host: www.example.com\n" +
            "\n" +
            " Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...\n" +
            "\n" +
            " Content-Type: application/json; charset=utf-8\n" +
            "\n" +
            " ```\n" +
            "\n\n\n\n\n" +
            " ### 5. Accessing a protected resource\n" +
            "\n" +
            " Use the 'Authorization' header to supply the JWT for authentication to a protected resource.\n" +
            "\n" +
            " ```\n" +
            "\n" +
            " GET /api/v2/gate/CONU1234561 HTTP/1.1\n" +
            "\n" +
            " Host: www.example.com\n" +
            "\n" +
            " Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...\n" +
            "\n" +
            " ```\n" +
            "\n" +
            "## Static Tokens\n" +
            "\n" +
            " A static token is assigned by an external process or system.  After assignment, any documented API endpoint can be invoked by including a header of \"Authorizaton\" and the value of the token.  Some systems will assign static tokens that are JWTs, in that case \"Bearer \" should prefix the token in the header value.\n" +
            "\n" +
            "### Static (JWT) Token Example\n" +
            "\n" +
            " Assuming an assigned JWT of \"eyJhbGciOiJIUzI1NiJ9\", an API gate show request would look like: \n" +
            "\n" +
            "```\n" +
            "\n" +
            " GET /api/v2/gate/CONU1234561 HTTP/1.1\n" +
            "\n" +
            " Host: www.example.com\n" +
            "\n" +
            " Authorization: Bearer eyJhbGciOiJIUzI1NiJ9\n" +
            "\n" +
            " ```\n",
        license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
        contact = @Contact(email = "technical@iicl.org")
    ),
    externalDocs = @ExternalDocumentation(description = "Find out more about this api", url = "https://github.com/IICL-API/depot-lifecycle-api"),
    tags = {
        @Tag(name = "estimate proposals", description = "*new estimate apis that are being considered*"),
        @Tag(name = "redelivery", description = "*turn in approval for shipping containers*"),
        @Tag(name = "release", description = "*lease out approval for shipping containers*"),
        @Tag(name = "gate", description = "*manage gate ins and gate outs of shipping containers*"),
        @Tag(name = "estimate", description = "*a damage or upgrade estimate for a shipping container after turn in*"),
        @Tag(name = "workOrder", description = "*manage damage estimates that are approved for repair*"),
        @Tag(name="m_error_response", description="<SchemaDefinition schemaRef=\"#/components/schemas/ErrorResponse\" />", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="ErrorResponse")})}),
        @Tag(name="m_insurance_coverage", description="<SchemaDefinition schemaRef=\"#/components/schemas/InsuranceCoverage\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="InsuranceCoverage")})}),
        @Tag(name="m_party", description="<SchemaDefinition schemaRef=\"#/components/schemas/Party\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="Party")})}),
        @Tag(name="m_pending_response", description="<SchemaDefinition schemaRef=\"#/components/schemas/PendingResponse\" />", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="PendingResponse")})}),
        @Tag(name="m_redelivery", description="<SchemaDefinition schemaRef=\"#/components/schemas/Redelivery\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="Redelivery")})}),
        @Tag(name="m_redelivery_detail", description="<SchemaDefinition schemaRef=\"#/components/schemas/RedeliveryDetail\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="RedeliveryDetail")})}),
        @Tag(name="m_redelivery_unit", description="<SchemaDefinition schemaRef=\"#/components/schemas/RedeliveryUnit\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="RedeliveryUnit")})}),
        @Tag(name="m_release", description="<SchemaDefinition schemaRef=\"#/components/schemas/Release\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="Release")})}),
        @Tag(name="m_release_detail", description="<SchemaDefinition schemaRef=\"#/components/schemas/ReleaseDetail\" />", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="ReleaseDetail")})}),
        @Tag(name="m_release_detail_criteria", description="<SchemaDefinition schemaRef=\"#/components/schemas/ReleaseDetailCriteria\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="ReleaseDetailCriteria")})}),
        @Tag(name="m_release_unit", description="<SchemaDefinition schemaRef=\"#/components/schemas/ReleaseUnit\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="ReleaseUnit")})}),
        @Tag(name="m_gate_create", description="<SchemaDefinition schemaRef=\"#/components/schemas/GateCreateRequest\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="GateCreateRequest")})}),
        @Tag(name="m_gate_create_photo", description="<SchemaDefinition schemaRef=\"#/components/schemas/GateCreatePhoto\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="GateCreatePhoto")})}),
        @Tag(name="m_gate_response", description="<SchemaDefinition schemaRef=\"#/components/schemas/GateResponse\"/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="GateResponse")})}),
        @Tag(name="m_gate_status", description="<SchemaDefinition schemaRef=\"#/components/schemas/GateStatus\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="GateStatus")})}),
        @Tag(name="m_gate_update_request", description="<SchemaDefinition schemaRef=\"#/components/schemas/GateUpdateRequest\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="GateUpdateRequest")})}),
        @Tag(name="m_gate_update_photo", description="<SchemaDefinition schemaRef=\"#/components/schemas/GateUpdatePhoto\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="GateUpdatePhoto")})}),
        @Tag(name="m_estimate", description="<SchemaDefinition schemaRef=\"#/components/schemas/Estimate\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="Estimate")})}),
        @Tag(name="m_estimate_photo", description="<SchemaDefinition schemaRef=\"#/components/schemas/EstimatePhoto\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="EstimatePhoto")})}),
        @Tag(name="m_estimate_line_item", description="<SchemaDefinition schemaRef=\"#/components/schemas/EstimateLineItem\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="EstimateLineItem")})}),
        @Tag(name="m_estimate_line_item_part", description="<SchemaDefinition schemaRef=\"#/components/schemas/EstimateLineItemPart\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="EstimateLineItemPart")})}),
        @Tag(name="m_estimate_line_item_photo", description="<SchemaDefinition schemaRef=\"#/components/schemas/EstimateLineItemPhoto\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="EstimateLineItemPhoto")})}),
        @Tag(name="m_estimate_allocation", description="<SchemaDefinition schemaRef=\"#/components/schemas/EstimateAllocation\"/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="EstimateAllocation")})}),
        @Tag(name="m_preliminary_decision", description="<SchemaDefinition schemaRef=\"#/components/schemas/PreliminaryDecision\"/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="PreliminaryDecision")})}),
        @Tag(name="m_estimate_customer_approval", description="<SchemaDefinition schemaRef=\"#/components/schemas/EstimateCustomerApproval\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="EstimateCustomerApproval")})}),
        @Tag(name="m_work_order", description="<SchemaDefinition schemaRef=\"#/components/schemas/WorkOrder\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="WorkOrder")})}),
        @Tag(name="m_work_order_unit", description="<SchemaDefinition schemaRef=\"#/components/schemas/WorkOrderUnit\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="WorkOrderUnit")})}),
        @Tag(name="m_repair_complete", description="<SchemaDefinition schemaRef=\"#/components/schemas/RepairComplete\" showReadOnly={false}/>", extensions = { @Extension(properties = {@ExtensionProperty(name = "x-displayName", value="RepairComplete")})})
    },
    extensions = {
        @Extension(properties = {@ExtensionProperty(name = "tagGroups", value = "[{ \"name\": \"API: Proposals (Alpha)\", \"tags\": [ \"estimate proposals\" ] }, { \"name\": \"API: Under Development (Beta)\", \"tags\": [ \"redelivery\", \"release\" ] }, { \"name\": \"API: Production Ready\", \"tags\": [ \"gate\", \"estimate\", \"workOrder\" ] }, { \"name\": \"Models\", \"tags\": [ \"m_error_response\", \"m_insurance_coverage\", \"m_party\", \"m_pending_response\", \"m_redelivery\", \"m_redelivery_detail\", \"m_redelivery_unit\", \"m_release\", \"m_release_detail\", \"m_release_detail_criteria\", \"m_release_unit\", \"m_gate_create\", \"m_gate_create_photo\", \"m_gate_response\", \"m_gate_status\", \"m_gate_update_request\", \"m_gate_update_photo\", \"m_estimate\", \"m_estimate_photo\", \"m_estimate_line_item\", \"m_estimate_line_item_part\", \"m_estimate_line_item_photo\", \"m_estimate_allocation\", \"m_preliminary_decision\", \"m_estimate_customer_approval\", \"m_work_order\", \"m_work_order_unit\", \"m_repair_complete\" ] }]", parseValue = true)})
    },
    servers = {
        @Server(url = "https://api.example.com/examplecontextpath")
    },
    security = {
        @SecurityRequirement(name = "Dynamic_Token"),
        @SecurityRequirement(name = "Static_Token"),
    }
)
@SecuritySchemes (
    value = {
            @SecurityScheme(
                    name = "Dynamic_Token",
                    description = "Dynamic JWT Bearer Authentication",
                    type = SecuritySchemeType.HTTP,
                    bearerFormat = "JWT",
                    scheme = "bearer"
            ),
            @SecurityScheme(
                    name = "Static_Token",
                    description = "Static JWT Bearer Authentication",
                    type = SecuritySchemeType.HTTP,
                    bearerFormat = "JWT",
                    scheme = "bearer"
            )
    }
)
@Singleton
@RequiredArgsConstructor
public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private final RedeliveryRepository redeliveryRepository;
    private final ReleaseRepository releaseRepository;
    private final PartyRepository partyRepository;

    public static void main(String[] args) {
        Micronaut.run(Application.class);
    }

    @EventListener
    void init(StartupEvent event) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Populating data");
        }

        buildTestData();
    }

    private void buildTestData() {
        Party depot1 = new Party();
        depot1.setCompanyId("DEHAMCMRA");
        depot1.setUserCode("JDOE");
        depot1.setUserName("John Doe");
        depot1.setCode("HAMG");
        depot1.setName("Depot Operator #1");

        Party depot2 = new Party();
        depot2.setCompanyId("DEHAMCMRB");
        depot2.setUserCode("JDOE");
        depot2.setUserName("John Doe");
        depot2.setCode("HAMB");
        depot2.setName("Depot Operator #2");

        Party customer = new Party();
        customer.setCompanyId("GBLONCUST");
        customer.setUserCode("JD");
        customer.setUserName("Jane Doe");
        customer.setCode("EXCUST");
        customer.setName("Example Customer");

        Party owner = new Party();
        owner.setCompanyId("USSFOEXAM");
        owner.setUserCode("JD");
        owner.setUserName("Jane Doe");
        owner.setCode("EXAM");
        owner.setName("Example Lessor Name");

        partyRepository.saveAll(Arrays.asList(depot1, depot2, customer, owner));

        buildRedeliveries(depot1, depot2, customer, owner);
        buildReleases(depot1, depot2, customer, owner);
    }

    private void buildReleases(Party depot1, Party depot2, Party customer, Party owner) {
        Release release = new Release();
        release.setStatus("APPROVED");
        release.setReleaseNumber("RHAMG134512");
        release.setType("BOOK");
        release.setApprovalDate(getLocal(LocalDateTime.now().minusDays(5)));
        release.setExpirationDate(getLocal(LocalDateTime.now().plusMonths(4)));
        release.setComments(Arrays.asList("an example release level comment"));
        release.setDepot(depot1);
        release.setOwner(owner);
        release.setRecipient(depot1);
        release.setQuantity(1);

        ReleaseDetail blanketDetail = new ReleaseDetail();
        blanketDetail.setCustomer(customer);
        blanketDetail.setContract("EXCUST01-100000");
        blanketDetail.setEquipment("22G1");
        blanketDetail.setGrade("IICL");
        blanketDetail.setQuantity(1);

        ReleaseDetail unitDetail = new ReleaseDetail();
        unitDetail.setCustomer(customer);
        unitDetail.setContract("EXCUST01-100000");
        unitDetail.setEquipment("42G1");
        unitDetail.setGrade("IICL");
        unitDetail.setQuantity(1);

        ReleaseUnit unit1 = new ReleaseUnit();
        unit1.setUnitNumber("CONU1234561");
        unit1.setComments(Arrays.asList("Example unit comment #1."));
        unit1.setStatus("TIED");

        ReleaseUnit unit2 = new ReleaseUnit();
        unit2.setUnitNumber("CONU1234526");
        unit2.setComments(Arrays.asList("Example unit comment #2."));
        unit2.setStatus("TIED");
        unit2.setManufactureDate(LocalDate.of(2012, 1, 1));

        release.getDetails().add(blanketDetail);
        release.getDetails().add(unitDetail);
        unitDetail.getUnits().add(unit1);
        unitDetail.getUnits().add(unit2);

        releaseRepository.save(release);
    }

    private void buildRedeliveries(Party depot1, Party depot2, Party customer, Party owner) {
        Redelivery redelivery = new Redelivery();
        redelivery.setStatus("APPROVED");
        redelivery.setRedeliveryNumber("AHAMG33141");
        redelivery.setApprovalDate(getLocal(LocalDateTime.now().minusDays(5)));
        redelivery.setExpirationDate(getLocal(LocalDateTime.now().plusMonths(4)));
        redelivery.setComments(Arrays.asList("an example redelivery level comment"));
        redelivery.setDepot(depot1);
        redelivery.setRecipient(depot1);
        redelivery.setOwner(owner);
        redelivery.setQuantity(2);

        RedeliveryDetail noInsuranceDetail = new RedeliveryDetail();
        noInsuranceDetail.setCustomer(customer);
        noInsuranceDetail.setContract("EXCUST01-100000");
        noInsuranceDetail.setEquipment("22G1");
        noInsuranceDetail.setQuantity(1);

        InsuranceCoverage coverage = new InsuranceCoverage();
        coverage.setAmountCovered(new BigDecimal(2000.0));
        coverage.setAmountCurrency("USD");
        coverage.setAllOrNothing(false);
        coverage.setExceptions(Arrays.asList("Exception #1", "Exception #2"));
        coverage.setExclusions(Arrays.asList("Exclusion #1", "Exclusion #2"));
        coverage.setInclusions(Arrays.asList("Inclusion #1", "Inclusion #2"));

        RedeliveryDetail insuranceDetail = new RedeliveryDetail();
        insuranceDetail.setCustomer(customer);
        insuranceDetail.setContract("EXCUST01-100000");
        insuranceDetail.setEquipment("22G2");
        insuranceDetail.setGrade("IICL");
        insuranceDetail.setInsuranceCoverage(coverage);
        insuranceDetail.setQuantity(1);

        RedeliveryUnit unit1 = new RedeliveryUnit();
        unit1.setUnitNumber("CONU1234561");
        unit1.setManufactureDate(LocalDate.of(2012, 1, 1));
        unit1.setLastOnHireDate(LocalDate.of(2012, 2, 1));
        unit1.setLastOnHireLocation(depot2);
        unit1.setComments(Arrays.asList("Example unit comment #1."));
        unit1.setBillingParty(depot1);
        unit1.setInspectionCriteria("IICL");
        unit1.setStatus("TIED");

        RedeliveryUnit unit2 = new RedeliveryUnit();
        unit2.setUnitNumber("CONU1234526");
        unit2.setManufactureDate(LocalDate.of(2012, 1, 1));
        unit2.setComments(Arrays.asList("Example unit comment #2."));
        unit2.setBillingParty(depot1);
        unit2.setInspectionCriteria("CWCA");
        unit2.setStatus("TIED");

        redelivery.getDetails().add(insuranceDetail);
        redelivery.getDetails().add(noInsuranceDetail);
        noInsuranceDetail.getUnits().add(unit1);
        insuranceDetail.getUnits().add(unit2);

        redeliveryRepository.save(redelivery);
    }

    private static ZonedDateTime getLocal(LocalDateTime date) {
        //Don't actually store a time zone for the purposes of this application
        return ZonedDateTime.of(date, ZoneId.systemDefault());
    }
}
