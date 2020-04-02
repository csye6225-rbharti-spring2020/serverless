import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handler class to receive SNS Topic messages and send an email to the user.
 */
public class Handler implements RequestHandler<SNSEvent, Object> {

    private final String FROM_EMAIL = "no-reply@cloud.prod.rohanbharti.me";
    private final String SUBJECT_EMAIL = "Bills Due Update";

    private AmazonSES amazonSESClient;
    private AmazonDynamoDBClient amazonDynamoDBClient;

    public Object handleRequest(SNSEvent snsEvent, Context context) {

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        context.getLogger().log("Handler Invocation started: " + timeStamp);

        SNSEvent.SNS sns = snsEvent.getRecords().get(0).getSNS();

        if(sns.getMessage()!=null && (!sns.getMessage().isEmpty())) {
            String billsDueUrlsEmailListString = sns.getMessage();
            List<String> billsDueUrlsEmailList = Stream.of(billsDueUrlsEmailListString.split(",", -1)).collect(Collectors.toList());

            String userEmail = billsDueUrlsEmailList.get(0);
            context.getLogger().log("User's Email: " + userEmail);

            //Only send the email if the email ID entry doesn't exist in the DynamoDB table anymore
            amazonDynamoDBClient = new AmazonDynamoDBClient();
            boolean checkIfEmailEntryExists = amazonDynamoDBClient.checkIfEmailExists(userEmail, context);
            if (!checkIfEmailEntryExists) {
                amazonDynamoDBClient.addItem(userEmail, context);
                context.getLogger().log("Added the userEmail to the DynamoDB Table with a TTL of 60 minutes");
            } else {
                context.getLogger().log("User has already been sent an email in the past 60 minutes or the client wasn't configured properly");
                return null;
            }

            //removing the email from the list
            billsDueUrlsEmailList.remove(0);
            String billsDueListString = String.join(", ", billsDueUrlsEmailList);

            amazonSESClient = new AmazonSES(FROM_EMAIL, userEmail, SUBJECT_EMAIL, billsDueListString);
            boolean emailSent = amazonSESClient.sendEmail(context);

            if (emailSent) {
                context.getLogger().log("Email sent to " + userEmail + " successfully containing all the due Bills Urls");
            } else {
                context.getLogger().log("Email wasn't sent to the user successfully");
            }

            timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
            context.getLogger().log("Handler Invocation completed: " + timeStamp);
        }
        return null;
    }
}

