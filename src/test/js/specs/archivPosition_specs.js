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
        var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode));
        erg = archivPosition.resolve();
        expect(erg.displayPath.split("/").slice(2).join("/") + "/" + erg.name).toBe("/Archiv/Dokumente/Auto/KFZ Steuern");
    });

    it("Test2", function () {
        var rules = ' <archivPosition folder="Dokumente/Auto/KFZ :Steuern" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode));
        erg = archivPosition.resolve();
        expect(erg).not.toBeDefined();
        expect(REC.errors[0]).toBe("Ung\ufffdtige Zeichen f\ufffdr Foldernamen!\n/Archiv/Dokumente/Auto/KFZ :Steuern\nPosition 27:\n:\n");
    });

    it("Test3", function () {
        var rules = '<searchItem name="tmp2" fix="Test"  />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var searchItem = new SearchItem(new XMLObject(XMLDoc.docNode));
        REC.currentSearchItems = REC.currentSearchItems.concat(searchItem);
        rules = ' <archivPosition folder="Dokumente/Rechnungen/Sonstige Rechnungen/{tmp2}">';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode));
        erg = archivPosition.resolve();
        expect(erg.displayPath.split("/").slice(2).join("/") + "/" + erg.name).toBe("/Archiv/Dokumente/Rechnungen/Sonstige Rechnungen/Test");
    });

    it("Test4", function () {
        var rules = ' <archivPosition folder="Dokumente/Rechnungen/Sonstige Rechnungen/{tmp2}">';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var archivPosition = new ArchivPosition(new XMLObject(XMLDoc.docNode));
        erg = archivPosition.resolve();
        expect(erg).not.toBeDefined();
        expect(REC.errors[1]).toBe("Variabel konnte nicht im Foldernamen ersetzt werden!\n");
    });

    it("testResolveFolder1", function () {
        var archivPosition = new ArchivPosition({});
        var newFolder = archivPosition.resolveFolder("/aa/bb/cc");
        expect(newFolder).not.toBeNull();
        expect(companyhome.childByNamePath("aa")).not.toBeNull();
        expect(companyhome.childByNamePath("aa/bb")).not.toBeNull();
        expect(companyhome.childByNamePath("aa/bb/cc")).not.toBeNull();
    });

    it("testResolveFolder2", function () {
        companyhome.createFolder("aa");
        var archivPosition = new ArchivPosition({});
        var newFolder = archivPosition.resolveFolder("/aa/bb/cc");
        expect(newFolder).not.toBeNull();
        expect(companyhome.childByNamePath("aa")).not.toBeNull();
        expect(companyhome.childByNamePath("aa/bb")).not.toBeNull();
        expect(companyhome.childByNamePath("aa/bb/cc")).not.toBeNull();
    });

});

