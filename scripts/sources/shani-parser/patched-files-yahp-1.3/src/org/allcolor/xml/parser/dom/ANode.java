/*
 * Copyright (C) 2005 by Quentin Anciaux
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Library General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *	@author Quentin Anciaux
 */
package org.allcolor.xml.parser.dom;

import org.allcolor.dtd.parser.CDocType;
import org.allcolor.dtd.parser.CEntity;
import org.allcolor.dtd.parser.CNotation;

import org.allcolor.xml.parser.CStringBuilder;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DOMLocator;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Notation;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

import java.io.Serializable;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * DOCUMENT ME!
 * 
 * @author Quentin Anciaux
 */
public abstract class ANode implements Node, INode, Cloneable {
	private static final CNamespace xmlnsdef = new CNamespace("xmlns",
			"http://www.w3.org/2000/xmlns/");
	private static int pos = 0;
	/** DOCUMENT ME! */
	public final static long serialVersionUID = -6807921832713920677L;

	public int rpos = pos++;

	/** DOCUMENT ME! */
	public ADocument ownerDocument;

	/** DOCUMENT ME! */
	public CElement parentNode;

	/** DOCUMENT ME! */
	public CNodeList listChild = null;

	/** DOCUMENT ME! */
	public CNamedNodeMap listAttributes = null;

	/** DOCUMENT ME! */
	public String localName;

	/** DOCUMENT ME! */
	public String name;

	/** DOCUMENT ME! */
	public String nameSpace = null;

	/** DOCUMENT ME! */
	public String prefix;

	/** DOCUMENT ME! */
	protected Map userDataMap = null;

	public final void notifyNSChange(String prefix) {
		// this.nsList = null;
		if (ownerDocument != null)
			ownerDocument.hasNS = true;
		if (prefix == null || prefix.equals(this.prefix)
				|| (this.prefix == null && "xmlns".equals(prefix))) {
			nameSpace = null;
		}
		if (this.listChild != null) {
			for (int i = 0; i < listChild.getLength(); i++) {
				ANode node = (ANode) listChild.item(i);
				node.notifyNSChange(prefix);
			}
		}
	}

	public void resetOwner(ADocument doc) {
		if (getNodeType() != DOCUMENT_NODE)
			setOwnerDocument(doc);
		NodeList nl = getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			ANode n = (ANode) nl.item(i);
			n.resetOwner(doc);
		}
		NamedNodeMap nnm = getAttributes();
		if (nnm != null) {
			for (int i = 0; i < nnm.getLength(); i++) {
				ANode n = (ANode) nnm.item(i);
				n.resetOwner(doc);
			}
		}
	}

	public Object clone() throws CloneNotSupportedException {
		ANode node = (ANode) super.clone();
		if (node instanceof ADocument) {
			((ADocument) node).setDocumentType(null);
		}
		node.ignoreAll = true;
		node.rpos = pos++;
		node.parentNode = null;
		node.ownerDocument = null;
		node.userDataMap = null;
		node.listChild = null;
		// node.nsList = null;
		node.listAttributes = new CNamedNodeMap(node);
		NodeList nl = listChild;
		if (nl != null) {
			for (int i = 0; i < nl.getLength(); i++) {
				ANode n = (ANode) nl.item(i);
				n = (ANode) n.clone();
				n.ignoreAll = true;
				n.rpos = pos++;
				n.parentNode = null;
				n.ownerDocument = null;
				n.userDataMap = null;
				node.appendChild(n);
				n.notifyNSChange(null);
				n.ignoreAll = false;
			}
		}
		NamedNodeMap nnm = listAttributes;
		if (nnm != null) {
			for (int i = 0; i < nnm.getLength(); i++) {
				ANode n = (ANode) nnm.item(i);
				n = (ANode) n.clone();
				n.ignoreAll = true;
				n.rpos = pos++;
				n.parentNode = null;
				n.userDataMap = null;
				((CElement) node).setAttributeNodeNS((Attr) n);
				n.ownerDocument = null;
				n.notifyNSChange(null);
				n.ignoreAll = false;
			}
		}
		node.ignoreAll = false;
		node.notifyNSChange(null);
		return node;
	}

	public ANode(final ADocument ownerDocument) {
		this.ownerDocument = ownerDocument;
	}

	/**
	 * Creates a new ANode object.
	 * 
	 * @param name
	 *            DOCUMENT ME!
	 * @param ownerDocument
	 *            DOCUMENT ME!
	 * 
	 * @throws NullPointerException
	 *             DOCUMENT ME!
	 */
	public ANode(final String name, final ADocument ownerDocument) {
		this.ownerDocument = ownerDocument;

		this.name = name.intern();

		int iIndex = name.indexOf(':', 1);

		if (iIndex != -1) {
			prefix = name.substring(0, iIndex).intern();
			localName = name.substring(iIndex + 1).intern();
		} // end if
		else {
			prefix = null;
			localName = this.name;
		} // end else
	} // end ANode()

	public ANode(final String name, final ADocument ownerDocument,
			final int indexSep) {
		this.ownerDocument = ownerDocument;

		this.name = name.intern();

		if (indexSep != -1) {
			prefix = name.substring(0, indexSep).intern();
			localName = name.substring(indexSep + 1).intern();
		} // end if
		else {
			prefix = null;
			localName = this.name;
		} // end else
	} // end ANode()

	public void setOwnerDocument(ADocument doc) {
		this.ownerDocument = doc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getAttributes()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public NamedNodeMap getAttributes() {
		if (listAttributes == null)
			listAttributes = new CNamedNodeMap(this);
		return listAttributes;
	} // end getAttributes()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getBaseURI()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getBaseURI() {
		if (getNodeType() == Node.ELEMENT_NODE) {
			Attr attr = ((CElement) this).getAttributeNode("xml:base");
			if (attr != null) {
				return attr.getValue();
			}
			try {
				isReadOnly();
			} catch (DOMException ignore) {
				ANode parent = parentNode;
				while (parent != null) {
					if (parent.getNodeType() == Node.ENTITY_NODE) {
						Entity ent = (Entity) parent;
						return ent.getSystemId();
					} else if (parent.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
						Entity ent = (Entity) ownerDocument.getDoctype()
								.getEntities().getNamedItem(
										parent.getNodeName());
						return ent.getSystemId();
					}
					parent = parent.parentNode;
				}
			}
		}
		ANode parent = parentNode;
		while (parent != null) {
			Attr attr = ((CElement) parent).getAttributeNode("xml:base");
			if (attr != null) {
				return attr.getValue();
			}
			parent = parent.parentNode;
		}
		if (ownerDocument != null) {
			return ownerDocument.getDocumentURI();
		} else if (getNodeType() == Node.DOCUMENT_NODE) {
			return ((ADocument) this).getDocumentURI();
		}

		return null;
	} // end getBaseURI()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getChildNodes()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public NodeList getChildNodes() {
		if (listChild == null) {
			listChild = new CNodeList(false);
		} // end if

		return listChild;
	} // end getChildNodes()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#isDefaultNamespace(java.lang.String)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param namespaceURI
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final boolean isDefaultNamespace(final String namespaceURI) {
		if (getNodeType() == Node.TEXT_NODE
				|| getNodeType() == Node.CDATA_SECTION_NODE
				|| getNodeType() == Node.COMMENT_NODE) {
			if (parentNode != null) {
				return parentNode.isDefaultNamespace(namespaceURI);
			}
		}
		if (getNodeType() == Node.ENTITY_NODE
				|| getNodeType() == Node.DOCUMENT_TYPE_NODE
				|| getNodeType() == Node.NOTATION_NODE) {
			return false;
		}
		if (isDom1() && getNodeType() != Node.DOCUMENT_NODE) {
			return false;
		}
		if (getNodeType() == Node.ATTRIBUTE_NODE && parentNode == null) {
			return false;
		}
		return getDefaultNamespaceURI() == null ? namespaceURI == null
				: getDefaultNamespaceURI().equals(namespaceURI)
						&& prefix == null;
	} // end isDefaultNamespace()

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final String getDefaultNamespaceURI() {
		List listns = getNamespaceList();
		if (listns == null)
			return null;
		for (int i = 0; i < listns.size(); i++) {
			CNamespace ns = (CNamespace) listns.get(i);

			if (ns.isDefault()) {
				return ns.namespaceURI;
			} // end if
		} // end for
		if (getNodeType() == Node.DOCUMENT_NODE) {
			CElement elem = (CElement) ((ADocument) this).getDocumentElement();
			if (elem.nameSpace != null && elem.prefix == null) {
				return elem.nameSpace;
			}
		} else {
			if (nameSpace != null && prefix == null) {
				return nameSpace;
			}
		}

		return null;
	} // end getDefaultNamespaceURI()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#isEqualNode(org.w3c.dom.Node)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param arg
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final boolean isEqualNode(final Node arg) {
		if (getNodeType() == DOCUMENT_TYPE_NODE
				&& arg.getNodeType() == DOCUMENT_TYPE_NODE) {
			return arg.getNodeName().equals(this.getNodeName());
		}
		if (getNodeType() == ELEMENT_NODE && arg.getNodeType() == ELEMENT_NODE) {
			Element e1 = (Element) this.cloneNode(true);
			Element e2 = (Element) arg.cloneNode(true);
			e1.normalize();
			e2.normalize();
			return e1.toString().trim().equals(e2.toString().trim());
		}
		if (getNodeType() == DOCUMENT_NODE
				&& arg.getNodeType() == DOCUMENT_NODE) {
			String i1 = ((Document) this).getInputEncoding();
			if (i1 == null)
				i1 = "";
			String i2 = ((Document) arg).getInputEncoding();
			if (i2 == null)
				i2 = "";

			if (!(i1.equals(i2)))
				return false;
			Document e1 = (Document) this.cloneNode(true);
			Document e2 = (Document) arg.cloneNode(true);
			e1.getDomConfig().setParameter(CDOMConfiguration.CANONICAL_FORM,
					Boolean.TRUE);
			e2.getDomConfig().setParameter(CDOMConfiguration.CANONICAL_FORM,
					Boolean.TRUE);
			e1.normalize();
			e2.normalize();
			e1.normalizeDocument();
			e2.normalizeDocument();
			return e1.toString().trim().equals(e2.toString().trim());
		}
		if (getNodeType() == ATTRIBUTE_NODE
				&& arg.getNodeType() == ATTRIBUTE_NODE) {
			Attr e1 = (Attr) this;
			Attr e2 = (Attr) arg;
			if ((e1.getLocalName() == null && e2.getLocalName() != null)
					|| (e1.getLocalName() != null && e2.getLocalName() == null)) {
				return false;
			}
			if ((e1.getNamespaceURI() == null && e2.getNamespaceURI() != null)
					|| (e1.getNamespaceURI() != null && !e1.getNamespaceURI()
							.equals(e2.getNamespaceURI()))) {
				return false;
			}
			return e1.toString().trim().equals(e2.toString().trim());
		}
		return arg.toString().equals(this.toString());
	} // end isEqualNode()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getFeature(java.lang.String, java.lang.String)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param feature
	 *            DOCUMENT ME!
	 * @param version
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final Object getFeature(String feature, final String version) {
		if (feature.startsWith("+"))
			feature = feature.substring(1);
		if (ownerDocument != null) {
			DOMImplementation impl = ownerDocument.getImplementation();
			if (impl != null) {
				if (impl.hasFeature(feature, version))
					return this;
			}
		} else if (getNodeType() == DOCUMENT_NODE) {
			DOMImplementation impl = ((Document) this).getImplementation();
			if (impl != null) {
				if (impl.hasFeature(feature, version))
					return this;
			}
		} else if (getNodeType() == DOCUMENT_FRAGMENT_NODE) {
			DOMImplementation impl = ((CDocumentFragment) this)
					.getImplementation();
			if (impl != null) {
				if (impl.hasFeature(feature, version))
					return this;
			}
		}
		return null;
	} // end getFeature()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getFirstChild()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Node getFirstChild() {
		if (listChild == null) {
			return null;
		} // end if

		if (listChild.getLength() > 0) {
			return listChild.item(0);
		} // end if

		return null;
	} // end getFirstChild()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getLastChild()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Node getLastChild() {
		if (listChild == null) {
			return null;
		} // end if

		if (listChild.getLength() > 0) {
			return listChild.item(listChild.getLength() - 1);
		} // end if

		return null;
	} // end getLastChild()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getLocalName()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final String getLocalName() {
		return localName;
	} // end getLocalName()

	protected boolean isDom1 = false;

	public void dom1Nullify() {
		prefix = null;
		localName = null;
		nameSpace = "  ";
		isDom1 = true;
	}

	public boolean isDom1() {
		return isDom1;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final List getNamespaceList() {
		if (ownerDocument != null && !ownerDocument.hasNS) {
			return null;
		}
		if (this.getNodeType() == Node.ELEMENT_NODE) {
			return _GetNamespaceList((CElement) this, new ArrayList(0));
		} // end if
		else if (this.getNodeType() == Node.DOCUMENT_NODE) {
			ADocument doc = (ADocument) this;
			CElement elem = (CElement) doc.getDocumentElement();
			if (elem != null) {
				return _GetNamespaceList(elem, new ArrayList(0));
			}
		} else if (this.getNodeType() == Node.ATTRIBUTE_NODE) {
			return _GetNamespaceList((CElement) this, new ArrayList(0));
		} // end else
		return null;
	} // end getNamespaceList()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNamespaceURI()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final String getNamespaceURI() {
		if (ownerDocument != null && !ownerDocument.hasNS) {
			return nameSpace == null ? null
					: ("".equals(nameSpace.trim()) ? null : nameSpace);
		}
		if (nameSpace == null) {
			if (this.getNodeType() == Node.ATTRIBUTE_NODE) {
				if ("xmlns".equals(this.prefix)
						|| "xmlns".equals(this.localName)) {
					nameSpace = "http://www.w3.org/2000/xmlns/";
					return nameSpace;
				} // end if
			} // end if

			List nslist = getNamespaceList();
			if (nslist != null) {
				for (int i = 0; i < nslist.size(); i++) {
					CNamespace ns = (CNamespace) nslist.get(i);
					if (prefix == null && ns.prefix == null) {
						nameSpace = ns.namespaceURI;

						break;
					} // end if
					else if ((prefix != null) && prefix.equals(ns.prefix)) {
						nameSpace = ns.namespaceURI;

						break;
					} // end else if
				} // end for
				if (nameSpace == null) {
					if (ownerDocument != null && !ownerDocument.isBuildStage)
						nameSpace = "";
				}
			} else {
				if (ownerDocument != null && !ownerDocument.isBuildStage)
					nameSpace = "";
			}
		} // end if
		return nameSpace == null ? null : ("".equals(nameSpace.trim()) ? null
				: nameSpace);
	} // end getNamespaceURI()

	public void setNamespaceURI(String nameSpace) {
		this.nameSpace = nameSpace == null ? "" : nameSpace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNextSibling()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final Node getNextSibling() {
		if (parentNode != null) {
			return parentNode._GetNextSibling(this);
		} // end if

		return null;
	} // end getNextSibling()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNodeName()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final String getNodeName() {
		return name;
	} // end getNodeName()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getOwnerDocument()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Document getOwnerDocument() {
		return ownerDocument;
	} // end getOwnerDocument()

	public ADocument getOwnerCDocument() {
		return ownerDocument;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param parent
	 *            DOCUMENT ME!
	 */
	public void setParent(final CElement parent) {
		parentNode = parent;
	} // end setParent()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNodeType()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public abstract short getNodeType();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#setNodeValue(java.lang.String)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param nodeValue
	 *            DOCUMENT ME!
	 * 
	 * @throws DOMException
	 *             DOCUMENT ME!
	 */
	public abstract void setNodeValue(final String nodeValue)
			throws DOMException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getNodeValue()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DOMException
	 *             DOCUMENT ME!
	 */
	public abstract String getNodeValue() throws DOMException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getParentNode()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Node getParentNode() {
		return parentNode;
	} // end getParentNode()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#setPrefix(java.lang.String)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param prefix
	 *            DOCUMENT ME!
	 * 
	 * @throws DOMException
	 *             DOCUMENT ME!
	 */
	public final void setPrefix(String prefix) throws DOMException {
		isReadOnly();
		String ns = getNamespaceURI();
		if (ns == null)
			throw new DOMException(DOMException.NAMESPACE_ERR,
					"The namespace corresponding to prefix " + prefix
							+ " was not found.");
		if (prefix != null && prefix.indexOf(':', 0) != -1)
			throw new DOMException(DOMException.NAMESPACE_ERR, "");
		if ("xml".equals(prefix)
				&& !"http://www.w3.org/XML/1998/namespace".equals(ns)) {
			throw new DOMException(
					DOMException.NAMESPACE_ERR,
					"xml namespace is a reserved namespace, and must be 'http://www.w3.org/XML/1998/namespace'.");
		}
		if (getNodeType() == Node.ATTRIBUTE_NODE && "xmlns".equals(prefix)
				&& !"http://www.w3.org/2000/xmlns/".equals(ns)) {
			throw new DOMException(
					DOMException.NAMESPACE_ERR,
					"xmlns namespace is a reserved namespace, and must be 'http://www.w3.org/2000/xmlns/'");
		}
		if (getNodeType() == Node.ATTRIBUTE_NODE
				&& "xmlns".equals(getNodeName())) {
			throw new DOMException(DOMException.NAMESPACE_ERR,
					"xmlns namespace is a reserved namespace.");
		}
		if (!ownerDocument.isBuildStage)
			ADocument.checkNameValidXML(prefix, ownerDocument.getXmlVersion());
		this.prefix = prefix;
		if (prefix == null || prefix.equals("")) {
			this.prefix = null;
			this.name = localName;
		} // end if
		else {
			this.name = prefix + ":" + localName;
		} // end else
		this.nameSpace = null;
	} // end setPrefix()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getPrefix()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final String getPrefix() {
		return prefix;
	} // end getPrefix()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getPreviousSibling()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final Node getPreviousSibling() {
		if (parentNode != null) {
			return parentNode._GetPreviousSibling(this);
		} // end if

		return null;
	} // end getPreviousSibling()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#isSameNode(org.w3c.dom.Node)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param other
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final boolean isSameNode(final Node other) {
		return other == this;
	} // end isSameNode()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#isSupported(java.lang.String, java.lang.String)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param feature
	 *            DOCUMENT ME!
	 * @param version
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final boolean isSupported(final String feature, final String version) {
		if (this.getNodeType() == Node.DOCUMENT_NODE) {
			return (((Document) this).getImplementation() == null ? false
					: ((Document) this).getImplementation().hasFeature(feature,
							version));
		}
		return ownerDocument == null ? false : (ownerDocument
				.getImplementation() == null ? false : ownerDocument
				.getImplementation().hasFeature(feature, version));
	} // end isSupported()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#setTextContent(java.lang.String)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param textContent
	 *            DOCUMENT ME!
	 * 
	 * @throws DOMException
	 *             DOCUMENT ME!
	 */
	public void setTextContent(final String textContent) throws DOMException {
	} // end setTextContent()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getTextContent()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DOMException
	 *             DOCUMENT ME!
	 */
	public String getTextContent() throws DOMException {
		if (this.getNodeType() == Node.ELEMENT_NODE
				|| this.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE
				|| this.getNodeType() == Node.ENTITY_REFERENCE_NODE
				|| this.getNodeType() == Node.ATTRIBUTE_NODE
				|| this.getNodeType() == Node.ENTITY_NODE) {
			return _GetTextContent((Element) this, null);
		} // end if
		else if ((this.getNodeType() == Node.TEXT_NODE)
				|| (this.getNodeType() == Node.CDATA_SECTION_NODE)) {
			return getNodeValue();
		} // end else if
		else {
			return null;
		} // end else
	} // end getTextContent()

	private boolean isGrandChild(Node toTst, Node parent) {
		if (parent == null)
			return false;
		if (parent.getChildNodes() != null
				&& ((CNodeList) parent.getChildNodes()).hasItem(toTst)) {
			return true;
		}
		if (parent.getAttributes() != null
				&& parent.getAttributes().getNamedItem(toTst.getNodeName()) == toTst) {
			return true;
		}
		NodeList nl = parent.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			boolean result = isGrandChild(toTst, nl.item(i));
			if (result)
				return true;
		}
		return false;
	}

	private boolean isPrevious(Node toTst, Node child) {
		child = child.getPreviousSibling();
		while (child != null) {
			if (child == toTst)
				return true;
			child = child.getPreviousSibling();
		}
		return false;
	}

	private boolean isNext(Node toTst, Node child) {
		child = child.getNextSibling();
		while (child != null) {
			if (child == toTst)
				return true;
			child = child.getNextSibling();
		}
		return false;
	}

	private boolean isGrandParent(Node toTst, Node child) {
		if (child == null)
			return false;
		if (((ANode) child).parentNode == toTst) {
			return true;
		}
		boolean result = isGrandParent(toTst, ((ANode) child).parentNode);
		if (result)
			return true;
		return false;
	}

	private Node hasCommonAncestor(Node toTst, Node child) {
		List parentChild = new ArrayList();
		List parenttoTst = new ArrayList();
		while (((ANode) child).parentNode != null) {
			parentChild.add(((ANode) child).parentNode);
			child = ((ANode) child).parentNode;
		}
		while (((ANode) toTst).parentNode != null) {
			parenttoTst.add(((ANode) toTst).parentNode);
			toTst = ((ANode) toTst).parentNode;
		}
		for (int i = 0; i < parentChild.size(); i++) {
			Node n = (Node) parentChild.get(i);
			if (parenttoTst.contains(n))
				return n;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#compareDocumentPosition(org.w3c.dom.Node)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param other
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DOMException
	 *             DOCUMENT ME!
	 */
	public final short compareDocumentPosition(final Node other)
			throws DOMException {
		if (other instanceof ANode) {
			if (this == other)
				return 0;
			short flags = 0;
			Node commonAncestor = null;
			if (other.getNodeType() == Node.ENTITY_NODE
					&& getNodeType() == Node.NOTATION_NODE) {
				flags = (short) (flags | Node.DOCUMENT_POSITION_PRECEDING);
			} else if (other.getNodeType() == Node.NOTATION_NODE
					&& getNodeType() == Node.ENTITY_NODE) {
				flags = (short) (flags | Node.DOCUMENT_POSITION_FOLLOWING);
			} else if (isGrandParent(other, this)) {
				flags = (short) (flags | Node.DOCUMENT_POSITION_CONTAINS | Node.DOCUMENT_POSITION_PRECEDING);
			} else if (isGrandChild(other, this)) {
				flags = (short) (flags | Node.DOCUMENT_POSITION_CONTAINED_BY | Node.DOCUMENT_POSITION_FOLLOWING);
			} else if (isPrevious(other, this)) {
				flags = (short) (flags | Node.DOCUMENT_POSITION_PRECEDING);
			} else if (isNext(other, this)) {
				flags = (short) (flags | Node.DOCUMENT_POSITION_FOLLOWING);
			} else if ((commonAncestor = hasCommonAncestor(other, this)) != null) {
				ANode po = (ANode) other;
				ANode pt = this;
				while (pt.parentNode != commonAncestor)
					pt = pt.parentNode;
				while (po.parentNode != commonAncestor)
					po = po.parentNode;
				if (isPrevious(po, pt)) {
					flags = (short) (flags | Node.DOCUMENT_POSITION_PRECEDING);
				} else if (isNext(po, pt)) {
					flags = (short) (flags | Node.DOCUMENT_POSITION_FOLLOWING);
				} else if (po.getNodeType() != Node.ATTRIBUTE_NODE
						&& pt.getNodeType() == Node.ATTRIBUTE_NODE) {
					flags = (short) (flags | Node.DOCUMENT_POSITION_FOLLOWING);
				} else if (po.getNodeType() == Node.ATTRIBUTE_NODE
						&& pt.getNodeType() != Node.ATTRIBUTE_NODE) {
					flags = (short) (flags | Node.DOCUMENT_POSITION_PRECEDING);
				} else if (po.getNodeType() == Node.ATTRIBUTE_NODE
						&& pt.getNodeType() == Node.ATTRIBUTE_NODE) {
					short opos = ((ANode) other).getPos();
					short pos = getPos();
					int rpos = getNodeType() == Node.DOCUMENT_NODE ? ((ADocument) this).rpos
							: (getNodeType() == Node.DOCUMENT_FRAGMENT_NODE ? ((CDocumentFragment) this).rpos
									: ownerDocument.rpos);
					if (opos > pos) {
						flags = 0xFB;
						rpos = rpos & 0xFB;
					} else {
						flags = 0xFD;
						rpos = rpos & 0xFD;
					}
					flags = (short) (flags
							| Node.DOCUMENT_POSITION_DISCONNECTED | rpos | 0x21);
					flags = (short) (flags & 0xE6);
				}
			} else {
				short opos = ((ANode) other).getPos();
				short pos = getPos();
				int rpos = getNodeType() == Node.DOCUMENT_NODE ? ((ADocument) this).rpos
						: (getNodeType() == Node.DOCUMENT_FRAGMENT_NODE ? ((CDocumentFragment) this).rpos
								: ownerDocument.rpos);
				if (opos > pos) {
					flags = 0xFB;
					rpos = rpos & 0xFB;
				} else {
					flags = 0xFD;
					rpos = rpos & 0xFD;
				}
				flags = (short) (flags | Node.DOCUMENT_POSITION_DISCONNECTED
						| rpos | 0x21);
				if (other.getNodeType() == Node.ENTITY_NODE
						&& getNodeType() == Node.ENTITY_NODE) {
					flags = (short) (flags & 0xE6);
				} else {
					flags = (short) (flags & 0xE7);
				}
			}
			return flags;
		} else {
			throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
					"Comparing a node from another implementation is not supported.");
		}
	} // end compareDocumentPosition()

	short getPos() {
		int rpos = getNodeType() == Node.DOCUMENT_NODE ? ((ADocument) this).rpos
				: (getNodeType() == Node.DOCUMENT_FRAGMENT_NODE ? ((CDocumentFragment) this).rpos
						: ownerDocument.rpos);
		if (parentNode != null) {
			NodeList nl = parentNode.listChild;
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					if (nl.item(i) == this) {
						return (short) ((i + 1) * rpos);
					}
				}
			}
			NamedNodeMap nnm = parentNode.listAttributes;
			if (nnm != null) {
				for (int i = 0; i < nnm.getLength(); i++) {
					if (nnm.item(i) == this) {
						return (short) ((i + 1) * rpos);
					}
				}
			}
		} else if (getNodeType() == Node.ENTITY_NODE) {
			DocumentType dt = ownerDocument.getDoctype();
			NamedNodeMap nnm = dt.getEntities();
			for (int i = 0; i < nnm.getLength(); i++) {
				if (nnm.item(i) == this) {
					return (short) ((i + 1) * rpos);
				}
			}
		} else if (getNodeType() == Node.NOTATION_NODE) {
			DocumentType dt = ownerDocument.getDoctype();
			NamedNodeMap nnm = dt.getNotations();
			for (int i = 0; i < nnm.getLength(); i++) {
				if (nnm.item(i) == this) {
					return (short) ((i + 1) * rpos);
				}
			}
		} else {
			rpos = this.rpos;
		}
		return (short) rpos;
	}

	protected boolean isReadOnly = false;

	protected boolean ignoreAll = false;

	public void isReadOnly() throws DOMException {
		if (ownerDocument != null && ownerDocument.isBuildStage())
			return;
		if (getNodeType() == DOCUMENT_NODE && ((ADocument) this).isBuildStage())
			return;
		if (ignoreAll)
			return;
		ANode current = this;
		while (current != null) {
			if (current.isReadOnly)
				throw new DOMException(
						DOMException.NO_MODIFICATION_ALLOWED_ERR,
						"The node is read-only.");
			current = (ANode) current.getParentNode();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#hasAttributes()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final boolean hasAttributes() {
		return listAttributes != null ? listAttributes.getLength() > 0 : false;
	} // end hasAttributes()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#insertBefore(org.w3c.dom.Node, org.w3c.dom.Node)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param newChild
	 *            DOCUMENT ME!
	 * @param refChild
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DOMException
	 *             DOCUMENT ME!
	 */
	public Node insertBefore(final Node newChild, final Node refChild)
			throws DOMException {
		isReadOnly();
		if (isGrandChild(newChild, refChild)) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					"Cannot insert node here.");
		}
		if (isGrandParent(newChild, refChild)) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					"Cannot insert node here.");
		}
		if (newChild == this) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					"Cannot insert node here.");
		}
		if (newChild.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
			if (ownerDocument != null
					|| this.getNodeType() == Node.DOCUMENT_NODE) {
				NodeList nl = this.getNodeType() == Node.DOCUMENT_NODE ? listChild
						: ownerDocument.listChild;
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					if (n.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
						throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
								"Cannot insert node here.");
					}
				}
			}
		}
		if (newChild.getNodeType() == Node.ELEMENT_NODE
				&& this.getNodeType() == Node.DOCUMENT_NODE) {
			if (((ADocument) this).getDocumentElement().getNodeType() == ELEMENT_NODE) {
				throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
						"A document can have only one root node.");
			}
		}
		if (this.getNodeType() == Node.DOCUMENT_NODE) {
			if (newChild.getOwnerDocument() != null
					&& newChild.getOwnerDocument() != this) {
				throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
						"The owner document of both node are different.");
			}
		} else if (ownerDocument != null) {
			if (newChild.getOwnerDocument() != null
					&& newChild.getOwnerDocument() != ownerDocument) {
				throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
						"The owner document of both node are different.");
			}
		} else if (ownerDocument == null) {
			if (newChild.getOwnerDocument() != null
					&& newChild.getOwnerDocument() != refChild
							.getOwnerDocument()) {
				throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
						"The owner document of both node are different.");
			}
		}
		return _InsertBefore(newChild, refChild);
	} // end insertBefore()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#lookupNamespaceURI(java.lang.String)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param prefix
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final String lookupNamespaceURI(final String prefix) {
		if (getNodeType() == Node.TEXT_NODE
				|| getNodeType() == Node.CDATA_SECTION_NODE
				|| getNodeType() == Node.COMMENT_NODE) {
			if (parentNode != null)
				return parentNode.lookupNamespaceURI(prefix);
		}

		List nslist = getNamespaceList();
		if (nslist != null) {
			for (int i = 0; i < nslist.size(); i++) {
				CNamespace ns = (CNamespace) nslist.get(i);
				if (prefix == null && ns.prefix == null) {
					return ns.namespaceURI != null ? (ns.namespaceURI.trim()
							.length() == 0 ? null : ns.namespaceURI) : null;
				} else if (prefix != null && prefix.equals(ns.prefix)) {
					return ns.namespaceURI != null ? (ns.namespaceURI.trim()
							.length() == 0 ? null : ns.namespaceURI) : null;
				} // end if
			} // end for
		}
		if (getNodeType() == Node.DOCUMENT_NODE) {
			CElement elem = (CElement) ((ADocument) this).getDocumentElement();
			if (prefix != null && prefix.equals(elem.getPrefix())) {
				return getNamespaceURI();
			} else if (prefix == null && elem.getPrefix() == null) {
				return elem.getNamespaceURI();
			}
		} else if (getNodeType() == Node.ELEMENT_NODE) {
			if (prefix != null && prefix.equals(getPrefix())) {
				return getNamespaceURI();
			} else if (prefix == null && getPrefix() == null) {
				return getNamespaceURI();
			}
		}
		return null;
	} // end lookupNamespaceURI()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#lookupPrefix(java.lang.String)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param namespaceURI
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final String lookupPrefix(final String namespaceURI) {
		if (getNodeType() == Node.TEXT_NODE
				|| getNodeType() == Node.CDATA_SECTION_NODE
				|| getNodeType() == Node.COMMENT_NODE) {
			if (parentNode != null) {
				return parentNode.lookupPrefix(namespaceURI);
			}
		}
		List nslist = getNamespaceList();
		if (nslist != null) {
			for (int i = 0; i < nslist.size(); i++) {
				CNamespace ns = (CNamespace) nslist.get(i);
				if (namespaceURI == null && ns.namespaceURI == null) {
					return ns.prefix;
				} else if (namespaceURI != null
						&& namespaceURI.equals(ns.namespaceURI)) {
					return ns.prefix;
				} // end if
			} // end for
		}
		if (getNodeType() == Node.DOCUMENT_NODE) {
			CElement elem = (CElement) ((ADocument) this).getDocumentElement();
			if (namespaceURI != null
					&& namespaceURI.equals(elem.getNamespaceURI())) {
				return elem.getPrefix();
			}
		} else if (getNodeType() == Node.ELEMENT_NODE) {
			if (namespaceURI != null && namespaceURI.equals(getNamespaceURI())) {
				return getPrefix();
			}
		}
		return null;
	} // end lookupPrefix()

	public final void _normalizeDocument() {
		try {
			_normalizeDocument(new ArrayList());
		} catch (CDOMError e) {
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#normalize()
	 */
	/**
	 * DOCUMENT ME!
	 */
	public final void _normalizeDocument(List errorNode) {
		if (parentNode == null && getNodeType() != Node.DOCUMENT_NODE)
			return;
		// this.nsList = null;
		NodeList nl = getChildNodes();
		ADocument ownerDocument = this.ownerDocument == null
				&& this.getNodeType() == Node.DOCUMENT_NODE ? (ADocument) this
				: this.ownerDocument;
		if (((Boolean) ownerDocument.getDomConfig().getParameter(
				CDOMConfiguration.CANONICAL_FORM)).booleanValue()) {
			if (getNodeType() == Node.DOCUMENT_TYPE_NODE) {
				Node parent = getParentNode();
				parent.removeChild(this);
				ownerDocument.setDocumentType(null);
				((ANode) parent)._normalizeDocument(errorNode);
				return;
			}
		}
		if ((((Boolean) ownerDocument.getDomConfig().getParameter(
				CDOMConfiguration.INFOSET)).booleanValue())
				|| (((Boolean) ownerDocument.getDomConfig().getParameter(
						CDOMConfiguration.WELL_FORMED)).booleanValue())) {
			if (getNodeType() == Node.ELEMENT_NODE
					|| getNodeType() == Node.ATTRIBUTE_NODE) {
				try {
					if (getLocalName() != null)
						ownerDocument.checkNameValidXML(getLocalName());
					else
						ownerDocument.checkNameValidXML(getNodeName());
				} catch (DOMException e) {
					if (ownerDocument.getDomConfig() != null) {
						if (!errorNode.contains(this)) {
							errorNode.add(this);
							DOMErrorHandler err = (DOMErrorHandler) ownerDocument
									.getDomConfig().getParameter(
											CDOMConfiguration.ERROR_HANDLER);
							if (err != null) {
								CDOMError error = new CDOMError(
										DOMError.SEVERITY_ERROR,
										new CDOMLocator(this, null),
										"wf-invalid-character-in-node-name");
								if (!err.handleError(error)) {
									throw error;
								}
							}
						}
					}
				}
			}
		}
		if ((!((Boolean) ownerDocument.getDomConfig().getParameter(
				CDOMConfiguration.ENTITIES)).booleanValue())) {
			if (getNodeType() == Node.ENTITY_REFERENCE_NODE) {
				if (nl.getLength() == 0)
					return;
				for (int i = 0; i < nl.getLength(); i++) {
					getParentNode().insertBefore(nl.item(i).cloneNode(true),
							this);
				}
				Node parent = getParentNode();
				parent.removeChild(this);
			}
		}
		if ((((Boolean) ownerDocument.getDomConfig().getParameter(
				CDOMConfiguration.NAMESPACES)).booleanValue())) {
			if (getNodeType() == Node.ATTRIBUTE_NODE) {
				if (isDom1()) {
					if (!errorNode.contains(this)) {
						errorNode.add(this);
						DOMErrorHandler err = (DOMErrorHandler) ownerDocument
								.getDomConfig().getParameter(
										CDOMConfiguration.ERROR_HANDLER);
						if (err != null) {
							CDOMError error = new CDOMError(
									DOMError.SEVERITY_ERROR, new CDOMLocator(
											this, null), "1_o/* paf !");
							if (!err.handleError(error)) {
								throw error;
							}
						}
					}
				}
			} else if (getNodeType() == Node.ELEMENT_NODE
					|| getNodeType() == Node.DOCUMENT_NODE) {
				if (isDom1()
						&& (getNodeType() == Node.ELEMENT_NODE || getNodeType() == Node.ATTRIBUTE_NODE)) {
					if (!errorNode.contains(this)) {
						errorNode.add(this);
						DOMErrorHandler err = (DOMErrorHandler) ownerDocument
								.getDomConfig().getParameter(
										CDOMConfiguration.ERROR_HANDLER);
						if (err != null) {
							CDOMError error = new CDOMError(
									DOMError.SEVERITY_ERROR, new CDOMLocator(
											this, null), "2_o/* paf !");
							if (!err.handleError(error)) {
								throw error;
							}
						}
					}
				}
				boolean isReadOnly = false;
				try {
					isReadOnly();
				} catch (DOMException e) {
					isReadOnly = true;
				}
				if (!isReadOnly) {
					List toRemove = new ArrayList();
					NamedNodeMap nnm = getAttributes();
					if (!((Boolean) ownerDocument.getDomConfig().getParameter(
							CDOMConfiguration.NAMESPACE_DECLARATIONS))
							.booleanValue()) {
						if (nnm != null) {
							for (int i = 0; i < nnm.getLength(); i++) {
								CAttr at = (CAttr) nnm.item(i);
								toRemove.add(at);
							}
						}
						for (int i = 0; i < toRemove.size(); i++) {
							((Element) this)
									.removeAttributeNode((Attr) toRemove.get(i));
						}
						setNamespaceURI("  ");
						// this.nsList = null;
					} else {
						if (nnm != null) {
							for (int i = 0; i < nnm.getLength(); i++) {
								CAttr at = (CAttr) nnm.item(i);
								if (at.isDefaults()) {
									at.setSpecified(true);
								}
								if ("xmlns".equals(at.getPrefix())) {
									String ns = at.lookupNamespaceURI(at
											.getLocalName());
									if (ns == null) {
										toRemove.add(at);
									} else {
										NodeList nlns = at
												.getOwnerElement()
												.getElementsByTagNameNS(ns, "*");
										if (nlns.getLength() == 0) {
											toRemove.add(at);
										}
									}
								}
							}
						}
						for (int i = 0; i < toRemove.size(); i++) {
							((Element) this)
									.removeAttributeNode((Attr) toRemove.get(i));
						}
					}
				}
			}
		}
		if ((!((Boolean) ownerDocument.getDomConfig().getParameter(
				CDOMConfiguration.SPLIT_CDATA_SECTIONS)).booleanValue())) {
			if (getNodeType() == Node.CDATA_SECTION_NODE) {
				String value = getNodeValue();
				if (value.indexOf("]]>") != -1) {
					if (!errorNode.contains(this)) {
						errorNode.add(this);
						DOMErrorHandler err = (DOMErrorHandler) ownerDocument
								.getDomConfig().getParameter(
										CDOMConfiguration.ERROR_HANDLER);
						if (err != null) {
							CDOMError error = new CDOMError(
									DOMError.SEVERITY_ERROR, new CDOMLocator(
											this, null),
									"cdata-sections-not-split");
							if (!err.handleError(error)) {
								throw error;
							}
						}
					}
				}
			}
		}
		if ((((Boolean) ownerDocument.getDomConfig().getParameter(
				CDOMConfiguration.SPLIT_CDATA_SECTIONS)).booleanValue())) {
			if (getNodeType() == Node.CDATA_SECTION_NODE) {
				Node parent = getParentNode();
				String value = getNodeValue();
				if (value.indexOf("]]>", 0) != -1) {
					CDATASection first = null;
					while (value.indexOf("]]>", 0) != -1) {
						// need to split
						String pre = value
								.substring(0, value.indexOf("]]>", 0));
						CDATASection section = ownerDocument
								.createCDATASection(pre);
						if (first == null)
							first = section;
						parent.insertBefore(section, this);
						Text t = ownerDocument.createTextNode("]]>");
						parent.insertBefore(t, this);
						value = value.substring(value.indexOf("]]>", 0) + 3);
					}
					if (value.length() > 0) {
						CDATASection section = ownerDocument
								.createCDATASection(value);
						parent.insertBefore(section, this);
					}
					if (!errorNode.contains(this)) {
						errorNode.add(this);
						DOMErrorHandler err = (DOMErrorHandler) ownerDocument
								.getDomConfig().getParameter(
										CDOMConfiguration.ERROR_HANDLER);
						if (err != null) {
							CDOMError error = new CDOMError(
									DOMError.SEVERITY_WARNING, new CDOMLocator(
											first, null),
									"cdata-sections-splitted");
							if (!err.handleError(error)) {
								throw error;
							}
						}
					}
					parent.removeChild(this);
					((ANode) parent)._normalizeDocument(errorNode);
					return;
				}
			}
		}
		if ((!((Boolean) ownerDocument.getDomConfig().getParameter(
				CDOMConfiguration.CDATA_SECTIONS)).booleanValue())) {
			if (getNodeType() == Node.CDATA_SECTION_NODE) {
				Text t = ownerDocument.createTextNode(getNodeValue());
				Node parent = getParentNode();
				parent.insertBefore(t, this);
				parent.removeChild(this);
				((ANode) parent)._normalizeDocument(errorNode);
				return;
			}
		}
		if ((!((Boolean) ownerDocument.getDomConfig().getParameter(
				CDOMConfiguration.COMMENTS)).booleanValue())) {
			if (getNodeType() == Node.COMMENT_NODE) {
				Node parent = getParentNode();
				parent.removeChild(this);
				((ANode) parent)._normalizeDocument(errorNode);
				return;
			}
		}
		if (((Boolean) ownerDocument.getDomConfig().getParameter(
				CDOMConfiguration.CHECK_CHARACTER_NORMALIZATION))
				.booleanValue()
				|| ((Boolean) ownerDocument.getDomConfig().getParameter(
						CDOMConfiguration.NORMALIZE_CHARACTERS)).booleanValue()) {
			if (getNodeType() == TEXT_NODE) {
				String value = getNodeValue();
				String nvalue = Normalizer
						.normalize(value, Normalizer.Form.NFC);
				try {
					setNodeValue(nvalue);
				} catch (DOMException ignore) {
				}
				if (((Boolean) ownerDocument.getDomConfig().getParameter(
						CDOMConfiguration.CHECK_CHARACTER_NORMALIZATION))
						.booleanValue()) {
					if (nvalue.toCharArray().length != value.toCharArray().length) {
						if (!errorNode.contains(this)) {
							errorNode.add(this);
							DOMErrorHandler err = (DOMErrorHandler) ownerDocument
									.getDomConfig().getParameter(
											CDOMConfiguration.ERROR_HANDLER);
							if (err != null) {
								CDOMError error = new CDOMError(
										DOMError.SEVERITY_ERROR,
										new CDOMLocator(this, null),
										"check-character-normalization-failure");
								if (!err.handleError(error)) {
									throw error;
								}
							}
						}
					}
				}
			}
		}
		if ((!((Boolean) ownerDocument.getDomConfig().getParameter(
				CDOMConfiguration.ELEMENT_CONTENT_WHITESPACE)).booleanValue())) {
			if (getNodeType() == TEXT_NODE || getNodeType() == ATTRIBUTE_NODE) {
				String value = getNodeValue().trim();
				value = value.replace('\n', ' ');
				value = value.replace('\r', ' ');
				value = value.replace('\t', ' ');
				value = value.replace((char) 65279, ' ');
				char[] array = value.toCharArray();
				char last = ' ';
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < array.length; i++) {
					if (array[i] == last && last == ' ')
						continue;
					last = array[i];
					buffer.append(array[i]);
				}
				try {
					setNodeValue(buffer.toString().trim());
				} catch (DOMException ignore) {
				}
			}
		}
		List toRemove = new ArrayList();
		Text n = null;
		boolean dom1 = isDom1();
		for (int i = 0; i < nl.getLength(); i++) {
			ANode node = (ANode) nl.item(i);
			if (node == null)
				continue;
			node._normalizeDocument(errorNode);
			if (node.getNodeType() == Node.TEXT_NODE) {
				if (n == null) {
					n = (Text) node;
					try {
						if (i == nl.getLength() - 1
								&& (nl.getLength() > 1 || getNodeType() == Node.DOCUMENT_FRAGMENT_NODE)) {
							if (n.getData().trim().length() == 0) {
								if (n.getData().length() > 0) {
									n.setData(" ");
								} else {
									toRemove.add(node);
								}
							} else {
								if (n.getNextSibling() != null
										&& n.getNextSibling().getNodeType() != Node.TEXT_NODE
										&& (n.getData().endsWith(" ")
												|| n.getData().endsWith("\t")
												|| n.getData().endsWith("\n") || n
												.getData().endsWith("\r"))) {
									if (n.getPreviousSibling() != null
											&& (n.getData().startsWith(" ")
													|| n.getData().startsWith(
															"\t")
													|| n.getData().startsWith(
															"\n") || n
													.getData().startsWith("\r"))) {
										n.setData(" " + n.getData().trim()
												+ " ");
									} else {
										n.setData(n.getData().trim() + " ");
									}
								} else if (n.getPreviousSibling() != null
										&& (n.getData().startsWith(" ")
												|| n.getData().startsWith("\t")
												|| n.getData().startsWith("\n") || n
												.getData().startsWith("\r"))) {
									n.setData(" " + n.getData().trim());
								} else {
									n.setData(n.getData().trim());
								}
							}
						} else if (n.getData().trim().length() > 0) {
							if (n.getNextSibling() != null
									&& n.getNextSibling().getNodeType() != Node.TEXT_NODE
									&& (n.getData().endsWith(" ")
											|| n.getData().endsWith("\t")
											|| n.getData().endsWith("\n") || n
											.getData().endsWith("\r"))) {
								if (n.getPreviousSibling() != null
										&& (n.getData().startsWith(" ")
												|| n.getData().startsWith("\t")
												|| n.getData().startsWith("\n") || n
												.getData().startsWith("\r"))) {
									n.setData(" " + n.getData().trim() + " ");
								} else {
									n.setData(n.getData().trim() + " ");
								}
							} else if (n.getPreviousSibling() != null
									&& (n.getData().startsWith(" ")
											|| n.getData().startsWith("\t")
											|| n.getData().startsWith("\n") || n
											.getData().startsWith("\r"))) {
								n.setData(" " + n.getData().trim());
							} else {
								n.setData(n.getData().trim());
							}
						} else {
							if (n.getData().length() > 0) {
								n.setData(" ");
							} else {
								toRemove.add(node);
							}
						}
					} catch (DOMException ignore) {
					}
				} else {
					if (node.getNextSibling() != null
							&& node.getNextSibling().getNodeType() != Node.TEXT_NODE
							&& (node.getNodeValue().endsWith(" ")
									|| node.getNodeValue().endsWith("\t")
									|| node.getNodeValue().endsWith("\n") || node
									.getNodeValue().endsWith("\r"))) {
						if (node.getPreviousSibling() != null
								&& (node.getNodeValue().startsWith(" ")
										|| node.getNodeValue().startsWith("\t")
										|| node.getNodeValue().startsWith("\n") || node
										.getNodeValue().startsWith("\r"))) {
							n
									.appendData(" "
											+ node.getNodeValue().trim() + " ");
						} else {
							n.appendData(node.getNodeValue().trim() + " ");
						}
					} else if (node.getPreviousSibling() != null
							&& (node.getNodeValue().startsWith(" ")
									|| node.getNodeValue().startsWith("\t")
									|| node.getNodeValue().startsWith("\n") || node
									.getNodeValue().startsWith("\r"))) {
						n.appendData(" " + node.getNodeValue().trim());
					} else {
						n.appendData(node.getNodeValue().trim());
					}
					toRemove.add(node);
				}
			} else {
				n = null;
				if (node.getNodeType() == Node.ELEMENT_NODE
						&& this.getNodeType() == Node.ELEMENT_NODE
						&& dom1 != node.isDom1()) {
					if (ownerDocument.getDomConfig() != null) {
						if (errorNode.contains(node))
							continue;
						errorNode.add(node);
						DOMErrorHandler err = (DOMErrorHandler) ownerDocument
								.getDomConfig().getParameter(
										CDOMConfiguration.ERROR_HANDLER);
						if (err != null) {
							CDOMError error = new CDOMError(
									DOMError.SEVERITY_ERROR, new CDOMLocator(nl
											.item(i), null), null);
							if (!err.handleError(error)) {
								throw error;
							}
						}
					}
				}
			}
		}
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
				String val = nl.item(i).getNodeValue();
				if ("".equals(val)) {
					if (!toRemove.contains(nl.item(i)))
						toRemove.add(nl.item(i));
				}
			}
		}
		NamedNodeMap nnm = getAttributes();
		if (nnm != null) {
			for (int j = 0; j < nnm.getLength(); j++) {
				((ANode) nnm.item(j))._normalizeDocument(errorNode);

			}
		}
		for (int i = 0; i < toRemove.size(); i++) {
			if (listChild.hasItem((Node) toRemove.get(i))) {
				removeChild((Node) toRemove.get(i));
			}
		}
	}

	public final void addXSDDefault() {
		if (getNodeType() == Node.ELEMENT_NODE) {
			if (ownerDocument.hasXSD()) {
				Document xsd = ownerDocument.getXSD(getNamespaceURI());
				if (xsd != null) {
					NodeList nl = xsd.getElementsByTagNameNS(
							"http://www.w3.org/2001/XMLSchema", "element");
					for (int i = 0; i < nl.getLength(); i++) {
						Element n = (Element) nl.item(i);
						if (getLocalName() != null
								&& getLocalName()
										.equals(n.getAttribute("name"))) {
							NodeList attributes = n.getElementsByTagNameNS(
									"http://www.w3.org/2001/XMLSchema",
									"attribute");
							for (int j = 0; j < attributes.getLength(); j++) {
								Element attri = (Element) attributes.item(j);
								Attr def = attri.getAttributeNode("default");
								if (def != null) {
									String value = def.getValue();
									if (value != null && !"".equals(value)) {
										String attrName = attri
												.getAttribute("name");
										if (((CElement) this)
												.getAttributeNode(attrName) == null)
											((CElement) this)
													.setAttributeAsDefault(
															attrName, value);
									}
								}
							}
							break;
						}
					}
				}
			}
		}
		NodeList nl = getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			((ANode) nl.item(i)).addXSDDefault();
		}
	}

	public final void normalize() {
		if (getNodeType() == Node.TEXT_NODE) {
			if (ownerDocument != null
					&& ((Boolean) ownerDocument.getDomConfig().getParameter(
							CDOMConfiguration.NORMALIZE_CHARACTERS))
							.booleanValue()) {
				try {
					setNodeValue(Normalizer.normalize(getNodeValue(),
							Normalizer.Form.NFC));
				} catch (DOMException ignore) {
				}
			}
		}
		NodeList nl = getChildNodes();
		Text n = null;
		List toRemove = new ArrayList();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
				if (ownerDocument != null
						&& ((Boolean) ownerDocument.getDomConfig()
								.getParameter(
										CDOMConfiguration.NORMALIZE_CHARACTERS))
								.booleanValue()) {
					try {
						nl.item(i).setNodeValue(
								Normalizer.normalize(nl.item(i).getNodeValue(),
										Normalizer.Form.NFC));
					} catch (DOMException ignore) {
					}
				}
				if (n == null) {
					n = (Text) nl.item(i);
				} else {
					n.appendData(nl.item(i).getNodeValue());
					toRemove.add(nl.item(i));
				}
			} else {
				n = null;
				nl.item(i).normalize();
			}
		}
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
				String val = nl.item(i).getNodeValue();
				if ("".equals(val)) {
					if (!toRemove.contains(nl.item(i)))
						toRemove.add(nl.item(i));
				}
			}
		}
		NamedNodeMap nnm = getAttributes();
		if (nnm != null) {
			for (int j = 0; j < nnm.getLength(); j++) {
				nnm.item(j).normalize();
			}
		}
		for (int i = 0; i < toRemove.size(); i++)
			removeChild((Node) toRemove.get(i));
	}

	private static class CDOMLocator implements DOMLocator, Serializable {
		static final long serialVersionUID = 3086647613754528496L;
		Node rnode;
		String uri;

		public CDOMLocator(Node rnode, String uri) {
			this.rnode = rnode;
			this.uri = uri;
		}

		public int getByteOffset() {
			return -1;
		}

		public int getColumnNumber() {
			return -1;
		}

		public int getLineNumber() {
			return -1;
		}

		public Node getRelatedNode() {
			return rnode;
		}

		public String getUri() {
			return uri;
		}

		public int getUtf16Offset() {
			return -1;
		}

	}

	private static class CDOMError extends RuntimeException implements
			DOMError, Serializable {
		static final long serialVersionUID = -5673745854801719993L;
		short severity;
		CDOMLocator loc;
		String type;

		public CDOMError(short severity, CDOMLocator loc, String type) {
			this.severity = severity;
			this.loc = loc;
			this.type = type;
		}

		public DOMLocator getLocation() {
			return loc;
		}

		public String getMessage() {
			return loc.getRelatedNode() + " has error type " + severity;
		}

		public Object getRelatedData() {
			return loc.getRelatedNode();
		}

		public Object getRelatedException() {
			return this;
		}

		public short getSeverity() {
			return severity;
		}

		public String getType() {
			return type;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#removeChild(org.w3c.dom.Node)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param oldChild
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DOMException
	 *             DOCUMENT ME!
	 */
	public Node removeChild(final Node oldChild) throws DOMException {
		if (listChild == null || listChild.getLength() == 0) {
			throw new DOMException(DOMException.NOT_FOUND_ERR, "");
		} // end if
		isReadOnly();
		if (!listChild.hasItem(oldChild))
			throw new DOMException(DOMException.NOT_FOUND_ERR, "");
		listChild.removeItem(oldChild);
		((ANode) oldChild).setParent(null);
		return oldChild;
	} // end removeChild()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#replaceChild(org.w3c.dom.Node, org.w3c.dom.Node)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param newChild
	 *            DOCUMENT ME!
	 * @param oldChild
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DOMException
	 *             DOCUMENT ME!
	 */
	public Node replaceChild(final Node newChild, final Node oldChild)
			throws DOMException {
		isReadOnly();
		if (listChild == null || listChild.getLength() == 0) {
			throw new DOMException(DOMException.NOT_FOUND_ERR,
					"The node was not found.");
		} // end if
		if (!listChild.hasItem(oldChild))
			throw new DOMException(DOMException.NOT_FOUND_ERR,
					"The node was not found.");
		if (newChild.getNodeType() == Node.ENTITY_NODE) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					"Cannot insert node here.");
		}
		if (newChild.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
			if (ownerDocument != null
					|| this.getNodeType() == Node.DOCUMENT_NODE) {
				NodeList nl = this.getNodeType() == Node.DOCUMENT_NODE ? listChild
						: ownerDocument.listChild;
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					if (n.getNodeType() == Node.DOCUMENT_TYPE_NODE
							&& oldChild.getNodeType() != Node.DOCUMENT_TYPE_NODE) {
						throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
								"This operation is not supported.");
					}
				}
			}
		}
		if (newChild.getNodeType() == Node.ELEMENT_NODE
				&& this.getNodeType() == Node.DOCUMENT_NODE) {
			if (((ADocument) this).getDocumentElement() != oldChild) {
				throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
						"This operation is not supported.");
			}
		}
		if (isGrandParent(newChild, oldChild)) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					"Cannot insert node here.");
		}
		if (this.getNodeType() == Node.DOCUMENT_NODE) {
			if (newChild.getOwnerDocument() != null
					&& newChild.getOwnerDocument() != this) {
				throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
						"The owner document of both node are different.");
			}
		} else if (ownerDocument != null) {
			if (newChild.getOwnerDocument() != null
					&& newChild.getOwnerDocument() != ownerDocument) {
				throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
						"The owner document of both node are different.");
			}
		} else if (ownerDocument == null) {
			if (newChild.getOwnerDocument() != null
					&& newChild.getOwnerDocument() != oldChild
							.getOwnerDocument()) {
				throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
						"The owner document of both node are different.");
			}
		}
		return _ReplaceChild(newChild, oldChild);
	} // end replaceChild()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#setUserData(java.lang.String, java.lang.Object,
	 * org.w3c.dom.UserDataHandler)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key
	 *            DOCUMENT ME!
	 * @param data
	 *            DOCUMENT ME!
	 * @param handler
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Object setUserData(final String key, final Object data,
			final UserDataHandler handler) {
		if (userDataMap == null) {
			userDataMap = new HashMap();
		} // end if

		Object[] obj = (Object[]) userDataMap.put(key, new Object[] { data,
				handler });
		if (obj == null)
			return null;
		return obj[0];
	} // end setUserData()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#getUserData(java.lang.String)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param key
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public Object getUserData(final String key) {
		if (userDataMap == null) {
			return null;
		} // end if

		Object res[] = ((Object[]) userDataMap.get(key));

		if (res == null) {
			return null;
		} // end if

		return res[0];
	} // end getUserData()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#appendChild(org.w3c.dom.Node)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param newChild
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws DOMException
	 *             DOCUMENT ME!
	 */
	public Node appendChild(Node newChild) throws DOMException {
		boolean isBuildStage = false;
		if (ownerDocument != null) {
			isBuildStage = ownerDocument.isBuildStage;
		} else if (this.getNodeType() == Node.DOCUMENT_NODE) {
			isBuildStage = ((ADocument) this).isBuildStage;
		}
		if (isBuildStage) {
			((INode) newChild).setParent((CElement) this);
			if (listChild == null) {
				listChild = new CNodeList(false);
			} // end if
			listChild.addItem(newChild);
			return newChild;
		}
		isReadOnly();
		if (newChild.getParentNode() == this) {
			if (listChild != null) {
				listChild.removeItem(newChild);
			}
		} else if (newChild.getParentNode() != null) {
			if (getNodeType() == DOCUMENT_FRAGMENT_NODE) {
				newChild.getParentNode().removeChild(newChild);
			} else {
				throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
						"Cannot insert node here.");
			}
		}
		short nodeType = this.getNodeType();

		if ((newChild.getNodeType() == Node.ATTRIBUTE_NODE)
				&& (nodeType == Node.ELEMENT_NODE)) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					"Cannot insert node here.");
		} // end if
		else if ((nodeType == Node.DOCUMENT_NODE)
				|| (nodeType == Node.ELEMENT_NODE)
				|| (nodeType == Node.DOCUMENT_FRAGMENT_NODE)
				|| (nodeType == Node.ATTRIBUTE_NODE)
				|| (nodeType == Node.ENTITY_NODE)
				|| (nodeType == Node.ENTITY_REFERENCE_NODE)) {
			if ((((ANode) newChild).ownerDocument != ownerDocument)
					&& (ownerDocument != null)) {
				if (newChild.getNodeType() != Node.DOCUMENT_FRAGMENT_NODE)
					throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
							"The owner document of both node are different.");
			} // end if

			((INode) newChild).setParent((CElement) this);

			if (listChild == null) {
				listChild = new CNodeList(false);
			} // end if

			if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
				NodeList nl = newChild.getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					ANode n = (ANode) nl.item(i).cloneNode(true);
					n.setParent(null);
					appendChild(n);
				}
			} else {
				listChild.addItem(newChild);
			}

			return newChild;
		} // end else if
		else {
			return null;
		} // end else
	} // end appendChild()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#cloneNode(boolean)
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @param deep
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public final Node cloneNode(final boolean deep) {
		if (this.getNodeType() == Node.DOCUMENT_NODE) {
			ADocument doc = new CDom2HTMLDocument();
			doc.setImplementation(((ADocument) this).getImplementation());
			return _CloneNode(doc, this, deep, true);
		} // end if

		ANode result = (ANode) _CloneNode(this.ownerDocument, this, deep, true);
		if (this.userDataMap != null) {
			for (Iterator it = this.userDataMap.entrySet().iterator(); it
					.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String key = (String) entry.getKey();
				Object[] o = (Object[]) entry.getValue();
				if (o[1] != null) {
					UserDataHandler udh = (UserDataHandler) o[1];
					udh.handle(UserDataHandler.NODE_CLONED, key, o[0], this,
							result);
				}
			}
		}
		result.notifyNSChange(null);
		return result;
	} // end cloneNode()

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.Node#hasChildNodes()
	 */
	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public boolean hasChildNodes() {
		if (listChild == null) {
			return false;
		} // end if

		return listChild.getLength() > 0;
	} // end hasChildNodes()

	private static String getChildAsText(Node node) {
		StringBuffer result = new StringBuffer();
		//
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			result.append("<");
			result.append(node.getNodeName());
			result.append(" ");
			NamedNodeMap nnm = node.getAttributes();
			if (nnm != null) {
				for (int i = 0; i < nnm.getLength(); i++) {
					Attr attr = (Attr) nnm.item(i);
					result.append(attr.getName());
					result.append("=\"");
					result.append(CEntityCoDec.encode(attr.getValue(), true));
					result.append("\" ");
				}
			}
			if (node.getChildNodes().getLength() == 0) {
				result.append("/>");
				return result.toString();
			} else {
				result.append(">");
			}
		} else if (node.getNodeType() == Node.COMMENT_NODE) {
			result.append("<!-- ");
			result.append(node.getNodeValue());
			result.append(" -->");
			return result.toString();
		} else if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
			result.append("<![CDATA[ ");
			result.append(node.getNodeValue());
			result.append(" ]]>");
			return result.toString();
		} else if (node.getNodeType() == Node.TEXT_NODE) {
			result.append(CEntityCoDec.encode(node.getNodeValue()));
			return result.toString();
		} else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
			ProcessingInstruction pi = (ProcessingInstruction) node;
			result.append("<?");
			result.append(pi.getTarget());
			result.append(" ");
			result.append(pi.getData());
			result.append("?>");
			return result.toString();
		}
		NodeList nl = node.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			result.append(getChildAsText(nl.item(i)));
		}
		//
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			result.append("</");
			result.append(node.getNodeName());
			result.append(">");
		}
		return result.toString();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param document
	 *            DOCUMENT ME!
	 * @param node
	 *            DOCUMENT ME!
	 * @param deep
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	protected final Node _CloneNode(final ADocument document, final Node node,
			boolean deep, boolean importNotSpecified) {
		Node newNode = null;

		if (node.getNodeType() == Node.DOCUMENT_NODE) {
			if (!deep) {
				return document;
			} // end if

			newNode = document;
		} // end if
		else if (node.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
			newNode = document.createDocumentFragment();
		} // end else if
		else if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element oldElem = (Element) node;
			CElement newElem = new CElement(oldElem.getNodeName(), document);
			newElem.setNamespaceURI(oldElem.getNamespaceURI());
			newNode = newElem;

			NamedNodeMap nnm = oldElem.getAttributes();
			if (nnm != null) {
				for (int i = 0; i < nnm.getLength(); i++) {
					Attr oldAttr = (Attr) nnm.item(i);
					CAttr newAttr = (CAttr) _CloneNode(document, nnm.item(i),
							deep, importNotSpecified);
					newAttr.setNamespaceURI(oldAttr.getNamespaceURI());
					if (oldAttr.getClass() != newAttr.getClass()) {
						if ((importNotSpecified && newAttr.getSpecified() == false)
								|| (newAttr.getSpecified()))
							newElem.setAttributeNode(newAttr);
					} else if ((importNotSpecified && newAttr.getSpecified() == false)
							|| (newAttr.getSpecified() && !((CAttr) nnm.item(i))
									.isDefaults())
							|| (importNotSpecified && ((CAttr) nnm.item(i))
									.isDefaults()))
						newElem.setAttributeNode(newAttr);
				} // end for
			}
		} // end else if
		else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
			Attr oldAttr = (Attr) node;
			Attr newAttr = new CAttr(oldAttr.getName(), "", document, null,
					oldAttr.getSpecified(), oldAttr.getNamespaceURI());
			newNode = newAttr;
			deep = true;
		} // end else if
		else if (node.getNodeType() == Node.TEXT_NODE) {
			Text oldText = (Text) node;
			Text newText = new CText(oldText.getNodeValue(), document);
			newNode = newText;
		} // end else if
		else if (node.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
			EntityReference oldText = (EntityReference) node;
			EntityReference newText = new CEntityReference(oldText
					.getNodeName(), oldText.getNodeValue(), document);
			return newText;
		} // end else if
		else if (node.getNodeType() == Node.ENTITY_NODE) {
			Entity oldText = (Entity) node;
			String text = getChildAsText(oldText);
			Entity newText = new CEntity("!ENTITY " + oldText.getNodeName()
					+ " \"" + text + "\"", oldText.getNodeName(), oldText
					.getNotationName(), text, oldText.getPublicId(), oldText
					.getSystemId(), false, document);
			return newText;
		} // end else if
		else if (node.getNodeType() == Node.NOTATION_NODE) {
			Notation oldText = (Notation) node;
			if (oldText.getPublicId() != null) {
				Notation newText = new CNotation("!NOTATION "
						+ oldText.getNodeName() + " PUBLIC \""
						+ oldText.getPublicId() + "\" \""
						+ oldText.getSystemId() + "\"", oldText.getNodeName(),
						oldText.getPublicId(), oldText.getSystemId(), document);
				return newText;
			} else {
				Notation newText = new CNotation("!NOTATION "
						+ oldText.getNodeName() + " SYSTEM \""
						+ oldText.getSystemId() + "\"", oldText.getNodeName(),
						oldText.getPublicId(), oldText.getSystemId(), document);
				return newText;
			}
		} // end else if
		else if (node.getNodeType() == Node.COMMENT_NODE) {
			Comment oldComment = (Comment) node;
			Comment newComment = new CComment(oldComment.getNodeValue(),
					document);
			newNode = newComment;
		} // end else if
		else if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
			CDATASection newCData = new CCDATASection(node.getNodeValue(),
					document);
			newNode = newCData;
		} // end else if
		else if (node.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
			if (node.getClass() == CDocType.class) {
				try {
					CDocType o2 = (CDocType) ((CDocType) node).clone();
					o2.setOwnerDocument(document);
					o2.setParent(null);
					newNode = o2;
				} catch (CloneNotSupportedException e) {
				}
			} else {
				DocumentType old = (DocumentType) node;
				String pi = old.getPublicId();
				String si = old.getSystemId();
				String is = old.getInternalSubset();
				StringBuffer content = new StringBuffer();
				content.append("!DOCTYPE ");
				content.append(old.getName());
				if (pi != null) {
					content.append(" PUBLIC \"");
					content.append(pi);
					content.append("\" ");
					if (si != null) {
						content.append("\"");
						content.append(si);
						content.append("\" ");
					}
				} else if (si != null) {
					content.append(" SYSTEM \"");
					content.append(si);
					content.append("\" ");
				}
				if (is != null) {
					content.append("[\n");
					content.append(is);
					content.append("]");
				}
				CDocType dt = new CDocType(content.toString(), old.getName(),
						old.getPublicId(), old.getSystemId(), document);
				dt.isReadOnly = false;
				CNamedNodeMap dtent = (CNamedNodeMap) dt.getEntities();
				dtent.setFreeze(false);
				NamedNodeMap nnm = old.getEntities();
				for (int i = 0; i < nnm.getLength(); i++) {
					CEntity ent = (CEntity) _CloneNode(document, nnm.item(i),
							deep, importNotSpecified);
					dtent.setNamedItem(ent);
				}
				dtent.setFreeze(true);
				CNamedNodeMap dnot = (CNamedNodeMap) dt.getNotations();
				dnot.setFreeze(false);
				nnm = old.getNotations();
				for (int i = 0; i < nnm.getLength(); i++) {
					CNotation not = (CNotation) _CloneNode(document, nnm
							.item(i), deep, importNotSpecified);
					dnot.setNamedItem(not);
				}
				dnot.setFreeze(true);
				dt.isReadOnly = true;
				newNode = dt;
			}
		} // end else if
		else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
			ProcessingInstruction procOld = (ProcessingInstruction) node;
			ProcessingInstruction procNew = new CProcessingInstruction(procOld
					.getNodeName(), procOld.getData(), document);
			newNode = procNew;
		} // end else if

		if ((deep) && (newNode != null)) {
			NodeList nl = node.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node childNode = _CloneNode(document, nl.item(i), deep,
						importNotSpecified);
				if (((childNode.getNodeType() == Node.ELEMENT_NODE)
						|| (childNode.getNodeType() == Node.COMMENT_NODE)
						|| (childNode.getNodeType() == Node.CDATA_SECTION_NODE)
						|| (childNode.getNodeType() == Node.TEXT_NODE)
						|| (childNode.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
						|| (childNode.getNodeType() == Node.DOCUMENT_TYPE_NODE) || (childNode
						.getNodeType() == Node.ENTITY_REFERENCE_NODE))
						&& ((newNode.getNodeType() == Node.ELEMENT_NODE)
								|| (newNode.getNodeType() == Node.ATTRIBUTE_NODE)
								|| (newNode.getNodeType() == Node.ENTITY_REFERENCE_NODE)
								|| (newNode.getNodeType() == Node.DOCUMENT_NODE) || (newNode
								.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE))) {
					newNode.appendChild(childNode);
				} // end if
			} // end for
		} // end if

		if (node instanceof ANode && ((ANode) node).userDataMap != null) {
			for (Iterator it = ((ANode) node).userDataMap.entrySet().iterator(); it
					.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String key = (String) entry.getKey();
				Object[] o = (Object[]) entry.getValue();
				newNode.setUserData(key, o[0], (UserDataHandler) o[1]);
			}
		}
		// ((ANode)newNode).nsList = null;
		return newNode;
	} // end _CloneNode()

	// private List nsList = null;

	/**
	 * DOCUMENT ME!
	 * 
	 * @param elem
	 *            DOCUMENT ME!
	 * @param namespaceList
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	protected final List _GetNamespaceList(final CElement elem,
			List namespaceList) {
		/*
		 * if (this.nsList != null) { namespaceList.addAll(this.nsList); return
		 * namespaceList; }
		 */
		if (!namespaceList.contains(xmlnsdef)) {
			namespaceList.add(xmlnsdef);
		}
		if (elem == null)
			return namespaceList;
		boolean addedNs = false;
		CNamedNodeMap nnm = elem.listAttributes;
		if (nnm != null)
			for (int j = 0; j < nnm.count; j++) {
				CAttr attr = (CAttr) nnm.list[j];
				String ln = attr.localName;
				if ("xmlns".equals(attr.prefix) || "xmlns".equals(ln)) {
					String prefix = "xmlns".equals(ln) ? null : ln;
					CNamespace ns = null;
					if (attr.deferredValue != null) {
						ns = new CNamespace(prefix, attr.deferredValue);
					} else if (attr.cachedValue != null) {
						ns = new CNamespace(prefix, attr.cachedValue);
					} else {
						ns = new CNamespace(prefix, attr.getValue());
					}
					if (!namespaceList.contains(ns)) {
						addedNs = true;
						namespaceList.add(ns);
					}
				} // end if
			} // end for

		if (!addedNs && elem.nameSpace != null
				&& elem.getNodeType() == Node.ELEMENT_NODE) {
			CNamespace ns = new CNamespace(elem.prefix, elem.nameSpace);
			if (!namespaceList.contains(ns)) {
				namespaceList.add(ns);
			}
		}

		if (elem.parentNode != null) {
			_GetNamespaceList(elem.parentNode, namespaceList);
		} else if (elem.ownerDocument != null) {
			_GetNamespaceList(elem.ownerDocument, namespaceList);
		}
		/*
		 * if (ownerDocument != null && !ownerDocument.isBuildStage) {
		 * this.nsList = new ArrayList(namespaceList.size());
		 * this.nsList.addAll(namespaceList); }
		 */
		return namespaceList;
	} // end _GetNamespaceList()

	/**
	 * DOCUMENT ME!
	 * 
	 * @param node
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	protected final Node _GetNextSibling(final Node node) {
		if (listChild == null) {
			return null;
		} // end if

		for (int i = 0; i < listChild.getLength(); i++) {
			if ((node == listChild.item(i))
					&& (i < (listChild.getLength() - 1))) {
				Node n = listChild.item(i + 1);
				/*
				 * if (n.getNodeType() == Node.TEXT_NODE && (n.getNodeValue() ==
				 * null || "".equals(n.getNodeValue().trim()))) { int j = i+2;
				 * while (j<listChild.getLength()) { n = listChild.item(j); j++;
				 * if (n.getNodeType() == Node.TEXT_NODE && (n.getNodeValue() ==
				 * null || "".equals(n.getNodeValue().trim()))) { continue; }
				 * return n; } return null; }
				 */
				return n;
			} // end if

			if (node == listChild.item(i)) {
				break;
			} // end if
		} // end for

		return null;
	} // end _GetNextSibling()

	/**
	 * DOCUMENT ME!
	 * 
	 * @param node
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	protected final Node _GetPreviousSibling(final Node node) {
		if (listChild == null) {
			return null;
		} // end if

		for (int i = 0; i < listChild.getLength(); i++) {
			if ((node == listChild.item(i)) && (i > 0)) {
				Node n = listChild.item(i - 1);
				/*
				 * if (n.getNodeType() == Node.TEXT_NODE && (n.getNodeValue() ==
				 * null || "".equals(n.getNodeValue().trim()))) { int j = i-2;
				 * while (j>=0) { n = listChild.item(j); j--; if
				 * (n.getNodeType() == Node.TEXT_NODE && (n.getNodeValue() ==
				 * null || "".equals(n.getNodeValue().trim()))) { continue; }
				 * return n; } return null; }
				 */

				return n;
			} // end if

			if (node == listChild.item(i)) {
				break;
			} // end if
		} // end for

		return null;
	} // end _GetPreviousSibling()

	/**
	 * DOCUMENT ME!
	 * 
	 * @param elem
	 *            DOCUMENT ME!
	 * @param content
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	protected final String _GetTextContent(final Element elem,
			CStringBuilder content) {
		if (content == null) {
			content = new CStringBuilder();
		} // end if

		NodeList nl = elem.getChildNodes();

		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE
					|| node.getNodeType() == Node.ENTITY_REFERENCE_NODE
					|| node.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE
					|| node.getNodeType() == Node.ATTRIBUTE_NODE
					|| node.getNodeType() == Node.ENTITY_NODE) {
				String result = _GetTextContent((Element) node, null);
				if (result.trim().length() > 0)
					content.append(result);
			} // end if
			if (node.getNodeType() == Node.TEXT_NODE) {
				String value = node.getNodeValue();
				if (nl.getLength() > 1 && nl.getLength() < 4) {
					if (node.getNextSibling() == null) {
						String tmp = value.trim();
						if (tmp.length() > 0) {
							int endindex = tmp.length() + value.indexOf(tmp);
							value = value.substring(0, endindex);
						} else {
							value = "";
						}
					}
					if (node.getPreviousSibling() == null) {
						String tmp = value.trim();
						if (tmp.length() > 0) {
							int startindex = value.indexOf(tmp, 0);
							value = value.substring(startindex);
						} else {
							value = "";
						}
					}
				}
				content.append(value);
			}
			if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
				content.append(node.getNodeValue());
			} // end if
		} // end for

		return content.toString();
	} // end _GetTextContent()

	/**
	 * DOCUMENT ME!
	 * 
	 * @param newChild
	 *            DOCUMENT ME!
	 * @param refChild
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	protected final Node _InsertBefore(Node newChild, final Node refChild) {
		if (newChild.getParentNode() == this) {
			if (listChild != null) {
				listChild.removeItem(newChild);
			}
		} else if (newChild.getParentNode() != null) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					"Cannot insert node here.");
		}
		if (!(this instanceof CElement))
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					"Cannot insert node here.");
		if (newChild.getNodeType() == Node.ATTRIBUTE_NODE)
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					"Cannot insert node here.");
		if ((((ANode) newChild).ownerDocument != ownerDocument)
				&& (ownerDocument != null)) {
			if (newChild.getNodeType() != Node.DOCUMENT_FRAGMENT_NODE)
				throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
						"The owner document of both node are different.");
		} // end if
		if (newChild instanceof INode) {
			((INode) newChild).setParent((CElement) this);
		} // end if
		else {
			newChild = _CloneNode(ownerDocument, newChild, true, true);
			((INode) newChild).setParent((CElement) this);
		} // end else

		if (listChild == null) {
			listChild = new CNodeList(false);
		} // end if

		if (refChild == null) {
			return appendChild(newChild);
		}
		boolean inserted = false;
		for (int i = 0; i < listChild.getLength(); i++) {
			if (listChild.item(i).hashCode() == refChild.hashCode()) {
				if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
					NodeList nl = newChild.getChildNodes();
					for (int j = nl.getLength() - 1; j >= 0; j--) {
						ANode node = (ANode) nl.item(j).cloneNode(true);
						node.setParent((CElement) this);
						listChild.insertAt(i, node);
					}
				} else {
					listChild.insertAt(i, newChild);
				}
				inserted = true;
				break;
			} // end if
		} // end for
		if (!inserted)
			throw new DOMException(DOMException.NOT_FOUND_ERR,
					"The node was not found.");

		return newChild;
	} // end _InsertBefore()

	/**
	 * DOCUMENT ME!
	 * 
	 * @param namespaceList
	 *            DOCUMENT ME!
	 * @param ns
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	protected final boolean _NamespaceAlreadyKnown(final List namespaceList,
			final CNamespace ns) {
		return namespaceList == null ? false : namespaceList.contains(ns);
	} // end _NamespaceAlreadyKnown()

	private static boolean isChildOf(Node toTest, Node child) {
		NodeList nl = child.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) == toTest)
				return true;
			boolean result = isChildOf(toTest, nl.item(i));
			if (result)
				return true;
		}
		return false;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param newChild
	 *            DOCUMENT ME!
	 * @param oldChild
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	protected final Node _ReplaceChild(Node newChild, final Node oldChild) {
		if (newChild.getParentNode() != null
				&& newChild.getParentNode() != this) {
			if (getNodeType() == DOCUMENT_FRAGMENT_NODE) {
				newChild = newChild.cloneNode(true);
			} else {
				((ANode) newChild).isReadOnly();
				if (isChildOf(newChild, oldChild))
					throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
							"This operation is not supported.");
				else
					throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
							"Cannot insert node here.");
			}
		}
		if (!(this instanceof CElement))
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					"Cannot insert node here.");
		if (newChild.getNodeType() == Node.ATTRIBUTE_NODE)
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
					"Cannot insert node here.");
		if ((((ANode) newChild).ownerDocument != ownerDocument)
				&& (ownerDocument != null)) {
			if (newChild.getNodeType() != Node.DOCUMENT_FRAGMENT_NODE)
				throw new DOMException(DOMException.WRONG_DOCUMENT_ERR,
						"The owner document of both node are different.");
		} // end if
		// newChild = _CloneNode(ownerDocument, newChild, true);
		((INode) newChild).setParent((CElement) this);

		if (listChild == null) {
			return null;
		} // end if
		if (newChild != oldChild)
			listChild.removeItem(newChild);
		boolean found = false;
		for (int i = 0; i < listChild.getLength(); i++) {
			Node n = listChild.item(i);
			if (n == oldChild) {
				((ANode) n).setParent(null);
				if (newChild.getNodeType() == Node.DOCUMENT_FRAGMENT_NODE) {
					NodeList nl = newChild.getChildNodes();
					boolean first = true;
					for (int j = nl.getLength() - 1; j >= 0; j--) {
						ANode node = (ANode) nl.item(j).cloneNode(true);
						node.setParent((CElement) this);
						if (first) {
							listChild.replace(i, node);
							first = false;
						} else {
							listChild.insertAt(i, node);
						}
					}
				} else {
					if (newChild.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
						if (getNodeType() == Node.DOCUMENT_NODE) {
							((ADocument) this)
									.setDocumentType((DocumentType) newChild);
						}
					}
					listChild.replace(i, newChild);
				}
				found = true;
				break;
			} // end if
		} // end for

		if (!found)
			throw new DOMException(DOMException.NOT_FOUND_ERR,
					"This operation is not supported.");

		return oldChild;
	} // end _ReplaceChild()

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @author $author$
	 * @version $Revision: 1.38 $
	 */
	public static class CNamespace implements Serializable, Cloneable {
		static final long serialVersionUID = -8668741329790586102L;

		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}

		/** DOCUMENT ME! */
		final String namespaceURI;

		/** DOCUMENT ME! */
		final String prefix;
		final int hashcode;
		final static int emptyhc = "".intern().hashCode();
		final boolean isDefault;

		/**
		 * Creates a new CNamespace object.
		 * 
		 * @param prefix
		 *            DOCUMENT ME!
		 * @param namespaceURI
		 *            DOCUMENT ME!
		 */
		public CNamespace(final String prefix, final String namespaceURI) {
			this.prefix = prefix;
			this.namespaceURI = namespaceURI;
			if (prefix == null) {
				this.hashcode = emptyhc;
				this.isDefault = true;
			} else {
				this.hashcode = prefix.intern().hashCode();
				this.isDefault = false;
			}
		} // end CNamespace()

		/**
		 * DOCUMENT ME!
		 * 
		 * @return DOCUMENT ME!
		 */
		public boolean isDefault() {
			return isDefault;
		} // end isDefault()

		/**
		 * DOCUMENT ME!
		 * 
		 * @return DOCUMENT ME!
		 */
		public String getNamespaceURI() {
			return namespaceURI;
		} // end getNamespaceURI()

		/**
		 * DOCUMENT ME!
		 * 
		 * @return DOCUMENT ME!
		 */
		public String getPrefix() {
			return prefix;
		} // end getPrefix()

		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj instanceof CNamespace) {
				return ((CNamespace) obj).hashcode == hashcode;
			}
			return false;
		}

		public int hashCode() {
			return hashcode;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		/**
		 * DOCUMENT ME!
		 * 
		 * @return DOCUMENT ME!
		 */
		public String toString() {
			CStringBuilder buffer = new CStringBuilder();
			buffer.append("Namespace : ");
			buffer.append(getNamespaceURI());
			buffer.append(" Prefix : '");
			buffer.append((getPrefix() == null) ? "" : getPrefix());
			buffer.append("'");

			return buffer.toString();
		} // end toString()
	} // end CNamespace

	public boolean isIgnoreAll() {
		return ignoreAll;
	}

	public void setIgnoreAll(boolean ignoreAll) {
		this.ignoreAll = ignoreAll;
	}

} // end ANode
