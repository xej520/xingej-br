package com.bonc.broker.exception;

public class ExceptionMsg {
    public static String SERVICENAME_BADREQUEST = "serviceName parameter does not meet the requirements![A service name consisting of 6,16-bit lowercase letters and lowercase letters or numbers]";

    public static String INSTANCEID_BADREQUEST = "instance_id does not meet the requirements!";
    public static String INSTANCEID_CONFLICT = "instance_id already exists";
    public static String INSTANCEID_NOTFOUND = "instance_id not exists!";

    public static String UNPROCESSABLE_ENTITY = "accepts_incomplete: AsyncRequired";

    public static String SERVICEID_BADREQUEST = "service_id does not meet the requirements![8780d398-a010-4696-bb0d-78ea6511fa95,764ad650-e6cf-4855-9ab5-51820afa3515]";
    public static String SERVICEID_NOTFOUND = "service_id does not exists!";

    public static String PLANID_BADREQUEST = "plan_id does not meet the requirements![aee3dab8-8cf9-4d3a-a2fd-f8155e56750e," +
            "f0dfb49f-91cf-42af-871c-d06d8f7887dd," +
            "17f5165d-737e-4742-a6a2-f368f0e632d6," +
            "64002283-77a5-40f3-859d-d37a6d202bc3," +
            "e44b275f-fe4b-4721-aa18-009c1ce7353e]";
    public static String PLANID_NOTFOUND = "plan_id does not exists!";
    public static String PLANID_FOUND = "plan_id has exists!";

    public static String PARAMETERS_NOTFOUND = "parameters not found!";
    public static String CONFIGURATION_NOTFOUND = "configuration parameter not found!";

    public static String CPU_NOTFOUND_MYSQL = "cpu parameter not found!";
    public static String CPU_BADREQUEST_MYSQL = "cpu parameter does not meet the requirements![1-16]";
    public static String CPU_BADREQUEST_REDIS = "cpu parameter does not meet the requirements![1-2]";

    public static String MEMORY_NOTFOUND_MYSQL = "memory parameter not found!";
    public static String MEMORY_BADREQUEST_MYSQL = "memory parameter does not meet the requirements![1-1024]";
    public static String MEMORY_BADREQUEST_REDIS = "memory parameter does not meet the requirements![1-64]";

    public static String CAPACITY_NOTFOUND_MYSQL = "capacity parameter parameters not found!";
    public static String CAPACITY_BADREQUEST_MYSQL = "capacity parameter does not meet the requirements![1-2048]";
    public static String CAPACITY_BADREQUEST_REDIS = "capacity parameter does not meet the requirements![1-64]";

    public static String VERSION_NOTFOUND_MYSQL = "version parameter not found!";
    public static String VERSION_BADREQUEST_MYSQL = "version parameter does not meet the requirements![5.6,5.7,5.8]";
    public static String VERSION_NOTFOUND_REDIS = "version parameter not found!";
    public static String VERSION_BADREQUEST_REDIS = "version parameter does not meet the requirements![3.2.11]";

    public static String PASSWORD_BADREQUEST = "password  parameter  does not meet the requirements![6-16 bits combination of letters and numbers]";

    public static String REPLICAS_NOTFOUND_MYSQL = "replicas parameter not found!";
    public static String REPLICAS_BADREQUEST_MYSQL = "replicas parameter does not meet the requirements![2,3,4]";
    public static String REPLICAS_BADREQUEST_REDIS = "replicas parameter does not meet the requirements![最小值为2]";

    public static String BINDING_INSTANCE_FORBID = "This instance ID also has a binding object";
    public static String BINDING_INSTANCE_CONFLICT = "binding_id has already exists";
    public static String BINDING_BADREQUEST = "binding_id parameter does not meet the requirements!";
    public static String BINDINGID_NOTFOUND = "bindingId object not exists";

    public static String SENTINEL_NUM_REDIS = "sentinelNum parameter does not meet the requirements![3,5]";
    public static String SENTINEL_CPU_REDIS = "sentinelCpu parameter does not meet the requirements![0.5-1]";
    public static String SENTINEL_MEM_REDIS = "sentinelMemory parameter does not meet the requirements![128-256]";




}
