/**
 * Created by m500288 on 21.11.16.
 */

describe("Test für die Rest Services", function () {

    beforeEach(function () {

        var json = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true}, [
            {"name": "filePath", "value": "/TestFolder"}
        ]);

        if (json.success) {

            json = executeService({
                "name": "deleteFolder",
                "ignoreError": true,
                "errorMessage": "Ordner konnte nicht gelöscht werden!"
            }, [
                {"name": "documentId", "value": json.data.objectId}
            ]);
        }

    });

    it("getNodeId", function () {
        var json = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true}, [
            {"name": "filePath", "value": "/Datenverzeichnis/Skripte"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data).not.toBe(null);
        expect(json.data.objectId).not.toBe(null);
        expect(json.data.objectID).not.toBe(null);
    });


    it("createFolder", function () {

        var json = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true}, [
            {"name": "filePath", "value": "/"}
        ]);

            expect(json.success).toBe(true);
            expect(json.data).not.toBe(null);
            expect(json.data.objectId).not.toBe(null);
            expect(json.data.objectID).not.toBe(null);

            var extraProperties = {
                "cmis:folder": {"cmis:name": "TestFolder"},
                "P:cm:titled": {"cm:title": "TestFolder", "cm:description": "Ordner für Tests"}
            };
            json = executeService({"name": "createFolder"}, [
                {"name": "documentId", "value": json.data.objectID},
                {"name": "extraProperties", "value": extraProperties}
            ]);

            expect(json.success).toBe(true);
            expect(json.data).not.toBe(null);
            expect(json.data.objectId).not.toBe(null);

            json = executeService({
                "name": "deleteFolder",
                "ignoreError": true,
                "errorMessage": "Ordner konnte nicht gelöscht werden!"
            }, [
                {"name": "documentId", "value": json.data.objectId}
            ]);

            expect(json.success).toBe(true);

    });


    it("createDocument", function () {

        var json = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true}, [
            {"name": "filePath", "value": "/"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data).not.toBe(null);
        expect(json.data.objectId).not.toBe(null);
        expect(json.data.objectID).not.toBe(null);

        var extraProperties = {
            "cmis:folder": {"cmis:name": "TestFolder"},
            "P:cm:titled": {"cm:title": "TestFolder", "cm:description": "Ordner für Tests"}
        };
        json = executeService({"name": "createFolder"}, [
            {"name": "documentId", "value": json.data.objectID},
            {"name": "extraProperties", "value": extraProperties}
        ]);

        expect(json.success).toBe(true);
        expect(json.data).not.toBe(null);
        expect(json.data.objectId).not.toBe(null);

        var folderId = json.data.objectId;

        extraProperties = {
            'P:cm:titled': {
                'cm:title': "Test",
                'cm:description': ""
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
            {"name": "fileName", "value": "Test.txt"},
            {"name": "content", "value": "abcde"},
            {"name": "extraProperties", "value": extraProperties},
            {"name": "mimeType", "value": "application/pdf"},
            {"name": "versionState", "value": "major"}
        ]);
        expect(json.success).toBe(true);
        expect(json.data.name).toBe("Test.txt");
        expect(json.data.versionLabel).toBe("1.0");

        json = executeService({
            "name": "deleteFolder",
            "ignoreError": true,
            "errorMessage": "Ordner konnte nicht gelöscht werden!"
        }, [
            {"name": "documentId", "value": folderId}
        ]);

        expect(json.success).toBe(true);

    });

    it("listFolderWithPagination", function () {


        var json = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true}, [
            {"name": "filePath", "value": "/"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data).not.toBe(null);
        expect(json.data.objectId).not.toBe(null);
        expect(json.data.objectID).not.toBe(null);

        var extraProperties = {
            "cmis:folder": {"cmis:name": "TestFolder"},
            "P:cm:titled": {"cm:title": "TestFolder", "cm:description": "Ordner für Tests"}
        };
        json = executeService({"name": "createFolder"}, [
            {"name": "documentId", "value": json.data.objectID},
            {"name": "extraProperties", "value": extraProperties}
        ]);

        expect(json.success).toBe(true);
        expect(json.data).not.toBe(null);
        expect(json.data.objectId).not.toBe(null);

        extraProperties = {
            'P:cm:titled': {
                'cm:title': "Test",
                'cm:description': ""
            },
            'D:my:archivContent': {
                'my:documentDate': "",
                'my:person': "Klaus"
            },
            'P:my:amountable': {'my:amount': "", "my:tax": ""},
            'P:my:idable': {'my:idvalue': ""}
        };


        var folderId = json.data.objectId;

        json = executeService({
            "name": "createDocument",
            "errorMessage": "Dokument konnte nicht erstellt werden!",
            "ignoreError": true
        }, [
            {"name": "documentId", "value": folderId},
            {"name": "fileName", "value": "Test.txt"},
            {"name": "content", "value": "abcde"},
            {"name": "extraProperties", "value": extraProperties},
            {"name": "mimeType", "value": "application/pdf"},
            {"name": "versionState", "value": "major"}
        ]);
        expect(json.success).toBe(true);

        json = executeService({
            "name": "listFolderWithPagination",
            "url": "http://localhost:8080/Archiv",
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
            "url": "http://localhost:8080/Archiv",
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
            "url": "http://localhost:8080/Archiv",
            "ignoreError": true
        }, [
            {"name": "folderId", "value": folderId},
            {"name": "withFolder", "value": "-1"},
            {"name": "start", "value": "0"},
            {"name": "length", "value": "7"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length === 0).toBeTruthy();

        json = executeService({
            "name": "deleteFolder",
            "ignoreError": true,
            "errorMessage": "Ordner konnte nicht gelöscht werden!"
        }, [
            {"name": "documentId", "value": folderId}
        ]);

        expect(json.success).toBe(true);
    });

    it("getDocumentContent", function () {

        var json = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true}, [
            {"name": "filePath", "value": "/Datenverzeichnis/Skripte/doc.xml"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data).not.toBe(null);
        expect(json.data.objectId).not.toBe(null);
        expect(json.data.objectID).not.toBe(null);

        json = executeService({
            "name": "getDocumentContent",
            "url": "http://localhost:8080/Archiv",
            "ignoreError": true
        }, [
            {"name": "documentId", "value": json.data.objectId}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length > 0).toBeTruthy();
    });

    it("updateDocument", function () {

        var json = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true}, [
            {"name": "filePath", "value": "/Datenverzeichnis/Skripte/doc.xml"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data).not.toBe(null);
        expect(json.data.objectId).not.toBe(null);
        expect(json.data.objectID).not.toBe(null);

        var rulesID = json.data.objectID;
        json = executeService({
            "name": "getDocumentContent",
            "url": "http://localhost:8080/Archiv",
            "ignoreError": true
        }, [
            {"name": "documentId", "value": rulesID}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length > 0).toBeTruthy();

        json = executeService({"name": "updateDocument",   "url": "http://localhost:8080/Archiv",
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
