/**
 * Created by m500288 on 21.11.16.
 */

describe("Test für die Rest Services", function() {

    it("getNodeId", function () {
    var json =   erg = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true},  [
        {"name": "filePath", "value": "/Archiv/Unbekannt"}
    ]);

        expect(json.success).toBe(true);
        expect(json.data.length > 0).toBeTruthy();
    });

    it("listFolderWithPagination", function () {

        var json =   erg = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true},  [
            {"name": "filePath", "value": "/Archiv/Unbekannt"}
        ]);

        expect(json.success).toBe(true);
        expect(json.data.length > 0).toBeTruthy();

        json =   erg = executeService({"name": "listFolderWithPagination", "url": "http://localhost:8080/Archiv", "ignoreError": true},  [
            {"name": "filePath", "value": json.data},
            {"name": "withFolder", "value": "1"},
            {"name": "itemsToSkip", "value": "0"},
            {"name": "length", "value": "7"}
        ]);

        expect(json.success).toBe(true);
        console.log(json.duration);
    });

});
