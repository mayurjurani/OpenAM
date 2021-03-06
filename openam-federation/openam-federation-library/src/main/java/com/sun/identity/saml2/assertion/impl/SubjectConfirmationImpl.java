/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006 Sun Microsystems Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: SubjectConfirmationImpl.java,v 1.2 2008/06/25 05:47:44 qcheng Exp $
 *
 */

/*
 * Portions Copyrighted 2013 ForgeRock AS
 */

package com.sun.identity.saml2.assertion.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.sun.identity.shared.xml.XMLUtils;
import com.sun.identity.saml2.assertion.SubjectConfirmation;
import com.sun.identity.saml2.assertion.SubjectConfirmationData;
import com.sun.identity.saml2.assertion.EncryptedID;
import com.sun.identity.saml2.assertion.NameID;
import com.sun.identity.saml2.assertion.BaseID;
import com.sun.identity.saml2.assertion.AssertionFactory;
import com.sun.identity.saml2.common.SAML2Constants;
import com.sun.identity.saml2.common.SAML2Exception;
import com.sun.identity.saml2.common.SAML2SDKUtils;

/**
 *  The <code>SubjectConfirmation</code> provides the means for a relying 
 *  party to verify the correspondence of the subject of the assertion
 *  with the party with whom the relying party is communicating.
 */
public class SubjectConfirmationImpl implements SubjectConfirmation {

    private SubjectConfirmationData subjectConfirmationData = null;
    private BaseID baseId = null;
    private NameID nameId = null;
    private EncryptedID encryptedId = null;
    private String method = null;
    private boolean isMutable = true;

    public static final String SUBJECT_CONFIRMATION_ELEMENT = 
                               "SubjectConfirmation";
    public static final String SUBJECT_CONFIRMATION_DATA_ELEMENT = 
                               "SubjectConfirmationData";
    public static final String BASE_ID_ELEMENT = "BaseID";
    public static final String NAME_ID_ELEMENT = "NameID";
    public static final String ENCRYPTED_ID_ELEMENT = "EncryptedID";
    public static final String METHOD_ATTR = "Method";

    /**
     * Default constructor
     */
    public SubjectConfirmationImpl() {
    }

    /**
     * This constructor is used to build <code>SubjectConfirmation</code>
     * object from a XML string.
     *
     * @param xml A <code>java.lang.String</code> representing
     *        a <code>SubjectConfirmation</code> object
     * @exception SAML2Exception if it could not process the XML string
     */
    public SubjectConfirmationImpl(String xml) throws SAML2Exception {
        Document document = XMLUtils.toDOMDocument(xml, SAML2SDKUtils.debug);
        if (document != null) {
            Element rootElement = document.getDocumentElement();
            processElement(rootElement);
            makeImmutable();
        } else {
            SAML2SDKUtils.debug.error(
                "SubjectConfirmationImpl.processElement(): invalid XML input");
            throw new SAML2Exception(SAML2SDKUtils.bundle.getString(
                "errorObtainingElement"));
        }
    }

    /**
     * This constructor is used to build <code>SubjectConfirmation</code>
     * object from a block of existing XML that has already been built 
     * into a DOM.
     *
     * @param element A <code>org.w3c.dom.Element</code> representing
     *        DOM tree for <code>SubjectConfirmation</code> object
     * @exception SAML2Exception if it could not process the Element
     */

    public SubjectConfirmationImpl(Element element) 
        throws SAML2Exception {
        processElement(element);
        makeImmutable();
    }

    private void processElement(Element element) throws SAML2Exception {
        if (element == null) {
            SAML2SDKUtils.debug.error("SubjectConfirmationImpl." 
                +"processElement(): invalid root element");
            throw new SAML2Exception(SAML2SDKUtils.bundle.getString(
                "invalid_element"));
        }
        String elemName = element.getLocalName(); 
        if (elemName == null) {
            SAML2SDKUtils.debug.error(
                "SubjectConfirmationImpl.processElement(): local name missing");
            throw new SAML2Exception(SAML2SDKUtils.bundle.getString(
                "missing_local_name"));
        }

        if (!elemName.equals(SUBJECT_CONFIRMATION_ELEMENT)) {
            SAML2SDKUtils.debug.error(
                "SubjectConfirmationImpl.processElement(): invalid local name " 
                + elemName);
            throw new SAML2Exception(SAML2SDKUtils.bundle.getString(
                "invalid_local_name"));
        }

        // starts processing attributes
        String attrValue = element.getAttribute(METHOD_ATTR);
        if ((attrValue == null) || (attrValue.length() == 0)) {
            SAML2SDKUtils.debug.error(
                "SubjectConfirmationImpl.processElement(): method missing");
            throw new SAML2Exception(SAML2SDKUtils.bundle.getString(
                "missing_confirmation_method"));
        } 
        method = attrValue;

        // starts processing subelements
        NodeList nodes = element.getChildNodes();
        int numOfNodes = nodes.getLength();
        if (numOfNodes < 1) {
            return;
        }
    
        int nextElem = 0;
        Node child = (Node)nodes.item(nextElem);
        while (child.getNodeType() != Node.ELEMENT_NODE) {
            if (++nextElem >= numOfNodes) {
                return;
            }
            child = (Node)nodes.item(nextElem);
        }

        String childName = child.getLocalName();
        if (childName != null) {
            if (childName.equals(SUBJECT_CONFIRMATION_DATA_ELEMENT)) {
                subjectConfirmationData = AssertionFactory.getInstance().
                    createSubjectConfirmationData((Element)child);
            } else if (childName.equals(BASE_ID_ELEMENT)) {
                baseId = AssertionFactory.getInstance().
                         createBaseID((Element)child);
            } else if (childName.equals(NAME_ID_ELEMENT)) {
                nameId = AssertionFactory.getInstance().
                         createNameID((Element)child);
            } else if (childName.equals(ENCRYPTED_ID_ELEMENT)) {
                encryptedId = AssertionFactory.getInstance().
                              createEncryptedID((Element)child);
            } else {
                SAML2SDKUtils.debug.error(
                    "SubjectConfirmationImpl.processElement(): "
                    + "unexpected subelement " + childName);
                throw new SAML2Exception(SAML2SDKUtils.bundle.getString(
                      "unexpected_subelement"));
            }
        }
    }

    /**
     *  Returns the encrypted ID
     *
     *  @return the encrypted ID
     */
    public EncryptedID getEncryptedID() {
        return encryptedId;
    }

    /**
     *  Sets the encrypted ID
     *
     *  @param value the encrypted ID
     *  @exception SAML2Exception if the object is immutable
     */
    public void setEncryptedID(EncryptedID value) 
        throws SAML2Exception{
        if (!isMutable) {
            throw new SAML2Exception(SAML2SDKUtils.bundle.getString(
                "objectImmutable"));
        }
        encryptedId = value;
    } 

    /**
     *  Returns the identifier in <code>NameID</code> format
     *
     *  @return the identifier in <code>NameID</code> format
     */
    public NameID getNameID() {
        return nameId;
    }

    /**
     *  Sets the identifier in <code>NameID</code> format
     *
     *  @param value the identifier in <code>NameID</code> format
     *  @exception SAML2Exception if the object is immutable
     */
    public void setNameID(NameID value) throws SAML2Exception {
        if (!isMutable) {
            throw new SAML2Exception(SAML2SDKUtils.bundle.getString(
                "objectImmutable"));
        }
        nameId = value;
    } 

    /** 
     * Returns the subject confirmation data
     *
     * @return the subject confirmation data
     */
    public SubjectConfirmationData getSubjectConfirmationData() {
        return subjectConfirmationData;
    }

    /** 
     * Sets the subject confirmation data
     *
     * @param value the subject confirmation data
     * @exception SAML2Exception if the object is immutable
     */
    public void setSubjectConfirmationData(SubjectConfirmationData value) 
        throws SAML2Exception {
        if (!isMutable) {
            throw new SAML2Exception(SAML2SDKUtils.bundle.getString(
                "objectImmutable"));
        }
        subjectConfirmationData = value;
    } 
    

    /**
     *  Returns the base ID
     *
     *  @return the base ID
     */
    public BaseID getBaseID() {
        return baseId;
    }

    /**
     *  Sets the base ID
     *
     *  @param value the base ID
     *  @exception SAML2Exception if the object is immutable
     */
    public void setBaseID(BaseID value) throws SAML2Exception {
        if (!isMutable) {
            throw new SAML2Exception(SAML2SDKUtils.bundle.getString(
                "objectImmutable"));
        }
        baseId = value;
    } 

    /**
     * Returns the confirmation method 
     *
     * @return the confirmation method 
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the confirmation method 
     *
     * @param value the confirmation method 
     * @exception SAML2Exception if the object is immutable
     */
    public void setMethod(String value) throws SAML2Exception {
        if (!isMutable) {
            throw new SAML2Exception(SAML2SDKUtils.bundle.getString(
                "objectImmutable"));
        }
        method = value;
    }

   /**
    * Returns a String representation
    * @param includeNSPrefix Determines whether or not the namespace 
    *        qualifier is prepended to the Element when converted
    * @param declareNS Determines whether or not the namespace is 
    *        declared within the Element.
    * @return A String representation
    * @exception SAML2Exception if something is wrong during conversion
    */
    public String toXMLString(boolean includeNSPrefix, boolean declareNS) throws SAML2Exception {
        StringBuilder sb = new StringBuilder(2000);
        String NS = "";
        String appendNS = "";
        if (declareNS) {
            NS = SAML2Constants.ASSERTION_DECLARE_STR;
        }
        if (includeNSPrefix) {
            appendNS = SAML2Constants.ASSERTION_PREFIX;
        }
        sb.append("<").append(appendNS).append(SUBJECT_CONFIRMATION_ELEMENT).append(NS);
        if ((method == null) || (method.trim().length() == 0)) {
            SAML2SDKUtils.debug.error("SubjectConfirmationImpl.toXMLString(): method missing");
            throw new SAML2Exception(SAML2SDKUtils.bundle.getString("missing_confirmation_method"));
        } 
        sb.append(" ").append(METHOD_ATTR).append("=\"").append(method).append("\"").append(">\n");
        if ((baseId != null) || (nameId != null) || (encryptedId != null)) {
            if ((baseId != null) && (nameId == null) && (encryptedId == null)) {
                sb.append(baseId.toXMLString(includeNSPrefix, false));
            } else if ((nameId != null) && (baseId == null) && (encryptedId == null)) {
                sb.append(nameId.toXMLString(includeNSPrefix, false));
            } else if ((encryptedId != null) && (baseId == null) && (nameId == null)) {
                sb.append(encryptedId.toXMLString(includeNSPrefix, false));
            } else {
                SAML2SDKUtils.debug.error("SubjectConfirmationImpl.toXMLString(): more than one types of id specified");
                throw new SAML2Exception(SAML2SDKUtils.bundle.getString("too_many_ids_specified"));
            }
        }
        if (subjectConfirmationData != null) {
            sb.append(subjectConfirmationData.toXMLString(includeNSPrefix, false));
        }
        sb.append("</").append(appendNS).append(SUBJECT_CONFIRMATION_ELEMENT).append(">\n");
        return sb.toString();
    }

   /**
    * Returns a String representation
    *
    * @return A String representation
    * @exception SAML2Exception if something is wrong during conversion
    */
    public String toXMLString() throws SAML2Exception {
        return this.toXMLString(true, false);
    }

   /**
    * Makes the object immutable
    */
    public void makeImmutable() {
        if (isMutable) {
            if (subjectConfirmationData != null) {
                subjectConfirmationData.makeImmutable();
            }
            if (baseId != null) {
                baseId.makeImmutable();
            }
            if (nameId != null) {
                nameId.makeImmutable();
            }
            isMutable = false;
        }
    }

   /**
    * Returns true if the object is mutable
    *
    * @return true if the object is mutable
    */
    public boolean isMutable() {
        return isMutable;
    }
}
