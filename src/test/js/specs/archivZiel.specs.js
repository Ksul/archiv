/**
 * Created by m500288 on 19.02.15.
 */
describe("Test für ArchivZiel", function() {

    beforeEach(function () {
        REC.init();
    });

    it("test1", function() {
        var rules = ' <archivZiel type="my:archivContent" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var archivZiel = new ArchivZiel(new XMLObject(XMLDoc.docNode));
        erg = archivZiel.resolve(REC.currentDocument);
        expect("falsches Archiv Ziel", REC.currentDocument.isSubType("my:archivContent")).toBeTruthy();
    });

    it("test2",  function() {
        var rules = ' <archivZiel aspect="my:idable" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var archivZiel = new ArchivZiel(new XMLObject(XMLDoc.docNode));
        erg = archivZiel.resolve(REC.currentDocument);
        expect("falscher Aspect", REC.currentDocument.hasAspect("my:idable")).toBeTruthy();
    });

    it("test3", function() {
        var rules = ' <archivZiel type="my:archivContent" aspect="my:idable" />';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var archivZiel = new ArchivZiel(new XMLObject(XMLDoc.docNode));
        erg = archivZiel.resolve(REC.currentDocument);
        expect("falscher Aspect", REC.currentDocument.hasAspect("my:idable")).toBeTruthy();
        expect("falsches Archiv Ziel", REC.currentDocument.isSubType("my:archivContent")).toBeTruthy();
    });

});







