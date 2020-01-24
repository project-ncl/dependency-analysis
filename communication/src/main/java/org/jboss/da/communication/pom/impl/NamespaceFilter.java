package org.jboss.da.communication.pom.impl;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/*
 * Original code from Stack Overflow (http://stackoverflow.com/a/2148541) user
 * Kristofer (http://stackoverflow.com/users/259485/kristofer) under CC BY-SA 3.0
 * Edited by Honza Brazdil <jbrazdil@redhat.com>
 */
public class NamespaceFilter extends XMLFilterImpl {

    private final String usedNamespaceUri;

    private boolean addedNamespace = false;

    public NamespaceFilter(String namespaceUri) {
        super();
        this.usedNamespaceUri = namespaceUri;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        startControlledPrefixMapping();
    }

    @Override
    public void startElement(String uri, String arg1, String arg2, Attributes arg3) throws SAXException {
        super.startElement(this.usedNamespaceUri, arg1, arg2, arg3);
    }

    @Override
    public void endElement(String arg0, String arg1, String arg2) throws SAXException {
        super.endElement(this.usedNamespaceUri, arg1, arg2);
    }

    @Override
    public void startPrefixMapping(String prefix, String url) throws SAXException {
        this.startControlledPrefixMapping();
    }

    private void startControlledPrefixMapping() throws SAXException {
        if (!this.addedNamespace) {
            super.startPrefixMapping("", this.usedNamespaceUri);
            this.addedNamespace = true;
        }
    }

}
