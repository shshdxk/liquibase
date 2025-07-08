package io.github.shshdxk.liquibase.parser.core.xml;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.change.ChangeFactory;
import io.github.shshdxk.liquibase.changelog.ChangeLogParameters;
import io.github.shshdxk.liquibase.changelog.DatabaseChangeLog;
import io.github.shshdxk.liquibase.logging.Logger;
import io.github.shshdxk.liquibase.parser.ChangeLogParserFactory;
import io.github.shshdxk.liquibase.parser.core.ParsedNode;
import io.github.shshdxk.liquibase.parser.core.ParsedNodeException;
import io.github.shshdxk.liquibase.precondition.PreconditionFactory;
import io.github.shshdxk.liquibase.resource.ResourceAccessor;
import io.github.shshdxk.liquibase.sql.visitor.SqlVisitorFactory;
import io.github.shshdxk.liquibase.util.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;

class XMLChangeLogSAXHandler extends DefaultHandler {

    private final ChangeFactory changeFactory;
    private final PreconditionFactory preconditionFactory;
    private final SqlVisitorFactory sqlVisitorFactory;
    private final ChangeLogParserFactory changeLogParserFactory;

    protected Logger log;

	private final DatabaseChangeLog databaseChangeLog;
	private final ResourceAccessor resourceAccessor;
	private final ChangeLogParameters changeLogParameters;
    private final Stack<ParsedNode> nodeStack = new Stack<>();
    private Stack<StringBuilder> textStack = new Stack<>();
    private ParsedNode databaseChangeLogTree;


    protected XMLChangeLogSAXHandler(String physicalChangeLogLocation, ResourceAccessor resourceAccessor, ChangeLogParameters changeLogParameters) {
		log = Scope.getCurrentScope().getLog(getClass());
		this.resourceAccessor = resourceAccessor;

		databaseChangeLog = new DatabaseChangeLog();
		databaseChangeLog.setPhysicalFilePath(physicalChangeLogLocation);
		databaseChangeLog.setChangeLogParameters(changeLogParameters);

        if (changeLogParameters == null) {
            this.changeLogParameters = new ChangeLogParameters();
        } else {
            this.changeLogParameters = changeLogParameters;
        }

        changeFactory = Scope.getCurrentScope().getSingleton(ChangeFactory.class);
        preconditionFactory = PreconditionFactory.getInstance();
        sqlVisitorFactory = SqlVisitorFactory.getInstance();
        changeLogParserFactory = ChangeLogParserFactory.getInstance();
    }

	public DatabaseChangeLog getDatabaseChangeLog() {
		return databaseChangeLog;
	}

    public ParsedNode getDatabaseChangeLogTree() {
        return databaseChangeLogTree;
    }

    @Override
    public void characters(char ch[], int start, int length) {
        textStack.peek().append(new String(ch, start, length));
    }


    @Override
    public void startElement(String uri, String localName, String qualifiedName, Attributes attributes) throws SAXException {
        ParsedNode node = new ParsedNode(null, localName);
        try {
            if (attributes != null) {
                for (int i=0; i< attributes.getLength(); i++) {
                    try {
                        node.addChild(null, attributes.getLocalName(i), attributes.getValue(i));
                    } catch (NullPointerException e) {
                        throw e;
                    }
                }
            }
            if (!nodeStack.isEmpty()) {
                nodeStack.peek().addChild(node);
            }
            if (nodeStack.isEmpty()) {
                databaseChangeLogTree = node;
            }
            nodeStack.push(node);
            textStack.push(new StringBuilder());
        } catch (ParsedNodeException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        ParsedNode node = nodeStack.pop();
        try {
            String seenText = this.textStack.pop().toString();
            if (!"".equals(StringUtil.trimToEmpty(seenText))) {
                node.setValue(seenText.trim());
            }
        } catch (ParsedNodeException e) {
            throw new SAXException(e);
        }
    }
}