package pt.ulisboa.tecnico.cnv.server;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

/** Frontend for the WebServer to interact with the MSS. */
public class MSS {

  private static final String REQUESTS_COSTS_TABLE = "requests_costs";
  private final Table requestsCostsTable;

  public MSS() {
    AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
    this.requestsCostsTable =
        new DynamoDB(
                AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.US_EAST_1)
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .build())
            .getTable(REQUESTS_COSTS_TABLE);
  }

  /**
   * Stores in MSS the cost associated with the request with the given query.
   *
   * @param query Query of the incoming request.
   * @param cost Incoming request cost.
   */
  protected void addRequestCost(String query, double cost) {
    Item item = new Item().withPrimaryKey("RequestQuery", query).withNumber("Cost", cost);
    this.requestsCostsTable.putItem(item);
  }
}
