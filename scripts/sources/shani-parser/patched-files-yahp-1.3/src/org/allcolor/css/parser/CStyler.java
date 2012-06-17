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
package org.allcolor.css.parser;
import org.allcolor.xml.parser.CStringBuilder;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.ElementCSSInlineStyle;
import org.w3c.dom.stylesheets.StyleSheetList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


/**
 * DOCUMENT ME!
 *
 * @author Quentin Anciaux
 */
public class CStyler {
	/** DOCUMENT ME! */
	public final static String STYLE_HOVER = "hover";

	/** DOCUMENT ME! */
	public final static String STYLE_FOCUS = "focus";

	/** DOCUMENT ME! */
	public final static String STYLE_ACTIVE = "active";

	/** DOCUMENT ME! */
	public final static String STYLE_LINK = "link";

	/** DOCUMENT ME! */
	public final static String STYLE_VISITED = "visited";

	private List styles = new ArrayList();
	
	public CStyler(StyleSheetList cssList) {
		for (int i = 0; i < cssList.getLength(); i++) {
			Object stylesheet = cssList.item(i);

			if (stylesheet instanceof CSSStyleSheet) {
				CSSStyleSheet css = (CSSStyleSheet) stylesheet;
				parseSelectors(css,styles);
			} // end if
		} // end for
	}
	
	private static class CStyle {
		private CSSStyleDeclaration decl;
		private CElement element;
		public CStyle(CElement element,CSSStyleDeclaration decl) {
			this.element = element;
			this.decl = decl;
		}
	}
	
	private void parseSelectors(CSSStyleSheet css,List result) {
		CSSRuleList ruleList = css.getCssRules();
		
		for (int j = 0; j < ruleList.getLength(); j++) {
			CSSRule rule = ruleList.item(j);

			if (rule == null) {
				continue;
			}

			if (rule.getType() == CSSRule.STYLE_RULE) {
				CSSStyleRule	    cssRule = (CSSStyleRule) rule;
				CSSStyleDeclaration decl = cssRule.getStyle();
				StringTokenizer selectorsToken = new StringTokenizer(cssRule.getSelectorText(),",", false);
				while (selectorsToken.hasMoreTokens()) {
					String   selector = selectorsToken.nextToken().trim();
					CElement element = getElements(selector);
					if (element != null) {
						result.add(new CStyle(element,decl));
					}
				}
			} // end if
			else if (rule.getType() == CSSRule.IMPORT_RULE) {
				CSSStyleSheet importCSS = ((CSSImportRule) rule).getStyleSheet();
				parseSelectors(importCSS,result);
			} // end else if
		}
	}
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param elem DOCUMENT ME!
	 * @param cssList DOCUMENT ME!
	 * @param pseudoElt DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public CSSStyleDeclaration getStyle(
		final Element  elem,
		final String pseudoClass) {
		List matchedRule = new ArrayList(1);
		for (int i = 0;i<styles.size();i++) {
			CStyle style = (CStyle)styles.get(i);
			if (match(elem,style.element,pseudoClass)) {
				matchedRule.add(new CMatchedRule(style.decl,calculateSpecificity(style.element),matchedRule.size()));
			}
		}
		
		if (elem instanceof ElementCSSInlineStyle) {
			CSSStyleDeclaration decl = ((ElementCSSInlineStyle) elem).getStyle();
			if (decl != null) {
				matchedRule.add(new CMatchedRule(decl, 1000000,
						matchedRule.size()));
			}
		} // end if

		matchedRule = sortRule(matchedRule);

		return getStyle(matchedRule);
	} // end getStyle()

	public CSSStyleDeclaration getStyle(
		final String  selector) {
		try {
			List matchedRule = new ArrayList(1);
			for (int i = 0;i<styles.size();i++) {
				CStyle style = (CStyle)styles.get(i);
				if (match(selector,style.element)) {
					matchedRule.add(new CMatchedRule(style.decl,calculateSpecificity(style.element),matchedRule.size()));
				}
			}
		
			matchedRule = sortRule(matchedRule);

			return getStyle(matchedRule);
		}
		catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	} // end getStyle()
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param selector DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private static CElement getElements(String selector) {
		selector = selector.trim();

		StringTokenizer elementTokenizer = new StringTokenizer(selector,
				" .#[]>:+", true);
		List		    selTokenizerList = new ArrayList();

		while (elementTokenizer.hasMoreTokens()) {
			selTokenizerList.add(elementTokenizer.nextToken());
		} // end while

		List     modifierList   = new ArrayList();
		boolean  isDescendant   = false;
		boolean  isChild	    = false;
		boolean  isSibling	    = false;
		CElement currentElement = null;
		CElement rootElement    = null;
		int		 i			    = selTokenizerList.size() - 1;

		while (i >= 0) {
			String token = (String) selTokenizerList.get(i);

			//System.err.println("new tk "+token);
			if ("]".equals(token)) {
				CStringBuilder modifier = new CStringBuilder();

				if ((i - 2) >= 0) {
					i--;
					token = (String) selTokenizerList.get(i);

					while ((i >= 0) && (!token.equals("["))) {
						modifier.append(token);
						i--;
						token = (String) selTokenizerList.get(i);
					} // end while

					modifierList.add(modifier.toString());
				} // end if
				else {

					break;
				}

				i--;
			} // end if
			else if ((":".equals(token)) ||
					(".".equals(token)) ||
					("#".equals(token))) {
				String modifier = (String) selTokenizerList.get(i + 1);

				if ((i - 1) >= 0) {
					String tk = (String) selTokenizerList.get(i - 1);

					if (tk.equals(" ")) {
						CElement element = new CElement("*");
						element.addModifier(token + modifier);

						if (currentElement != null) {
							if (isDescendant) {
								currentElement.setDescendantOf(element);
								currentElement = element;
							} // end if
							else if (isChild) {
								currentElement.setChildOf(element);
								currentElement = element;
							} // end else if
							else if (isSibling) {
								currentElement.setSiblingOf(element);
								currentElement = element;
							} // end else if
						} // end if
						else {
							currentElement     = element;
							rootElement		   = element;
						} // end else
					} // end if
					else {
						modifierList.add(token + modifier);
					} // end else
				} // end if
				else {
					modifierList.add(token + modifier);
				} // end else

				i--;
			} // end else if
			else if (" ".equals(token)) {
				if ((currentElement != null) &&
						(!isSibling) &&
						(!isChild)) {
					isDescendant = true;
				} // end if

				i--;
			} // end else if
			else if ("+".equals(token)) {
				if (currentElement != null) {
					isSibling		 = true;
					isDescendant     = false;
					isChild			 = false;
				} // end if

				i--;
			} // end else if
			else if (">".equals(token)) {
				if (currentElement != null) {
					isChild			 = true;
					isSibling		 = false;
					isDescendant     = false;
				} // end if

				i--;
			} // end else if
			else {
				if ((i - 1) > 0) {
					String prevToken = (String) selTokenizerList.get(i -
							1);

					if ((prevToken.equals(".")) ||
							(prevToken.equals(":")) ||
							(prevToken.equals("#"))) {
						i--;

						continue;
					} // end if
				} // end if

				CElement element = new CElement(token);

				if ((i - 1) == 0) {
					String prevToken = (String) selTokenizerList.get(i -
							1);

					if ((prevToken.equals(".")) ||
							(prevToken.equals(":")) ||
							(prevToken.equals("#"))) {
						//System.err.println("new element * "+prevToken+token);
						element = new CElement("*");
						element.addModifier(prevToken + token);
					} // end if
				} // end if
				else {
					//System.err.println("new element "+token);
				} // end else

				if (currentElement != null) {
					if (isDescendant) {
						currentElement.setDescendantOf(element);
						currentElement = element;
					} // end if
					else if (isChild) {
						currentElement.setChildOf(element);
						currentElement = element;
					} // end else if
					else if (isSibling) {
						currentElement.setSiblingOf(element);
						currentElement = element;
					} // end else if
				} // end if
				else {
					currentElement     = element;
					rootElement		   = element;
				} // end else

				Iterator it = modifierList.iterator();

				while (it.hasNext()) {
					currentElement.addModifier((String) it.next());
				} // end while

				isDescendant     = false;
				isChild			 = false;
				isSibling		 = false;
				modifierList.clear();
				i--;
			} // end else
		} // end while

		return rootElement;
	} // end getElements()

	/**
	 * DOCUMENT ME!
	 *
	 * @param elem DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private static boolean isFirstChild(final Element elem) {
		Node parent = elem.getParentNode();

		if (parent != null) {
			NodeList list = parent.getChildNodes();

			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {
					return node.hashCode() == elem.hashCode();
				} // end if
			} // end for
		} // end if

		return false;
	} // end isFirstChild()

	/**
	 * DOCUMENT ME!
	 *
	 * @param matchedRule DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private static CSSStyleDeclaration getStyle(final List matchedRule) {
		CSSStyleDeclaration style = new CCSSStyleDeclaration(null);
		Iterator		    it = matchedRule.iterator();

		while (it.hasNext()) {
			CSSStyleDeclaration decl = ((CMatchedRule) it.next()).decl;

			for (int k = 0; k < decl.getLength(); k++) {
				String property    = decl.item(k);
				String priorityNew = decl.getPropertyPriority(property);
				String priorityOld = style.getPropertyPriority(property);

				if ((priorityOld == null) ||
						("important".equals(priorityNew))) {
					style.setProperty(property,
						decl.getPropertyValue(property), priorityNew);
				}
			} // end for
		} // end while

		return style;
	} // end getStyle()

	/**
	 * DOCUMENT ME!
	 *
	 * @param attributeName DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 * @param elem DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private static boolean attributeSpListContains(
		final String attributeName,
		final String value,
		final Element elem) {
		String		    attrVal   = elem.getAttribute(attributeName);
		StringTokenizer tokenizer = new StringTokenizer(attrVal, " ",
				false);

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();

			if (token.equals(value)) {
				return true;
			}
		} // end while

		return false;
	} // end attributeSpListContains()

	/**
	 * DOCUMENT ME!
	 *
	 * @param attributeName DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 * @param elem DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private static boolean attributeStartWith(
		final String attributeName,
		final String value,
		final Element elem) {
		String attrVal = elem.getAttribute(attributeName);

		return attrVal.startsWith(value);
	} // end attributeStartWith()

	/**
	 * DOCUMENT ME!
	 *
	 * @param rule DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private static int calculateSpecificity(final CElement rule) {
		return 0;
	} // end calculateSpecificity()

	/**
	 * DOCUMENT ME!
	 *
	 * @param attributeName DOCUMENT ME!
	 * @param elem DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private static boolean hasAttributeSet(
		final String attributeName,
		final Element elem) {
		return !(elem.getAttribute(attributeName).equals(""));
	} // end hasAttributeSet()

	/**
	 * DOCUMENT ME!
	 *
	 * @param attributeName DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 * @param elem DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private static boolean hasAttributeSetTo(
		final String attributeName,
		final String value,
		final Element elem) {
		return elem.getAttribute(attributeName).equals(value);
	} // end hasAttributeSetTo()

	/**
	 * DOCUMENT ME!
	 *
	 * @param sClass DOCUMENT ME!
	 * @param elem DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private static boolean hasClassSetTo(
		final String sClass,
		final Element elem) {
		String sclasses = elem.getAttribute("class");
		String [] classes = sclasses.split("\\s");
		for (int i=0;i<classes.length;i++) {
			boolean isset = classes[i].equals(sClass);
			if (isset) return true;
		}
		return false;
	} // end hasClassSetTo()

	private static boolean hasClassSetTo(
		final String sClass,
		final String sclasses) {
		String [] classes = sclasses.split("\\s");
		for (int i=0;i<classes.length;i++) {
			boolean isset = classes[i].equals(sClass);
			if (isset) return true;
		}
		return false;
	} // end hasClassSetTo()
	
	/**
	 * DOCUMENT ME!
	 *
	 * @param id DOCUMENT ME!
	 * @param elem DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private static boolean hasIdSetTo(
		final String id,
		final Element elem) {
		return hasAttributeSetTo("id", id, elem);
	} // end hasIdSetTo()

	/**
	 * DOCUMENT ME!
	 *
	 * @param curDomElem DOCUMENT ME!
	 * @param curElem DOCUMENT ME!
	 * @param pseudoClass DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private static boolean match(
		Element curDomElem,
		CElement curElem,
		final String pseudoClass) {
		boolean booMatch	  = true;
		boolean wasDescendant = false;

		// try to match
		while ((curElem != null) && (curDomElem != null)) {
			if ((curElem.getName()
							.equalsIgnoreCase(curDomElem.getNodeName())) ||
					(curElem.getName().equals("*"))) {
				// match modifier
				List     modifierList = curElem.getModifier();
				Iterator it = modifierList.iterator();

				while (it.hasNext()) {
					String modifier = (String) it.next();

					if (modifier.startsWith(":")) {
						if (modifier.equals(":firstchild")) {
							if (!isFirstChild(curDomElem)) {
								booMatch = false;

								break;
							} // end if
						} // end if
						else if ((pseudoClass != null) &&
								(!modifier.equals(pseudoClass))) {
							booMatch = false;

							break;
						} // end else if
						else if (pseudoClass == null) {
							booMatch = false;

							break;
						} // end else if
					} // end if
					else if (modifier.startsWith("#")) {
						String id = modifier.substring(1);

						if (!hasIdSetTo(id, curDomElem)) {
							booMatch = false;

							if (!curElem.getName().equals("*")) {
								break;
							}
						} // end if
					} // end else if
					else if (modifier.startsWith(".")) {
						String className = modifier.substring(1);

						if (!hasClassSetTo(className, curDomElem)) {
							booMatch = false;

							if (!curElem.getName().equals("*")) {
								break;
							}
						} // end if
					} // end else if
					else if (modifier.indexOf("~=") != -1) {
						String attrName = modifier.substring(0,
								modifier.indexOf("~="));
						String value = modifier.substring(modifier.indexOf(
									"=") + 1);

						if (value.startsWith("\"")) {
							value = value.substring(1);
						} // end if

						if (value.endsWith("\"")) {
							value = value.substring(0,
									value.length() - 1);
						} // end if

						if (!attributeSpListContains(attrName, value,
									curDomElem)) {
							booMatch = false;

							if (!curElem.getName().equals("*")) {
								break;
							}
						} // end if
					} // end else if
					else if (modifier.indexOf("|=") != -1) {
						String attrName = modifier.substring(0,
								modifier.indexOf("|="));
						String value = modifier.substring(modifier.indexOf(
									"=") + 1);

						if (value.startsWith("\"")) {
							value = value.substring(1);
						} // end if

						if (value.endsWith("\"")) {
							value = value.substring(0,
									value.length() - 1);
						} // end if

						if (!attributeStartWith(attrName, value,
									curDomElem)) {
							booMatch = false;

							if (!curElem.getName().equals("*")) {
								break;
							}
						} // end if
					} // end else if
					else if (modifier.indexOf("=") != -1) {
						String attrName = modifier.substring(0,
								modifier.indexOf("="));
						String value = modifier.substring(modifier.indexOf(
									"=") + 1);

						if (value.startsWith("\"")) {
							value = value.substring(1);
						} // end if

						if (value.endsWith("\"")) {
							value = value.substring(0,
									value.length() - 1);
						} // end if

						if (!hasAttributeSetTo(attrName, value,
									curDomElem)) {
							booMatch = false;

							if (!curElem.getName().equals("*")) {
								break;
							}
						} // end if
					} // end else if
					else {
						String attrName = modifier;

						if (!hasAttributeSet(attrName, curDomElem)) {
							booMatch = false;

							if (!curElem.getName().equals("*")) {
								break;
							}
						} // end if
					} // end else
				} // end while

				if ((!booMatch) && (!curElem.getName().equals("*"))) {
					break;
				}

				if ((!booMatch) && (curElem.getName().equals("*"))) {
					if (!wasDescendant) {
						break;
					}

					curDomElem     = (Element) curDomElem.getParentNode();
					booMatch	   = true;

					continue;
				} // end if
				else if (curElem.isChildOf() != null) {
					wasDescendant     = false;
					curElem			  = curElem.isChildOf();
					curDomElem		  = (Element) curDomElem.getParentNode();

					if ((curDomElem != null) &&
							(curElem != null) &&
							(curElem.getName()
										.equalsIgnoreCase(curDomElem.getNodeName()) ||
							curElem.getName().equalsIgnoreCase("*"))) {
						continue;
					} // end if

					booMatch = false;

					break;
				} // end else if
				else if (curElem.isDescendantOf() != null) {
					wasDescendant     = true;
					curElem			  = curElem.isDescendantOf();
					curDomElem		  = (Element) curDomElem.getParentNode();

					while ((curDomElem != null) &&
							(curElem != null) &&
							(!curElem.getName()
										 .equalsIgnoreCase(curDomElem.getNodeName()) &&
							!curElem.getName().equalsIgnoreCase("*"))) {
						curDomElem = (Element) curDomElem.getParentNode();
					} // end while

					if (curDomElem == null) {
						booMatch = false;

						break;
					} // end if
				} // end else if
				else if (curElem.isSiblingOf() != null) {
					wasDescendant     = false;
					curElem			  = curElem.isSiblingOf();

					Node node		  = curDomElem;

					while ((node.getPreviousSibling() != null) &&
							(node.getPreviousSibling().getNodeType() != Node.ELEMENT_NODE)) {
						node = node.getPreviousSibling();
					} // end while

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						curDomElem = (Element) node;
					} else {
						curDomElem = null;
					}

					if ((curDomElem != null) &&
							(curElem != null) &&
							(curElem.getName()
										.equalsIgnoreCase(curDomElem.getNodeName()) ||
							curElem.getName().equalsIgnoreCase("*"))) {
						continue;
					} // end if

					booMatch = false;

					break;
				} // end else if
				else {

					break;
				}
			} // end if
			else {
				booMatch = false;

				break;
			} // end else
		} // end while

		return booMatch;
	} // end match()
	
	private static boolean match(
			String selector,
			CElement curElem) {
			boolean booMatch	  = true;

			// try to match
			if ((curElem != null) && (selector != null)) {
				// match modifier
				List     modifierList = curElem.getModifier();
				Iterator it = modifierList.iterator();

				while (it.hasNext()) {
					String modifier = (String) it.next();
					if (modifier.startsWith(".")) {
						String className = modifier.substring(1);
						if (!hasClassSetTo(className, selector.substring(1))) {
							booMatch = false;
						} else {
							booMatch = true;
							break;
						}
					} else {
						booMatch = false;
					}
				} // end while
			} // end while
			return booMatch;
		} // end match()	

	/**
	 * DOCUMENT ME!
	 *
	 * @param matchedRule DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private static List sortRule(final List matchedRule) {
		Object array[] = matchedRule.toArray();
		Arrays.sort(array, new CRuleComparator());

		return Arrays.asList(array);
	} // end sortRule()

	/**
	 * DOCUMENT ME!
	 *
	 * @author $author$
	 * @version $Revision: 1.13 $
	 */
	public static class CElement {
		/** DOCUMENT ME! */
		private CElement child = null;

		/** DOCUMENT ME! */
		private CElement descendant = null;

		/** DOCUMENT ME! */
		private CElement sibling = null;

		/** DOCUMENT ME! */
		private List modifierList = new ArrayList();

		/** DOCUMENT ME! */
		private String name;

		/**
		 * Creates a new CElement object.
		 *
		 * @param name DOCUMENT ME!
		 */
		public CElement(final String name) {
			this.name = name;
		} // end CElement()

		/**
		 * DOCUMENT ME!
		 *
		 * @param element DOCUMENT ME!
		 */
		public void setChildOf(final CElement element) {
			this.child = element;
		} // end setChildOf()

		/**
		 * DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		public CElement isChildOf() {
			return child;
		} // end isChildOf()

		/**
		 * DOCUMENT ME!
		 *
		 * @param element DOCUMENT ME!
		 */
		public void setDescendantOf(final CElement element) {
			this.descendant = element;
		} // end setDescendantOf()

		/**
		 * DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		public CElement isDescendantOf() {
			return descendant;
		} // end isDescendantOf()

		/**
		 * DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		public List getModifier() {
			return modifierList;
		} // end getModifier()

		/**
		 * DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		public String getName() {
			return this.name;
		} // end getName()

		/**
		 * DOCUMENT ME!
		 *
		 * @param element DOCUMENT ME!
		 */
		public void setSiblingOf(final CElement element) {
			this.sibling = element;
		} // end setSiblingOf()

		/**
		 * DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		public CElement isSiblingOf() {
			return sibling;
		} // end isSiblingOf()

		/**
		 * DOCUMENT ME!
		 *
		 * @param modifier DOCUMENT ME!
		 */
		public void addModifier(final String modifier) {
			modifierList.add(modifier);
		} // end addModifier()

		/**
		 * DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(getName());

			for (Iterator it = getModifier().iterator(); it.hasNext();) {
				buffer.append(it.next());
			} // end for

			buffer.append("\n");

			if (descendant != null) {
				buffer.append("descendant :" + descendant.toString());
			} // end if

			if (child != null) {
				buffer.append("child :" + child.toString());
			} // end if

			if (sibling != null) {
				buffer.append("sibling :" + sibling.toString());
			} // end if

			return buffer.toString();
		} // end toString()
	} // end CElement

	/**
	 * DOCUMENT ME!
	 *
	 * @author $author$
	 * @version $Revision: 1.13 $
	 */
	private static class CMatchedRule {
		/** DOCUMENT ME! */
		CSSStyleDeclaration decl;

		/** DOCUMENT ME! */
		int order = 0;

		/** DOCUMENT ME! */
		int specificity = 0;

		/**
		 * Creates a new CMatchedRule object.
		 *
		 * @param decl DOCUMENT ME!
		 * @param specificity DOCUMENT ME!
		 * @param order DOCUMENT ME!
		 */
		public CMatchedRule(
			final CSSStyleDeclaration decl,
			final int			specificity,
			final int			order) {
			this.decl			 = decl;
			this.specificity     = specificity;
			this.order			 = order;
		} // end CMatchedRule()
	} // end CMatchedRule

	/**
	 * DOCUMENT ME!
	 *
	 * @author $author$
	 * @version $Revision: 1.13 $
	 */
	private static class CRuleComparator
		implements Comparator {
		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		/**
		 * DOCUMENT ME!
		 *
		 * @param arg0 DOCUMENT ME!
		 * @param arg1 DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		public int compare(
			final Object arg0,
			final Object arg1) {
			CMatchedRule rule1 = (CMatchedRule) arg0;
			CMatchedRule rule2 = (CMatchedRule) arg1;

			if (rule2.order >= rule1.order) {
				return rule2.specificity - rule1.specificity;
			}

			return rule1.specificity - rule2.specificity;
		} // end compare()
	} // end CRuleComparator
} // end CStyler
