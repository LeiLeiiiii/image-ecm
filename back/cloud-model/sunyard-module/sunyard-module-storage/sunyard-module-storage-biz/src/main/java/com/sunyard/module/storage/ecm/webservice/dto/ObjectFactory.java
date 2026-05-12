
package com.sunyard.module.storage.ecm.webservice.dto;

import com.sunyard.module.storage.dto.webservice.BusinessRespBean;
import com.sunyard.module.storage.dto.webservice.ResponseBean;
import com.sunyard.module.storage.dto.webservice.SunBean;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the com
 * package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content.
 * The Java representation of XML content can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory methods for each of these are provided in
 * this class.
 *
 * @author 刘港
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName DEL_BUSINESS_RESPONSE_QNAME =
        new QName("http://batch.mgmt.webservice.ecm.insurance.sunyard.com/", "delBusinessResponse");
    private final static QName MOD_BUSINESS_RESPONSE_QNAME =
        new QName("http://batch.mgmt.webservice.ecm.insurance.sunyard.com/", "modBusinessResponse");
    private final static QName ADD_BUSINESS_RESPONSE_QNAME =
        new QName("http://batch.mgmt.webservice.ecm.insurance.sunyard.com/", "addBusinessResponse");
    private final static QName DEL_BUSINESS_QNAME =
        new QName("http://batch.mgmt.webservice.ecm.insurance.sunyard.com/", "delBusiness");
    private final static QName MOD_BUSINESS_QNAME =
        new QName("http://batch.mgmt.webservice.ecm.insurance.sunyard.com/", "modBusiness");
    private final static QName ADD_BUSINESS_QNAME =
        new QName("http://batch.mgmt.webservice.ecm.insurance.sunyard.com/", "addBusiness");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com
     */
    public ObjectFactory() {}

    /**
     * Create an instance of {@link ModBusiness }
     */
    public ModBusiness createModBusiness() {
        return new ModBusiness();
    }

    /**
     * Create an instance of {@link AddBusiness }
     */
    public AddBusiness createAddBusiness() {
        return new AddBusiness();
    }

    /**
     * Create an instance of {@link ModBusinessResponse }
     */
    public ModBusinessResponse createModBusinessResponse() {
        return new ModBusinessResponse();
    }

    /**
     * Create an instance of {@link AddBusinessResponse }
     */
    public AddBusinessResponse createAddBusinessResponse() {
        return new AddBusinessResponse();
    }

    /**
     * Create an instance of {@link DelBusiness }
     */
    public DelBusiness createDelBusiness() {
        return new DelBusiness();
    }

    /**
     * Create an instance of {@link DelBusinessResponse }
     */
    public DelBusinessResponse createDelBusinessResponse() {
        return new DelBusinessResponse();
    }

    /**
     * Create an instance of {@link ResponseBean }
     */
    public ResponseBean createResponseBean() {
        return new ResponseBean();
    }

    /**
     * Create an instance of {@link BusinessRespBean }
     */
    public BusinessRespBean createBusinessRespBean() {
        return new BusinessRespBean();
    }

    /**
     * Create an instance of {@link SunBean }
     */
    public SunBean createSunBean() {
        return new SunBean();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelBusinessResponse }{@code >}}
     */
    @XmlElementDecl(namespace = "http://batch.mgmt.webservice.ecm.insurance.sunyard.com/", name = "delBusinessResponse")
    public JAXBElement<DelBusinessResponse> createDelBusinessResponse(DelBusinessResponse value) {
        return new JAXBElement<DelBusinessResponse>(DEL_BUSINESS_RESPONSE_QNAME, DelBusinessResponse.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ModBusinessResponse }{@code >}}
     */
    @XmlElementDecl(namespace = "http://batch.mgmt.webservice.ecm.insurance.sunyard.com/", name = "modBusinessResponse")
    public JAXBElement<ModBusinessResponse> createModBusinessResponse(ModBusinessResponse value) {
        return new JAXBElement<ModBusinessResponse>(MOD_BUSINESS_RESPONSE_QNAME, ModBusinessResponse.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddBusinessResponse }{@code >}}
     */
    @XmlElementDecl(namespace = "http://batch.mgmt.webservice.ecm.insurance.sunyard.com/", name = "addBusinessResponse")
    public JAXBElement<AddBusinessResponse> createAddBusinessResponse(AddBusinessResponse value) {
        return new JAXBElement<AddBusinessResponse>(ADD_BUSINESS_RESPONSE_QNAME, AddBusinessResponse.class, null,
            value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelBusiness }{@code >}}
     */
    @XmlElementDecl(namespace = "http://batch.mgmt.webservice.ecm.insurance.sunyard.com/", name = "delBusiness")
    public JAXBElement<DelBusiness> createDelBusiness(DelBusiness value) {
        return new JAXBElement<DelBusiness>(DEL_BUSINESS_QNAME, DelBusiness.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ModBusiness }{@code >}}
     */
    @XmlElementDecl(namespace = "http://batch.mgmt.webservice.ecm.insurance.sunyard.com/", name = "modBusiness")
    public JAXBElement<ModBusiness> createModBusiness(ModBusiness value) {
        return new JAXBElement<ModBusiness>(MOD_BUSINESS_QNAME, ModBusiness.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddBusiness }{@code >}}
     */
    @XmlElementDecl(namespace = "http://batch.mgmt.webservice.ecm.insurance.sunyard.com/", name = "addBusiness")
    public JAXBElement<AddBusiness> createAddBusiness(AddBusiness value) {
        return new JAXBElement<AddBusiness>(ADD_BUSINESS_QNAME, AddBusiness.class, null, value);
    }

}
