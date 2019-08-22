/**
 * XmapiImplServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.seeyon.apps.ext.xk263Email.axis.xmapi;

public class XmapiImplServiceLocator extends org.apache.axis.client.Service implements XmapiImplService {

    public XmapiImplServiceLocator() {
    }


    public XmapiImplServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public XmapiImplServiceLocator(String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for xmapi
    private String xmapi_address = "http://macom.263.net/axis/xmapi";

    public String getxmapiAddress() {
        return xmapi_address;
    }

    // The WSDD service name defaults to the port name.
    private String xmapiWSDDServiceName = "xmapi";

    public String getxmapiWSDDServiceName() {
        return xmapiWSDDServiceName;
    }

    public void setxmapiWSDDServiceName(String name) {
        xmapiWSDDServiceName = name;
    }

    public XmapiImpl getxmapi() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(xmapi_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getxmapi(endpoint);
    }

    public XmapiImpl getxmapi(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            XmapiSoapBindingStub _stub = new XmapiSoapBindingStub(portAddress, this);
            _stub.setPortName(getxmapiWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setxmapiEndpointAddress(String address) {
        xmapi_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (XmapiImpl.class.isAssignableFrom(serviceEndpointInterface)) {
                XmapiSoapBindingStub _stub = new XmapiSoapBindingStub(new java.net.URL(xmapi_address), this);
                _stub.setPortName(getxmapiWSDDServiceName());
                return _stub;
            }
        }
        catch (Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("xmapi".equals(inputPortName)) {
            return getxmapi();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://macom.263.net/axis/xmapi", "XmapiImplService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://macom.263.net/axis/xmapi", "xmapi"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(String portName, String address) throws javax.xml.rpc.ServiceException {
        
if ("xmapi".equals(portName)) {
            setxmapiEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
