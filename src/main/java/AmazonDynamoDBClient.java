import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.*;
import java.util.HashMap;

/**
 * Amazon DynamoDB Client
 *
 * @author rohan_bharti
 */
public class AmazonDynamoDBClient {

    private DynamoDbClient amazonDynamoDB;
    private final String TABLE_NAME = "MyDynamoDBTable";
    private final String ATTRIBUTE_EMAIL_ID_NAME = "EMAIL_ID";
    private final String ATTRIBUTE_TTL_NAME = "EMAIL_TTL";

    /**
     * Constructor
     */
    public AmazonDynamoDBClient() {
        Region region = Region.US_EAST_1;
        this.amazonDynamoDB = DynamoDbClient.builder().region(region).build();
    }

    /**
     * Adds the email ID and its respective TTL field to the DynamoDb table
     *
     * @param userEmail
     * @return
     */
    public boolean addItem(String userEmail) {
        HashMap<String,AttributeValue> itemValues = new HashMap<String,AttributeValue>();
        itemValues.put(ATTRIBUTE_EMAIL_ID_NAME, AttributeValue.builder().s(userEmail).build());

        Calendar cal = Calendar.getInstance(); //current date and time
        cal.add(Calendar.MINUTE, 60); //add minutes
        double ttl =  (cal.getTimeInMillis() / 1000L);
        itemValues.put(ATTRIBUTE_TTL_NAME, AttributeValue.builder().n(Double.toString(ttl)).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(itemValues)
                .build();
        try {
            this.amazonDynamoDB.putItem(request);
            return true;

        } catch (ResourceNotFoundException e) {
            return false;
        } catch (DynamoDbException e) {
            return false;
        }
    }

    /**
     * Checks if the email id field exists already in the DynamoDB table
     *
     * @param userEmail
     * @return
     */
    public boolean checkIfEmailExists(String userEmail) {
        HashMap<String,AttributeValue> keyToGet = new HashMap<String,AttributeValue>();
        keyToGet.put(ATTRIBUTE_EMAIL_ID_NAME, AttributeValue.builder().s(userEmail).build());

        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(TABLE_NAME)
                .build();

        try {
            Map<String,AttributeValue> returnedItem = amazonDynamoDB.getItem(request).item();

            if (returnedItem != null) {
                Set<String> keys = returnedItem.keySet();
                for (String key1 : keys) {
                    if(returnedItem.get(key1).toString().equals(userEmail))
                        return true;
                }
            } else {
               return false;
            }
        } catch (DynamoDbException e) {
            return true;
        }
        return true;
    }
}