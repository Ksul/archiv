/**
 * Created by m500288 on 21.11.16.
 */

describe("Test für die Rest Services", function() {

    it("getNodeId", function () {
    var json =   erg = executeService({"name": "getNodeId", "url": "http://localhost:8080/Archiv", "ignoreError": true},  [
        {"name": "filePath", "value": "/Archiv/Unbekannt"}
    ]);

        expect(json.success).toBe(true);
    });

});
