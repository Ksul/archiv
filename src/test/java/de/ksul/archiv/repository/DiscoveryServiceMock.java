package de.ksul.archiv.repository;

import de.ksul.archiv.PDFConnector;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.runtime.SessionImpl;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.spi.DiscoveryService;
import org.apache.commons.io.IOUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.07.18
 * Time: 10:20
 */
public class DiscoveryServiceMock {

    private DiscoveryService discoveryService;
    private Repository repository;

    public DiscoveryServiceMock() {
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public DiscoveryService getDiscoveryService() {
        if (discoveryService == null)
            discoveryService = getMock();
        return discoveryService;
    }

    private DiscoveryService getMock() {
        DiscoveryService discoveryService = mock(DiscoveryService.class);
        when(discoveryService.query(any(String.class), any(String.class), any(Boolean.class), any(Boolean.class), any(IncludeRelationships.class), any(String.class), any(BigInteger.class), any(BigInteger.class), any())).then(new Answer<ObjectList>() {

            class ObjectTypeHelper {
                boolean isObjectType(FileableCmisObject cmisObject, String objectTypeId){
                    if (objectTypeId == null)
                        return true;
                    ObjectType objectType = cmisObject.getType();
                    do {
                        if (objectType.getId().contains(objectTypeId))
                            return true;
                        objectType = objectType.getParentType();
                    } while(objectType != null);
                    return false;
                }

                boolean isObjectInSearch(FileableCmisObject cmisObject, List<String> contains) {
                    if (contains == null || contains.isEmpty())
                        return true;
                    else {

                        for (String x: contains){
                            List<String> liste = Arrays.asList(new StringBuilder(x).reverse().toString().split(":", 2)).stream().map(element -> new StringBuilder(element).reverse().toString()).collect(Collectors.toList());
                            Collections.reverse(liste);
                            if (!liste.get(0).contains("TEXT") && cmisObject.getPropertyValue(liste.get(0)).toString().contains(liste.get(1)))
                                return true;
                            if (liste.get(0).contains("TEXT") && cmisObject instanceof Document &&  ((Document) cmisObject).getContentStream() != null )   {
                                byte[] content = new byte[0];
                                try {
                                    content = IOUtils.toByteArray(((Document) cmisObject).getContentStream().getStream());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                if ( ((Document) cmisObject).getContentStream().getMimeType().contains("text/plain") && new String(content).contains(liste.get(1)))
                                    return true;
                                if ( ((Document) cmisObject).getContentStream().getMimeType().contains("application/pdf")) {
                                    PDFConnector con = new PDFConnector();
                                    if (con.pdftoText(new ByteArrayInputStream(content)).contains(liste.get(1)))
                                        return true;
                                }
                            }
                        }
                    }

                    return false;
                }
            }

            public ObjectList answer(InvocationOnMock invocation) throws Throwable {
                int typen = 0;
                int i = 0;
                ObjectTypeHelper helper = new ObjectTypeHelper();
                List<FileableCmisObject> liste;
                List<String> contains = new ArrayList<>();
                ObjectListImpl objectList = new ObjectListImpl();
                Object[] args = invocation.getArguments();
                String statement = (String) args[1];
                BigInteger skip = (BigInteger) args[7];
                BigInteger maxItems = (BigInteger) args[6];
                List<ObjectData> list = new ArrayList<>();
                final String search = statement.substring(statement.indexOf("'") + 1, statement.indexOf("'", statement.indexOf("'") + 1));
                String typ = null;
                List stmt = Arrays.asList(statement.split(" "));
                Iterator<String> stmtIt = stmt.iterator();
                while (stmtIt.hasNext()) {
                    String part = stmtIt.next();
                    if (part.contains("CONTAINS"))
                        contains.add(stmtIt.next().replaceAll("[']|[)]|[)]|[*]", ""));
                    if (part.contains("TEXT:"))
                        contains.add(part.replaceAll("[']|[)]|[)]|[*]", ""));
                }

                if (statement.contains("from ")) {
                    typ = (String) stmt.get(stmt.indexOf("from") + 1);
                }

                if (statement.contains("IN_FOLDER")) {
                    liste = repository.getChildren(search);
                } else if (statement.contains("IN_TREE")) {
                    liste = repository.getChildrenForAllLevels(search);
                } else {
                    liste = repository.query(search);
                }

                for (FileableCmisObject cmisObject : liste) {

                    if (helper.isObjectType(cmisObject, typ) && helper.isObjectInSearch(cmisObject, contains))
                        list.add(MockUtils.getInstance().getObjectDataFromProperties(cmisObject.getProperties()));
                }

                if (statement.contains("ORDER BY")) {
                    String[] order = new String[2];
                    String parts = statement.substring(statement.indexOf("ORDER BY") + 9);
                    final String[] sortColumns = parts.split(",");
                    Collections.sort(list, new Comparator<ObjectData>() {
                        @Override
                        public int compare(ObjectData o1, ObjectData o2) {
                            int ret = 0;
                            for (int j = 0; j < sortColumns.length; j++) {
                                String[] column = sortColumns[j].trim().split(" ");
                                order[0] = column[0].substring(1, 2).equals(".") ? column[0].substring(2) : column[0];
                                order[1] = column.length > 1 ? !column[1].isEmpty() ? column[1] : "ASC" : "ASC";
                                Comparable valA = null, valB = null;
                                for (int i = 0; i < o1.getProperties().getPropertyList().size(); i++) {
                                    if (((AbstractPropertyData) o1.getProperties().getPropertyList().get(i)).getId().equalsIgnoreCase(order[0])) {
                                        valA = (Comparable) o1.getProperties().getPropertyList().get(i).getFirstValue();
                                        break;
                                    }
                                }
                                for (int i = 0; i < o2.getProperties().getPropertyList().size(); i++) {
                                    if (((AbstractPropertyData) o2.getProperties().getPropertyList().get(i)).getId().equalsIgnoreCase(order[0])) {
                                        valB = (Comparable) o2.getProperties().getPropertyList().get(i).getFirstValue();
                                        break;
                                    }
                                }
                                if (valA != null && valB != null)
                                    ret = valA.compareTo(valB) * (order[1].equalsIgnoreCase("ASC") ? 1 : -1);
                                else if (valA == null && valB == null)
                                    ret = 0;
                                else if (valA == null)
                                    ret = order[1].equalsIgnoreCase("ASC") ? 1 : 1;
                                else if (valB == null)
                                    ret = order[1].equalsIgnoreCase("ASC") ? -1 : -1;
                                if (ret != 0)
                                    break;
                            }
                            return ret;
                        }
                    });
                }
                objectList.setObjects(list.subList(skip.intValue(), skip.intValue() + maxItems.intValue() > list.size() ? list.size() : skip.intValue() + maxItems.intValue()));
                objectList.setNumItems(BigInteger.valueOf(list.size()));
                objectList.setHasMoreItems(skip.intValue() + maxItems.intValue() > list.size() ? false : true);
                return objectList;
            }
        });
        return discoveryService;
    }
}
