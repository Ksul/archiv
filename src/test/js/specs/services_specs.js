/**
 * Created by m500288 on 21.11.16.
 */

describe("Test für die Rest Services", function() {

    it("getNodeId", function () {
    var json =   erg = executeService("getNodeId", null, [
        {"name": "filePath", "value": "/Datenverzeichnis/Skripte/doc.xml"}
    ], null, true);

        //expect(json.success).toBe(true);
    });

});
