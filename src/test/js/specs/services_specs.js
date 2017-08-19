/**
 * Created by m500288 on 21.11.16.
 */

describe("Test für die Rest Services", function () {

    it("getNodeId", function () {
        var json = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true}, [
            {"name": "filePath", "value": "/Archiv/Unbekannt"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length > 0).toBeTruthy();
    });

    it("listFolderWithPagination", function () {

        var json = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true}, [
            {"name": "filePath", "value": "/Archiv"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length > 0).toBeTruthy();

        json = executeService({
            "name": "listFolderWithPagination",
            "url": "http://localhost:8080/Archiv",
            "ignoreError": true
        }, [
            {"name": "folderId", "value": json.data},
            {"name": "withFolder", "value": "-1"},
            {"name": "itemsToSkip", "value": "0"},
            {"name": "length", "value": "7"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length > 0).toBeTruthy();
        console.log(JSON.stringify(json));
    });

    it("getDocumentContent", function () {

        var json = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true}, [
            {"name": "filePath", "value": "/Datenverzeichnis/Skripte/doc.xml"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length > 0).toBeTruthy();

        json = executeService({
            "name": "getDocumentContent",
            "url": "http://localhost:8080/Archiv",
            "ignoreError": true
        }, [
            {"name": "documentId", "value": json.data},
            {"name": "extract", "value": "false"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length > 0).toBeTruthy();
    });

    it("updateDocument", function () {

        var json = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true}, [
            {"name": "filePath", "value": "/Datenverzeichnis/Skripte/doc.xml"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length > 0).toBeTruthy();

        var rulesID = json.data;
        json = executeService({
            "name": "getDocumentContent",
            "url": "http://localhost:8080/Archiv",
            "ignoreError": true
        }, [
            {"name": "documentId", "value": rulesID},
            {"name": "extract", "value": "false"}
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
        expect(json.data.length > 0).toBeTruthy();

    });

});
