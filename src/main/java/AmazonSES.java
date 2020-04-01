import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * AmazonSES Helper class to construct an email and send the email to the respective user's email
 *
 * @author rohan_bharti
 */
public class AmazonSES {

    private String FROM;
    private String TO;
    private String SUBJECT;
    private String BODY;

    private AmazonSimpleEmailService client;

    /**
     * Constructor to set up the SES client and message components
     *
     * @param FROM
     * @param TO
     * @param SUBJECT
     */
    public AmazonSES(String FROM, String TO, String SUBJECT, String billsDueUrls) {
        this.client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        this.FROM = FROM;
        this.TO = TO;
        this.SUBJECT = SUBJECT;
        this.BODY = constructBodyForBillsDue(billsDueUrls);
    }

    /**
     * Sends the Email to the user
     *
     * @return boolean
     */
    public boolean sendEmail() {
        try {
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(TO))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(this.BODY)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(SUBJECT)))
                    .withSource(FROM);
            this.client.sendEmail(request);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Constructs the Html message String Body for the email. The parameter is a String containing List of Bill JSON object values.
     *
     * @param billsDueString
     */
    private String constructBodyForBillsDue(String billsDueString) {
        List<String> billsDueUrlsList = Stream.of(billsDueString.split(",", -1)).collect(Collectors.toList());
        StringBuilder bodyStringBuilder = new StringBuilder();
        bodyStringBuilder.append("Hi, here is a list of all the Bills due: \n\n\n");
        for(String billDueUrl: billsDueUrlsList) {
            bodyStringBuilder.append(billDueUrl + "\n");
        }
        return bodyStringBuilder.toString();
    }
}
