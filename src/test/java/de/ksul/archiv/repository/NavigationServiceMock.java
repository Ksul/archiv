package de.ksul.archiv.repository;

import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.FolderType;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.chemistry.opencmis.commons.spi.NavigationService;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 04.07.18
 * Time: 10:23
 */
public class NavigationServiceMock {

    private NavigationService navigationService;
    private Repository repository;

    public NavigationServiceMock() {

    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public NavigationService getNavigationService() {
        if (navigationService == null)
            navigationService = getMock();
        return navigationService;
    }

    private NavigationService getMock() {

        NavigationService navigationService = mock(NavigationService.class);
        when(navigationService.getFolderParent(anyString(), anyString(), anyString(), any())).then(new Answer<ObjectData>() {
            public ObjectData answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String id = (String) args[1];
                FileableCmisObject result = null;
                FileableCmisObject cmisObject = repository.getParent(id);
                if (cmisObject != null)
                    return MockUtils.getInstance().getObjectDataFromProperties(cmisObject.getProperties());
                else
                    return null;
            }
        });
        when(navigationService.getObjectParents(anyString(), anyString(), anyString(), anyBoolean(), any(IncludeRelationships.class), any(), anyBoolean(), any())).then(new Answer<List<ObjectParentData>>() {
            public List<ObjectParentData> answer(InvocationOnMock invocation) throws Throwable {
                List<ObjectParentData> result = new ArrayList<>();
                FileableCmisObject cmisObject = repository.getById((String) invocation.getArguments()[1]);
                FileableCmisObject parentObject = repository.getParent((String) invocation.getArguments()[1]);
                ObjectParentDataImpl objectData = new ObjectParentDataImpl();
                objectData.setObject(MockUtils.getInstance().getObjectDataFromProperties(parentObject.getProperties()));
                if (cmisObject.getType() instanceof FolderType)
                    objectData.setRelativePathSegment(parentObject.getPropertyValue(PropertyIds.PATH));
                else
                    objectData.setRelativePathSegment(cmisObject.getName());

                result.add(objectData);
                return result;
            }
        });
        when(navigationService.getChildren(anyString(), anyString(), any(), anyString(), anyBoolean(), any(IncludeRelationships.class), anyString(), anyBoolean(), any(BigInteger.class), any(BigInteger.class), any())).then(new Answer<ObjectInFolderList>() {
            public ObjectInFolderList answer(InvocationOnMock invocation) throws Throwable {
                ObjectInFolderListImpl objectInFolderList = new ObjectInFolderListImpl();
                BigInteger skip = (BigInteger) invocation.getArguments()[9];
                BigInteger maxItems = (BigInteger) invocation.getArguments()[8];
                List<ObjectInFolderData> folderDatas = new ArrayList<>();
                List<FileableCmisObject> results = repository.getChildren((String) invocation.getArguments()[1], ((BigInteger) invocation.getArguments()[9]).intValue(), ((BigInteger) invocation.getArguments()[8]).intValue());
                for (FileableCmisObject cmisObject : results) {
                    ObjectInFolderDataImpl objectInFolderData = new ObjectInFolderDataImpl();
                    objectInFolderData.setObject(MockUtils.getInstance().getObjectDataFromProperties(cmisObject.getProperties()));
                    objectInFolderData.setPathSegment(cmisObject.getPropertyValue(PropertyIds.PATH));
                    folderDatas.add(objectInFolderData);
                }
                if (invocation.getArguments()[3] != null) {
                    String[] order = new String[2];
                    String[] sortColumns = invocation.getArguments()[3].toString().split(",");
                    Collections.sort(folderDatas, new Comparator<ObjectInFolderData>() {
                        @Override
                        public int compare(ObjectInFolderData o1, ObjectInFolderData o2) {
                            int ret = 0;
                            for (int j = 0; j < sortColumns.length; j++) {
                                String[] column = sortColumns[j].trim().split(" ");
                                order[0] = column[0];
                                order[1] = column.length > 1 ? !column[1].isEmpty() ? column[1] : "ASC" : "ASC";
                                Comparable valA = null, valB = null;
                                for (int i = 0; i < o1.getObject().getProperties().getPropertyList().size(); i++) {
                                    if (((AbstractPropertyData) o1.getObject().getProperties().getPropertyList().get(i)).getId().equalsIgnoreCase(order[0])) {
                                        valA = (Comparable) o1.getObject().getProperties().getPropertyList().get(i).getFirstValue();
                                        break;
                                    }
                                }
                                for (int i = 0; i < o2.getObject().getProperties().getPropertyList().size(); i++) {
                                    if (((AbstractPropertyData) o2.getObject().getProperties().getPropertyList().get(i)).getId().equalsIgnoreCase(order[0])) {
                                        valB = (Comparable) o2.getObject().getProperties().getPropertyList().get(i).getFirstValue();
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
                objectInFolderList.setObjects(folderDatas.subList(skip.intValue(), skip.intValue() + maxItems.intValue() > folderDatas.size() ? folderDatas.size() : skip.intValue() + maxItems.intValue()));
                objectInFolderList.setNumItems(BigInteger.valueOf(folderDatas.size()));
                objectInFolderList.setHasMoreItems(skip.intValue() + maxItems.intValue() > folderDatas.size() ? false : true);
                return objectInFolderList;
            }
        });
        return navigationService;
    }
}
