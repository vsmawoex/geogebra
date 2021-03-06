package org.geogebra.common.util;

/**
 * Builds HTML code of string with indices.
 */
public class IndexHTMLBuilder {
	private StringBuilder sb;
	private boolean needsTag;

	/**
	 * @param addTag
	 *            whether to add &lt;html&gt; tag around
	 */
	public IndexHTMLBuilder(boolean addTag) {
		this.sb = new StringBuilder();
		if (addTag) {
			this.needsTag = true;
			sb.append("<html>");
		}
	}

	public void append(String s) {
		sb.append(s);
	}

	public void startIndex() {
		sb.append("<sub><font size=\"-1\">");
	}

	public void endIndex() {
		sb.append("</font></sub>");
	}

	@Override
	public String toString() {

		if (needsTag) {
			needsTag = false;
			sb.append("</html>");
		}
		return sb.toString();
	}

	public void clear() {
		sb.setLength(needsTag ? "<html>".length() : 0);
	}

	public boolean canAppendRawHtml() {
		return true;
	}

	public void appendHTML(String str) {
		sb.append(StringUtil.toHTMLString(str));
	}
}
