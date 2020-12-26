package uk.ac.ox.ctsu.arts.fhirservice.helpers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IRestfulClientFactory;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IParam;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;

import java.io.IOException;

public class FHIRClientHelper
{
    private FhirContext ctx = FhirContext.forR4();
    private volatile Object creatingClient = new Object();
    private volatile Boolean creatingClientFlag = false;
    private IGenericClient client;
    private String fhirURI;
    private String accessToken;

    public FHIRClientHelper(IGenericClient client)
    {
        this.client=client;
    }

    public String getAccessToken()
    {
        return accessToken;
    }

    public String getFhirURI()
    {
        return fhirURI;
    }

    public FHIRClientHelper(String fhirURI, String accessToken) throws Exception
    {
        this.fhirURI=fhirURI;
        this.accessToken=accessToken;
        this.client=this.createClient(fhirURI, accessToken);
    }

    public FHIRClientHelper(String fhirURI) throws Exception
    {
        this.fhirURI=fhirURI;
        this.client=this.createClient(fhirURI);
    }

    public MethodOutcome createResource(Resource resource)
    {
        return client.create().resource(resource).prettyPrint().encodedJson().execute();
    }

    public MethodOutcome updateResource(Resource resource)
    {
        return client.update().resource(resource).prettyPrint().encodedJson().execute();
    }

    public String searchResource(String resourceName, ICriterion<? extends IParam> searchCriteria)
    {
        return  show(client.search().forResource(resourceName).where(searchCriteria)
                .returnBundle(Bundle.class).execute());
    }
    public IBaseOperationOutcome deleteResource(Resource resource)
    {
        return (IBaseOperationOutcome) client.delete().resource(resource).prettyPrint().encodedJson().execute();
    }

    public IGenericClient getClient()
    {
        return client;
    }

    public void checkCapabilities() throws Exception
    {
        try
        {
            client.capabilities();
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    private synchronized IGenericClient createClient(String fhirURI) throws Exception, IOException, InterruptedException
    {
        if (!creatingClientFlag)
        {
            synchronized (creatingClient)
            {
                creatingClientFlag = true;
                client = ctx.newRestfulGenericClient(fhirURI);
                ctx.getRestfulClientFactory().setSocketTimeout(100000);
                creatingClientFlag = false;
                creatingClient.notifyAll();
                return client;
            }
        }
        else
        {
            creatingClient.wait();
        }
        return null;
    }

    private synchronized IGenericClient createClient(String fhirURI,String accessToken) throws Exception, IOException, InterruptedException
    {
        if (!creatingClientFlag)
        {
            synchronized (creatingClient)
            {
                creatingClientFlag = true;
                FhirContext ctx = FhirContext.forDstu3();
                IRestfulClientFactory clientFactory = ctx.getRestfulClientFactory();
                ctx.getRestfulClientFactory().setSocketTimeout(100);
                client = clientFactory.newGenericClient(fhirURI);
                BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(accessToken);
                client.registerInterceptor(authInterceptor);
                creatingClientFlag = false;
                creatingClient.notifyAll();
                return client;
            }
        }
        else
        {
            creatingClient.wait();
        }
        return null;
    }

    public String show(Bundle bundle) {
        return ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    public Organization findOrganizationByID(String id) {
        return client.read().resource(Organization.class).withId(id).execute();
    }

}
