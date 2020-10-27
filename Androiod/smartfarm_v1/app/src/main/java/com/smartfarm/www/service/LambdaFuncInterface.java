package com.smartfarm.www.service;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;
import com.smartfarm.www.retailResponse;

public interface LambdaFuncInterface {

    /**
     * Invoke the Lambda function "AndroidBackendLambdaFunction".
     * The function name is the method name.
     *
     * @LambdaFunction(functionName = "CognitoAuthTest")
     */
    @LambdaFunction
    ResponseClass fireDetection(RequestClass request);

    @LambdaFunction
    DiseaseResponseclass crop_classification(RequestClass request);

    @LambdaFunction
    DiseaseResponseclass crop_classification_v2(RequestClass request);

    @LambdaFunction
    ResponseClass objectDetection(RequestClass request);

    @LambdaFunction
    retailResponse retailPrediction(RequestClass request);


}
