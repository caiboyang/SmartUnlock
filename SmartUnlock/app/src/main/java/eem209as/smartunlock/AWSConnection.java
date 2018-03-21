package eem209as.smartunlock;

import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.machinelearning.*;
import com.amazonaws.services.machinelearning.model.EntityStatus;
import com.amazonaws.services.machinelearning.model.GetMLModelRequest;
import com.amazonaws.services.machinelearning.model.GetMLModelResult;
import com.amazonaws.services.machinelearning.model.PredictRequest;
import com.amazonaws.services.machinelearning.model.PredictResult;
import com.amazonaws.services.machinelearning.model.RealtimeEndpointStatus;

import java.util.HashMap;

/**
 * Created by boyang on 3/19/18.
 */

public class AWSConnection {

    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS permissions.
    private static final String COGNITO_POOL_ID = "us-east-1:9fd8c353-ed7f-4a3f-b9a3-c0e818d1d52d";
    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_EAST_1;
    // Use a created model that has a created real-time endpoint
    public static final String mlModelId = "ml-azQvUDQ68KH";


    private MainActivity activity;
    private AmazonMachineLearningClient client;
    private String endpoint;
    private PredictRequest predictRequest;

    GetMLModelRequest getMLModelRequest;

    public AWSConnection(MainActivity activity) {
        this.activity = activity;
    }

    public void initialize() {

        // Initialize the AWS Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                activity.getApplicationContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        client = new AmazonMachineLearningClient(credentialsProvider);
        Log.i("AWSConnection", "Class Initialized!!!");
        connect();
    }

    public void connect(){

        // Call GetMLModel to get the realtime endpoint URL
        getMLModelRequest = new GetMLModelRequest();
        getMLModelRequest.setMLModelId(mlModelId);
        // The following block uses a Cognito credentials provider for authentication with AWS IoT.
        new Thread(new Runnable() {
            @Override
            public void run() {
                GetMLModelResult mlModelResult = client.getMLModel(getMLModelRequest);

                // Validate that the ML model is completed
                if (!mlModelResult.getStatus().equals(EntityStatus.COMPLETED.toString())) {
                    Log.e("AWSConnection", "ML Model is not completed: + mlModelResult.getStatus()");
                    return;
                }

// Validate that the realtime endpoint is ready
                if (!mlModelResult.getEndpointInfo().getEndpointStatus().equals(RealtimeEndpointStatus.READY.toString())) {
                    Log.e("AWSConnection", "Realtime endpoint is not ready: " + mlModelResult.getEndpointInfo().getEndpointStatus());
                    return;
                }
                endpoint = mlModelResult.getEndpointInfo().getEndpointUrl();
                predictRequest = new PredictRequest();
                predictRequest.setMLModelId(mlModelId);
                Log.i("AWSConnection", "Predict is Ready!");
                activity.isAWSReady = true;
            }
        }).start();



    }

    public void callPredict(DataClass myData) {


        // Create a Predict request with your ML model ID and the appropriate Record mapping
        HashMap<String, String> record = new HashMap<>();
        switch(myData.dayStamp){
            case "Monday":
            case "Tuesday":
            case "Wednesday":
            case "Thursday":
            case "Friday":
                record.put("localDay", "Weekday");
                break;
            case "Saturday":
            case "Sunday":
                record.put("localDay", "Weekend");
                break;
        }
        record.put("g", Double.toString(myData.g));
        record.put("localTime", myData.timeStamp.substring(0,2));
        record.put("latitude", Double.toString(myData.lat));
        record.put("longitude", Double.toString(myData.lng));
        record.put("accuracy", Double.toString(myData.acu));
        record.put("altitude", Double.toString(myData.alt));
        record.put("speed", Double.toString(myData.speed));
        record.put("wifi mac", myData.wifiInfo.get("BSSID"));
        record.put("wifi ssid", myData.wifiInfo.get("SSID"));
        record.put("wifi signal level", myData.wifiInfo.get("RSSI"));
        record.put("provider", myData.provider);

        Log.i("callPredict", "localTime is set to: " + myData.timeStamp.substring(0,2));

        predictRequest.setRecord(record);
        predictRequest.setPredictEndpoint(endpoint);

// Call Predict and print out your prediction
        new Thread(new Runnable() {
            @Override
            public void run() {
                PredictResult predictResult = client.predict(predictRequest);
                String predictResultLabel = predictResult.getPrediction().getPredictedLabel();
                Log.i("PredictionResult", predictResultLabel);
                myData.result = Integer.parseInt(predictResultLabel);
                activity.awsCallBack();
//                Toast.makeText(activity.getApplicationContext(), predictResultLabel, Toast.LENGTH_LONG).show();
            }
        }).start();


// Do something with the prediction
// ...
    }
}
