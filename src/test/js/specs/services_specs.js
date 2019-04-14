/**
 * Created by m500288 on 21.11.16.
 */

function Sleep(milliseconds) {
    return new Promise(resolve => setTimeout(resolve, milliseconds));
}

function buildDocument(name, title, description, folderId) {

    var extraProperties = {
        'P:cm:titled': {
            'cm:title': title,
            'cm:description': description
        },
        'D:my:archivContent': {
            'my:documentDate': "",
            'my:person': "Klaus"
        },
        'P:my:amountable': {'my:amount': "", "my:tax": ""},
        'P:my:idable': {'my:idvalue': ""}
    };

    json = executeService({
        "name": "createDocument",
        "errorMessage": "Dokument konnte nicht erstellt werden!",
        "ignoreError": true
    }, [
        {"name": "documentId", "value": folderId},
        {"name": "fileName", "value": name},
        {"name": "content", "value": "abcde", "type": "byte"},
        {"name": "extraProperties", "value": extraProperties},
        {"name": "mimeType", "value": "text/plain"},
        {"name": "versionState", "value": "major"}
    ]);
    expect(json.success).toBe(true);
    expect(json.data.name).toBe("Test.txt");
    expect(json.data.versionLabel).toBe("1.0");

    return json;
}

function buildTestFolder(name, description, folder) {

    var json = executeService({
        "name": "getNodeId",
        "ignoreError": true}, [
        {"name": "filePath", "value": folder}
    ]);

    expect(json.success).toBe(true);
    expect(json.data).not.toBe(null);
    expect(json.data.objectId).not.toBe(null);
    expect(json.data.objectID).not.toBe(null);

    var extraProperties = {
        "cmis:folder": {"cmis:name": name},
        "P:cm:titled": {"cm:title": name, "cm:description": description}
    };
    json = executeService({
        "name": "createFolder"}, [
        {"name": "documentId", "value": json.data.objectID},
        {"name": "extraProperties", "value": extraProperties}
    ]);

    expect(json.success).toBe(true);
    expect(json.data).not.toBe(null);
    expect(json.data.objectId).not.toBe(null);

    return json;
}

describe("Test für die Rest Services", function () {

    beforeEach(function () {

        var json = executeService({
            "name": "getNodeId",
            "ignoreError": true}, [
            {"name": "filePath", "value": "/TestFolder"}
        ]);

        if (json.success) {

            var ids = [];
            ids.push(json.data.objectID);
            json = executeService({
                "name": "deleteFolder",
                "ignoreError": true,
                "errorMessage": "Ordner konnte nicht gelöscht werden!"
            }, [
                {"name": "documentId", "value": ids}
            ]);
        }

    });

    it("getNodeId", function () {
        var json = executeService({
            "name": "getNodeId",
            "ignoreError": true}, [
            {"name": "filePath", "value": "/"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data).not.toBe(null);
        expect(json.data.objectId).not.toBe(null);
        expect(json.data.objectID).not.toBe(null);
    });


    it("createFolder", function () {

        var json = buildTestFolder("TestFolder", "Ordner für Tests", "/");

            var ids = [];
            ids.push(json.data.objectId);
            json = executeService({
                "name": "deleteFolder",
                "ignoreError": true,
                "errorMessage": "Ordner konnte nicht gelöscht werden!"
            }, [
                {"name": "documentId", "value": ids}
            ]);

            expect(json.success).toBe(true);

    });


    it("createDocument", function () {

        var json = buildTestFolder("TestFolder", "Ordner für Tests", "/");

        var folderId = json.data.objectID;

        json = buildDocument("Test.txt", "Test", "", folderId);

        var ids = [];
        ids.push(folderId);
        json = executeService({
            "name": "deleteFolder",
            "ignoreError": true,
            "errorMessage": "Ordner konnte nicht gelöscht werden!"
        }, [
            {"name": "documentId", "value": ids}
        ]);

        expect(json.success).toBe(true);

    });

    it("listFolderWithPagination", function () {

        var json = buildTestFolder("TestFolder", "Ordner für Tests", "/");
        var folderId = json.data.objectID;
        json = buildDocument("Test.txt", "Test", "", folderId);

        json = executeService({
            "name": "listFolderWithPagination",
            "ignoreError": true
        }, [
            {"name": "folderId", "value": folderId},
            {"name": "withFolder", "value": "1"},
            {"name": "start", "value": "0"},
            {"name": "length", "value": "7"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length > 0).toBeTruthy();

        json = executeService({
            "name": "listFolderWithPagination",
            "ignoreError": true
        }, [
            {"name": "folderId", "value": folderId},
            {"name": "withFolder", "value": "0"},
            {"name": "start", "value": "0"},
            {"name": "length", "value": "7"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length > 0).toBeTruthy();

        json = executeService({
            "name": "listFolderWithPagination",
            "ignoreError": true
        }, [
            {"name": "folderId", "value": folderId},
            {"name": "withFolder", "value": "-1"},
            {"name": "start", "value": "0"},
            {"name": "length", "value": "7"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length === 0).toBeTruthy();

        var ids = [];
        ids.push(folderId);
        json = executeService({
            "name": "deleteFolder",
            "ignoreError": true,
            "errorMessage": "Ordner konnte nicht gelöscht werden!"
        }, [
            {"name": "documentId", "value": ids}
        ]);

        expect(json.success).toBe(true);
    });

    it("getDocumentContent", function () {

        var json = buildTestFolder("TestFolder", "Ordner für Tests", "/");
        var folderId = json.data.objectID;
        json = buildDocument("Test.txt", "Test", "", folderId);
        var result = getDocumentContent(json.data.objectId, true);

        expect(stringFromUTF8Array(result)).toBe("abcde");
    });

    it("updateDocument", function () {

        var json = buildTestFolder("TestFolder", "Ordner für Tests", "/");
        var folderId = json.data.objectID;
        json = buildDocument("Test.txt", "Test", "", folderId);

        var rulesID = json.data.objectID;
        json = executeService({
            "name": "getDocumentContent",
            "ignoreError": true
        }, [
            {"name": "documentId", "value": rulesID}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length > 0).toBeTruthy();

        json = executeService({
            "name": "updateDocument",
            "ignoreError": true}, [
            {"name": "documentId", "value": rulesID},
            {"name": "content", "value": json.data, "type": "byte"},
            {"name": "mimeType", "value": "text/xml"},
            {"name": "extraProperties", "value": {}},
            {"name": "versionState", "value": "minor"},
            {"name": "versionComment", "value": ""}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.objectID).toBe(rulesID);

    });
    
});
