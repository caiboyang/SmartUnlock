package eem209as.smartunlock;

import android.util.Log;

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

    // Customer specific endpoint
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a11nf0pk1jaec3.iot.us-west-2.amazonaws.com";
    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS permissions.
    private static final String COGNITO_POOL_ID = "us-east-1:9fd8c353-ed7f-4a3f-b9a3-c0e818d1d52d";
    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.US_EAST_1;


    CognitoCachingCredentialsProvider credentialsProvider;
    MainActivity activity;
    AmazonMachineLearningClient client;

    public AWSConnection(MainActivity activity) {
        this.activity = activity;
    }

    public void initialize() {

        // Initialize the AWS Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                activity.getApplicationContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        client = new AmazonMachineLearningClient(credentialsProvider);

    }


    public void callPredict() {
        // Use a created model that has a created real-time endpoint
        String mlModelId = "ml-azQvUDQ68KH";

// Call GetMLModel to get the realtime endpoint URL
        GetMLModelRequest getMLModelRequest = new GetMLModelRequest();
        getMLModelRequest.setMLModelId(mlModelId);
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

        Log.i("AWSConnection", "Predict is Ready!");

//        // Create a Predict request with your ML model ID and the appropriate Record mapping
//        PredictRequest predictRequest = new PredictRequest();
//        predictRequest.setMLModelId(mlModelId);
//
//        HashMap<String, String> record = new HashMap<>();
//        record.put("example attribute", "example value");
//
//        predictRequest.setRecord(record);
//        predictRequest.setPredictEndpoint(mlModelResult.getEndpointInfo().getEndpointUrl());
//
//// Call Predict and print out your prediction
//        PredictResult predictResult = client.predict(predictRequest);
//        System.out.println(predictResult.getPrediction());

// Do something with the prediction
// ...
    }
}
