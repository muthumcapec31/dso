/*
 * All content copyright (c) 2003-2007 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.dso.editors.xmlbeans;

import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaStringEnumEntry;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlObject;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

final class XmlConfigPersistenceManager {

  private static final Class[]  NO_PARAMS        = new Class[0];
  private static final Object[] NO_ARGS          = new Object[0];
  private static final String   SET_STRING_VALUE = "setStringValue";
  private static final String   XSET             = "xset";
  private static final String   SET              = "set";
  private static final String   XGET             = "xget";
  private static final String   GET              = "get";
  private static final String   TYPE             = "type";

  static String readElement(XmlObject parent, String elementName) {
    try {
      Class parentType = parent.schemaType().getJavaClass();
      XmlAnySimpleType element = ((XmlAnySimpleType) getXmlObject(parent, parentType, convertElementName(elementName)));
      if (element != null) return element.getStringValue();
      return getSchemaProperty(parentType, elementName).getDefaultText();
    } catch (Exception e) {
      e.printStackTrace(); // XXX
      return "";
    }
  }

  static void writeElement(XmlObject parent, String elementName, String value) {
    try {
      Class parentType = parent.schemaType().getJavaClass();
      XmlObject xmlObject = ensureElementHierarchy(parent, parentType, elementName, convertElementName(elementName));
      Class[] params = new Class[] { String.class };
      Object[] args = new Object[] { value };
      String methodName = SET_STRING_VALUE;
      Class objClass = xmlObject.getClass();
      Method method = objClass.getMethod(methodName, params);
      method.invoke(xmlObject, args);
    } catch (Exception e) {
      e.printStackTrace(); // XXX
    }
  }

  static String[] getListDefaults(Class parentType, String elementName) {
    try {
      SchemaStringEnumEntry[] enumEntries = getPropertySchemaType(parentType, elementName).getStringEnumEntries();
      String[] values = new String[enumEntries.length];
      for (int i = 0; i < enumEntries.length; i++) {
        values[i] = enumEntries[i].getString();
      }
      return values;
    } catch (Exception e) {
      e.printStackTrace(); // XXX
      return new String[0];
    }
  }

  static XmlObject ensureXml(XmlObject parent, Class parentType, String elementName) {
    try {
      return ensureElementHierarchy(parent, parentType, elementName, convertElementName(elementName));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  static private XmlObject ensureElementHierarchy(XmlObject parent, Class parentType, String elementName,
                                                  String fieldName) throws Exception {
    XmlObject xmlObject = getXmlObject(parent, parentType, fieldName);
    if (xmlObject != null) return xmlObject;
    Class[] params = new Class[] { getPropertySchemaType(parentType, elementName).getJavaClass() };
    Object[] args = new Object[] { getSchemaProperty(parentType, elementName).getDefaultValue() };
    Method method = null;
    try {
      method = parentType.getMethod(XSET + fieldName, params);
    } catch (NoSuchMethodException e) {
      method = parentType.getMethod(SET + fieldName, params);
    }
    method.invoke(parent, args);
    return getXmlObject(parent, parentType, fieldName);
  }

  static private String convertElementName(String s) {
    StringBuffer sb = new StringBuffer();
    StringTokenizer st = new StringTokenizer(s, "-");
    String tok;
    while (st.hasMoreTokens()) {
      tok = st.nextToken();
      sb.append(Character.toUpperCase(tok.charAt(0)));
      sb.append(tok.substring(1));
    }
    return sb.toString();
  }

  static private XmlObject getXmlObject(XmlObject parent, Class parentType, String fieldName) throws Exception {
    return (XmlObject) invokePrefixedParentNoParams(XGET, parent, parentType, fieldName);
  }

  static private Object invokePrefixedParentNoParams(String prefix, XmlObject parent, Class parentType, String fieldName)
      throws Exception {
    Method method = null;
    try {
      method = parentType.getMethod(prefix + fieldName, NO_PARAMS);
    } catch (NoSuchMethodException e) {
      method = parentType.getMethod(GET + fieldName, NO_PARAMS);
    }
    return (method != null) ? method.invoke(parent, NO_ARGS) : null;
  }

  static private SchemaType getParentSchemaType(Class parentType) throws Exception {
    return (SchemaType) parentType.getField(TYPE).get(null);
  }

  static private SchemaProperty getSchemaProperty(Class parentType, String elementName) throws Exception {
    QName qname = QName.valueOf(elementName);
    SchemaType type = getParentSchemaType(parentType);
    SchemaProperty property = type.getElementProperty(qname);
    if (property == null) property = type.getAttributeProperty(qname);
    return property;
  }

  static private SchemaType getPropertySchemaType(Class parentType, String elementName) throws Exception {
    return getSchemaProperty(parentType, elementName).getType();
  }
}
