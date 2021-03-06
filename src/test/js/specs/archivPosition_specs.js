/**
 * Created by m500288 on 18.02.15.
 */

describe("Test für ArchivPosition", function() {

    beforeEach(function () {
        REC.init();
        companyhome.init();
    });

    it("Test1", function () {
        var rules = ' <archivPosition folder="Dokumente/Auto/KFZ Steuern" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode), "System");
        erg = archivPosition.resolve();
        expect(erg.displayPath.split("/").slice(2).join("/") + "/" + erg.name).toBe("/Archiv/Dokumente/Auto/KFZ Steuern");
    });

    it("Test2", function () {
        var rules = ' <archivPosition folder="Dokumente/Auto/KFZ :Steuern" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode), "System");
        erg = archivPosition.resolve();
        expect(erg).not.toBeDefined();
        expect(REC.errors[0]).toBe("invalid characters for a foldername\n/Archiv/Dokumente/Auto/KFZ :Steuern\nPosition 27:\n:\n");
    });

    it("Test3", function () {
        var rules = '<searchItem name="tmp2" fix="Test"  />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode), "System");
        REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
        rules = ' <archivPosition folder="Dokumente/Rechnungen/Sonstige Rechnungen/{tmp2}"/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode), "System");
        erg = archivPosition.resolve();
        expect(erg.displayPath.split("/").slice(2).join("/") + "/" + erg.name).toBe("/Archiv/Dokumente/Rechnungen/Sonstige Rechnungen/Test");
    });

    it("Test4", function () {
        var rules = ' <archivPosition folder="Dokumente/Rechnungen/Sonstige Rechnungen/{tmp2}"/>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode), "System");
        erg = archivPosition.resolve();
        expect(erg).not.toBeDefined();
        expect(REC.errors[0]).toBe("Could not replace Placeholder {tmp2}!");
    });

});

