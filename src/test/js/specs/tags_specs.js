/**
 * Created by m500288 on 23.02.15.
 */

describe("Test für Tags", function() {

    beforeEach(function () {
        REC.init();
        var iBox = companyhome.childByNamePath("/Archiv/Inbox");
        REC.currentDocument = iBox.createNode("WebScriptTest", "my:archivContent");
    });

    it("tagTest", function() {
        var rules = '<tags name="Rückrechnung"/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var tags = new Tags(new XMLObject(XMLDoc.docNode));
        tags.resolve(0, REC.currentDocument);
        expect(REC.currentDocument.hasTag("Rückrechnung")).toBeTruthy();
    });
});


