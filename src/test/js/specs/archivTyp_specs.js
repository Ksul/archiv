/**
 * Created by m500288 on 23.02.15.
 */


describe("Test für ArchivTyp", function() {

    beforeEach(function () {
        REC.init();
        var iBox = companyhome.childByNamePath("/Archiv/Inbox");
        REC.currentDocument = iBox.createNode("WebScriptTest", "my:archivContent");
        REC.currentDocument.setProperty("cm:title", "Test Title");
        REC.currentDocument.setProperty("my:person", "Klaus");
        REC.currentDocument.properties.content.write("Test");
        search.setFind(false);
    });



    it("testWithNestedArchivZielWithLink", function() {
        REC.currentDocument.removeProperty("my:person");
        REC.currentDocument.properties.content.write("ZAUBERFRAU Rechnung Test");
        var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">'+
            ' <archivPosition folder="Dokumente/Rechnungen/{fol}/{tmp}"/> ' +
            ' <archivPosition link="true" folder="Dokumente/Rechnungen/Sonstige Rechnungen/{tmp}">' +
            '    <archivZiel type="my:archivFolder" />' +
            '</archivPosition>' +
            '<searchItem name="person" fix="Test" target="my:person" />' +
            '<searchItem name="id" fix="99.233.620.0" target="my:idvalue"/>' +
            ' <archivTyp name="Rechnung Zauberfrau" searchString="Rechnung">' +
            ' <archivZiel type="my:archivContent" /> ' +
            ' <archivZiel type="my:archivFolder" /> ' +
            ' <searchItem name="fol" fix="Rechnungen Zauberfrau" /> ' +
            ' <searchItem name="tmp" fix="2015" />' +
            ' </archivTyp>' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        console.log(Logger.getMessages(true));
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest")).not.toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Sonstige Rechnungen/2015/WebScriptTest")).not.toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.properties["my:idvalue"]).toBe("99.233.620.0");
        expect(doc.properties["my:person"]).toBe("Test");
    });

    it("testWithSearchInDifferentLevel", function() {
        REC.currentDocument.properties.content.write("Bezügemitteilung Personal ab: 05.05.2005");
        var rules = '<archivTyp name="Bezügemitteilung" searchString="Bezügemitteilung">\n' +
            '        <archivZiel type="my:archivContent" />\n' +
            '        <archivPosition folder="Dokumente/Gehalt/Landesamt für Besoldung/Bezügemitteilungen/{tmp}">\n' +
            '            <archivZiel type="my:archivFolder" />\n' +
            '        </archivPosition>' +
            '            <searchItem name="tmp" objectTyp="date" value="datum">\n' +
            '                <format formatString="YYYY" />\n' +
            '            </searchItem>\n' +
            '            <archivTyp name="Bezügemitteilung neu" searchString="Behörde">\n' +
            '                <searchItem name="datum" text="Westfalen" word="1" objectTyp="date" target="my:documentDate">\n' +
            '                    <check lowerValue="01/01/2000" upperValue="01/01/2020" />\n' +
            '                </searchItem>\n' +
            '            </archivTyp>\n' +
            '            <archivTyp name="Bezügemitteilung alt" searchString="Personal">\n' +
            '                <searchItem name="datum" text="ab:" removeBlanks="after" objectTyp="date" target="my:documentDate">\n' +
            '                    <check lowerValue="01/01/2000" upperValue="01/01/2020" />\n' +
            '                </searchItem>\n' +
              '            </archivTyp>\n' +
            '        </archivTyp>\n' +
            '    </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        expect(companyhome.childByNamePath("/Archiv/Dokumente/Gehalt/Landesamt für Besoldung/Bezügemitteilungen/2005/WebScriptTest")).not.toBeNull();
     });

    it("testWithSearchInDifferentLevel1", function() {
        REC.currentDocument.properties.content.write("Bezügemitteilung Behörde Westfalen: 05.05.2006");
        var rules = '<archivTyp name="Bezügemitteilung" searchString="Bezügemitteilung">\n' +
            '        <archivZiel type="my:archivContent" />\n' +
            '        <archivPosition folder="Dokumente/Gehalt/Landesamt für Besoldung/Bezügemitteilungen/{tmp}">\n' +
            '            <archivZiel type="my:archivFolder" />\n' +
            '        </archivPosition>' +
            '            <searchItem name="tmp" objectTyp="date" value="datum">\n' +
            '                <format formatString="YYYY" />\n' +
            '            </searchItem>\n' +
            '            <archivTyp name="Bezügemitteilung neu" searchString="Behörde">\n' +
            '                <searchItem name="datum" text="Westfalen" word="1" objectTyp="date" target="my:documentDate">\n' +
            '                    <check lowerValue="01/01/2000" upperValue="01/01/2020" />\n' +
            '                </searchItem>\n' +
            '            </archivTyp>\n' +
            '            <archivTyp name="Bezügemitteilung alt" searchString="Personal">\n' +
            '                <searchItem name="datum" text="ab:" removeBlanks="after" objectTyp="date" target="my:documentDate">\n' +
            '                    <check lowerValue="01/01/2000" upperValue="01/01/2020" />\n' +
            '                </searchItem>\n' +
            '            </archivTyp>\n' +
            '        </archivTyp>\n' +
            '    </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        expect(companyhome.childByNamePath("/Archiv/Dokumente/Gehalt/Landesamt für Besoldung/Bezügemitteilungen/2006/WebScriptTest")).not.toBeNull();
        console.log(Logger.getMessages(true));
    });


    it("testWithRegularExpression", function() {
        REC.currentDocument.removeProperty("my:person");
        REC.currentDocument.properties.content.write("ZAUBERFRAU Rechnung Test");
        var rules = ' <archivTyp name="Zauberfrau" searchString="(?=.*ZAUBERFRAU)(?=.*Rechnung)">'+
            ' <archivPosition folder="Dokumente/Rechnungen"/> ' +
            '<searchItem name="person" fix="Test" target="my:person" />' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        console.log(Logger.getMessages(true));
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/WebScriptTest")).not.toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.properties["my:person"]).toBe("Test");
    });

    it("testWithNestedArchivZielWithSearchItems", function() {
        REC.currentDocument.removeProperty("my:person");
        REC.currentDocument.properties.content.write("ZAUBERFRAU Rechnung Test");
        var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">'+
            ' <archivPosition folder="Dokumente/Rechnungen/{fol}/{tmp}"/> ' +
            '<searchItem name="person" fix="Test" target="my:person" />' +
            '<searchItem name="id" fix="99.233.620.0" target="my:idvalue"/>' +
            ' <archivTyp name="Rechnung Zauberfrau" searchString="Rechnung">' +
            ' <archivZiel type="my:archivContent" /> ' +
            ' <archivZiel type="my:archivFolder" /> ' +
            ' <searchItem name="fol" fix="Rechnungen Zauberfrau" /> ' +
            ' <searchItem name="tmp" fix="2015" />' +
            ' </archivTyp>' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        console.log(Logger.getMessages(true));
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest")).not.toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.properties["my:idvalue"]).toBe("99.233.620.0");
        expect(doc.properties["my:person"]).toBe("Test");
    });

    it("testWithNestedArchivZielWithError", function() {
        REC.currentDocument.properties.content.write("ZAUBERFRAU Rechnung");
        var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">' +
            ' <archivTyp name="Rechnung Zauberfrau" searchString="Zechnung">' +
            ' <archivZiel type="my:archivContent" /> ' +
            ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
            ' <archivZiel type="my:archivFolder" /> ' +
            ' </archivPosition>' +
            ' <searchItem name="tmp1" fix="2015" />' +
            ' </archivTyp>' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        var result = archivTyp.resolve();
        console.log(Logger.getMessages(false));
        expect(result).toBe(false);
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Unbekannt/WebScriptTest")).toBeNull();
    });

    it("testWithMissingMandatoryField", function() {
        var folder = REC.archivRoot.createFolder("Dokumente");
        folder = folder.createFolder("Rechnungen");
        folder = folder.createFolder("Rechnungen Zauberfrau");
        folder = folder.createFolder("2015");
        folder.createNode("WebScriptTest", "my:archivContent");
        REC.currentDocument.properties.content.write("ZAUBERFRAU");
        REC.mandatoryElements = ["cm:hansel"];
        var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">' +
            ' <archivZiel type="my:archivContent" /> ' +
            ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
            ' <archivZiel type="my:archivFolder" /> ' +
            ' </archivPosition>' +
            ' <searchItem name="tmp" fix="2015" />' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        console.log(Logger.getMessages(true));
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest")).not.toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).not.toBeNull();
    });

    it("testWithNestedArchivZiel", function() {
        REC.currentDocument.properties.content.write(  "ZAUBERFRAU Rechnung");
        var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">' +
            ' <archivTyp name="Rechnung Zauberfrau" searchString="Rechnung">' +
            ' <archivZiel type="my:archivContent" /> ' +
            ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
            ' <archivZiel type="my:archivFolder" /> ' +
            ' </archivPosition>' +
            ' <searchItem name="tmp" fix="2015" />' +
            ' </archivTyp>' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest")).not.toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });


    it("testNormal", function () {
        REC.currentDocument.properties.content.write("ZAUBERFRAU");
        var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">' +
            ' <archivZiel type="my:archivContent" /> ' +
            ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
            ' <archivZiel type="my:archivFolder" /> ' +
            ' </archivPosition>' +
            ' <searchItem name="tmp" fix="2015" />' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        console.log(Logger.getMessages(true));
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.isSubType("my:archivContent")).toBeTruthy();
        expect(doc.parent[0].isSubType("my:archivFolder")).toBeTruthy();
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });

    it("testDuplicateWithError1", function () {
        var folder = REC.archivRoot.createFolder("Dokumente");
        folder = folder.createFolder("Rechnungen");
        folder = folder.createFolder("Rechnungen Zauberfrau");
        folder = folder.createFolder("2015");
        folder.createNode("WebScriptTest", "my:archivContent");
        REC.currentDocument.properties.content.write(  "ZAUBERFRAU");
        var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">' +
            ' <archivZiel type="my:archivContent" /> ' +
            ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
            ' <archivZiel type="my:archivFolder" /> ' +
            ' </archivPosition>' +
            ' <searchItem name="tmp" fix="2015" />' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        doc = companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.isSubType("my:archivContent")).toBeTruthy();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });

    it("testDuplicateWithError2", function () {
        var folder = REC.archivRoot.createFolder("Dokumente");
        folder = folder.createFolder("Rechnungen");
        folder = folder.createFolder("Rechnungen Zauberfrau");
        folder = folder.createFolder("2015");
        var node = folder.createNode("WebScriptTest", "my:archivContent");
        node.setProperty("cm:title", "Rechnung 1");
        search.setFind(true, node);
        REC.currentDocument.properties.content.write("ZAUBERFRAU");
        var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU">' +
            ' <archivZiel type="my:archivContent" /> ' +
            ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
            ' <archivZiel type="my:archivFolder" /> ' +
            ' </archivPosition>' +
            ' <searchItem name="tmp" fix="2015" />' +
            ' <searchItem name="title" fix="Rechnung 1" />' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        doc = companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.isSubType("my:archivContent")).toBeTruthy();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });


    it("testDuplicateWithNothing1", function () {
        var folder = REC.archivRoot.createFolder("Dokumente");
        folder = folder.createFolder("Rechnungen");
        folder = folder.createFolder("Rechnungen Zauberfrau");
        folder = folder.createFolder("2015");
        folder.createNode("WebScriptTest", "my:archivContent");
        REC.currentDocument.properties.content.write("ZAUBERFRAU");
        var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU" unique="nothing">' +
            ' <archivZiel type="my:archivContent" /> ' +
            ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
            ' <archivZiel type="my:archivFolder" /> ' +
            ' </archivPosition>' +
            ' <searchItem name="tmp" fix="2015" />' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });

    it("testDuplicateWithOverwrite1", function () {
        var folder = REC.archivRoot.createFolder("Dokumente");
        folder = folder.createFolder("Rechnungen");
        folder = folder.createFolder("Rechnungen Zauberfrau");
        folder = folder.createFolder("2015");
        var node = folder.createNode("WebScriptTest", "my:archivContent");
        node.setProperty("my:person", "Till");
        REC.currentDocument.properties.content.write(  "ZAUBERFRAU");
        var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU" unique="overWrite">' +
            ' <archivZiel type="my:archivContent" /> ' +
            ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
            ' <archivZiel type="my:archivFolder" /> ' +
            ' </archivPosition>' +
            ' <searchItem name="tmp" fix="2015" />' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.properties["my:person"]).toBe("Klaus");
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });

    it("testDuplicateWithOverwrite2", function () {
        var folder = REC.archivRoot.createFolder("Dokumente");
        folder = folder.createFolder("Rechnungen");
        folder = folder.createFolder("Rechnungen Zauberfrau");
        folder = folder.createFolder("2015");
        var node = folder.createNode("Test", "my:archivContent");
        node.setProperty("cm:title", "Rechnung 1");
        search.setFind(true, node);
        REC.currentDocument.properties.content.write(  "ZAUBERFRAU");
        var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU" unique="overWrite">' +
            ' <archivZiel type="my:archivContent" /> ' +
            ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
            ' <archivZiel type="my:archivFolder" /> ' +
            ' </archivPosition>' +
            ' <searchItem name="tmp" fix="2015" />' +
            ' <searchItem name="title" fix="Rechnung 1" />' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/Test")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.properties["my:person"]).toBe("Klaus");
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });

    it("testDuplicateWithNewVersion1", function () {
        var folder = REC.archivRoot.createFolder("Dokumente");
        folder = folder.createFolder("Rechnungen");
        folder = folder.createFolder("Rechnungen Zauberfrau");
        folder = folder.createFolder("2015");
        var node = folder.createNode("WebScriptTest", "my:archivContent");
        node.properties.content.write(  "Hallo");
        REC.currentDocument.properties.content.write(  "ZAUBERFRAU");
        var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU" unique="newVersion">' +
            ' <archivZiel type="my:archivContent" /> ' +
            ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
            ' <archivZiel type="my:archivFolder" /> ' +
            ' </archivPosition>' +
            ' <searchItem name="tmp" fix="2015" />' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        console.log(Logger.getMessages(false));
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.isVersioned()).toBeTruthy();
        expect(doc.hasAspect(new BasicObject("cm:workingcopy"))).not.toBeTruthy();
        expect(doc.content).toBe("ZAUBERFRAU");
        var version = doc.getVersion(1);
        expect(version).not.toBeNull();
        expect(version.content).toBe("Hallo");
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });

    it("testDuplicateWithNewVersion2", function () {
        var folder = REC.archivRoot.createFolder("Dokumente");
        folder = folder.createFolder("Rechnungen");
        folder = folder.createFolder("Rechnungen Zauberfrau");
        folder = folder.createFolder("2015");
        var node = folder.createNode("Test", "my:archivContent");
        node.properties.content.write(  "Hallo");
        node.setProperty("cm:title", "Rechnung 1");
        search.setFind(true, node);
        REC.currentDocument.properties.content.write(  "ZAUBERFRAU");
        var rules = ' <archivTyp name="Zauberfrau" searchString="ZAUBERFRAU" unique="newVersion">' +
            ' <archivZiel type="my:archivContent" /> ' +
            ' <archivPosition folder="Dokumente/Rechnungen/Rechnungen Zauberfrau/{tmp}"> ' +
            ' <archivZiel type="my:archivFolder" /> ' +
            ' </archivPosition>' +
            ' <searchItem name="tmp" fix="2015" />' +
            ' <searchItem name="title" fix="Rechnung 1" />' +
            ' </archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/Test");
        expect(doc).not.toBeNull();
        expect(doc.isVersioned()).toBeTruthy();
        expect(doc.hasAspect(new BasicObject("cm:workingcopy"))).not.toBeTruthy();
        expect(doc.properties.content.content).toBe("ZAUBERFRAU");
        var version = doc.getVersion(1);
        expect(version).not.toBeNull();
        expect(version.properties.content.content).toBe("Hallo");
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });

    it("testComplete1", function () {
        REC.currentDocument.properties.content.write(  "Verdienstabrechnung     0000123456  3000 Abrechnungsmonat Mai 2015");
        var rules = '<archivTyp name="LVMGehalt" searchString="Verdienstabrechnung" debugLevel="debug">                              ' +
            ' <archivZiel type="my:archivContent" />                                                      ' +
            '<archivPosition folder="Dokumente/Gehalt/Gehalt Hansel/{tmp}">                                              ' +
            '<archivZiel type="my:archivFolder" />                                                                    ' +
            '</archivPosition>                                                                                        ' +
            '<archivPosition link="true" folder="Dokumente/Hansel/Gehalt Hansel">                                           ' +
            '<archivZiel type="my:archivFolder" />                                                                    ' +
            '</archivPosition>                                                                                        ' +
            '<tags name="Gehalt" />                                                                                   ' +
            '<tags name="Hansel" />                                                                                      ' +
            '<category name="Gehalt/Gehalt Hansel" />                                                                  ' +
            '<searchItem name="person" fix="Hansel" target="my:person" />                                              ' +
            '<searchItem name="tmp" objectTyp="date" value="datum">                                                   ' +
            '<format formatString="YYYY" />                                                                           ' +
            '</searchItem>                                                                                            ' +
            '<archivTyp name="Rückrechnung" searchString="Rückrechnungsdifferenz">                                    ' +
            '<tags name="Rückrechnung" />                                                                             ' +
            '<searchItem name="titel" text="Abrechnungsmonat" word="2,2"  />  ' +
            '<searchItem name="title" fix="Rückrechnung {titel}" target="cm:title"/>  ' +
            '<searchItem name="datum" text="Abrechnungsmonat" word="2,2" objectTyp="date" target="my:documentDate">   ' +
            '<check lowerValue="01/01/2005" upperValue="01/01/2020" />                                                ' +
            '</searchItem>                                                                                            ' +
            '<searchItem name="betrag" text="Rückrechnungsdifferenz" objectTyp="float" target="my:amount">            ' +
            '<check lowerValue="-200" upperValue="200" />                                                             ' +
            '<delimitter typ="start" text="&#0032;" count="1" removeBlanks="after" />                                 ' +
            '<archivZiel aspect="my:amountable" />                                                                    ' +
            '</searchItem>                                                                                            ' +
            '</archivTyp>                                                                                             ' +
            '<archivTyp name="Verdienstabrechnung" searchString="" unique="error">                                    ' +
            '<searchItem name="title" text="Abrechnungsmonat" word="1,2" target="cm:title" />                         ' +
            '<searchItem name="datum" value="title" objectTyp="date" target="my:documentDate">                        ' +
            '<check lowerValue="01/01/2005" upperValue="01/01/2020" />                                                ' +
            '</searchItem>                                                                                            ' +
            '<searchItem name="betrag" text="0000123456" objectTyp="float" target="my:amount">                        ' +
            '<check lowerValue="3000" upperValue="15000" />                                                           ' +
            '<archivZiel aspect="my:amountable" />                                                                    ' +
            '</searchItem>                                                                                            ' +
            '</archivTyp>                                                                                             ' +
            '</archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Gehalt/Gehalt Hansel/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.isSubType("my:archivContent")).toBeTruthy();
        expect(doc.properties["my:amount"]).toBe(3000);
        expect(doc.properties["my:documentDate"].getTime()).toBe(new Date(2015, 4, 1).getTime());
        expect(doc.properties["cm:title"]).toBe("Mai 2015");
        expect(doc.properties["my:person"]).toBe("Hansel");
        expect(doc.hasTag("Gehalt")).toBeTruthy();
        expect(doc.hasTag("Hansel")).toBeTruthy();
        expect(doc.hasAspect("my:amountable")).toBeTruthy();
        expect(doc.properties["cm:categories"][0].name).toBe("Gehalt Hansel");
        //assertTrue(doc.isCategory());
        //assertTrue(doc.category.contains(new BasicObject("")));
        var linkDoc = companyhome.childByNamePath("/Archiv/Dokumente/Hansel/Gehalt Hansel/WebScriptTest");
        expect(linkDoc).not.toBeNull();
        expect(linkDoc.isSubType("my:archivContent")).toBeTruthy();
        expect(linkDoc.properties["my:amount"]).toBe(3000);
        expect(linkDoc.properties["my:documentDate"].getTime()).toBe(new Date(2015, 4, 1).getTime());
        expect(linkDoc.properties["cm:title"]).toBe("Mai 2015");
        expect(linkDoc.properties["my:person"]).toBe("Hansel");
        expect(linkDoc.hasTag("Gehalt")).toBeTruthy();
        expect(linkDoc.hasTag("Hansel")).toBeTruthy();
        expect(linkDoc.hasAspect("my:amountable")).toBeTruthy();
        expect(linkDoc.parent[0].isSubType("my:archivFolder")).toBeTruthy();
        expect(linkDoc.properties["cm:categories"][0].name).toBe("Gehalt Hansel");
        expect(doc.id).toBe(linkDoc.id);
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });


    it("testComplete2", function () {
        REC.currentDocument.properties.content.write(  "Verdienstabrechnung     0000123456 Rückrechnungsdifferenz 200 Abrechnungsmonat R Mai 2015");
        var rules = '<archivTyp name="LVMGehalt" searchString="Verdienstabrechnung" debugLevel="debug">                              ' +
            ' <archivZiel type="my:archivContent" />                                                      ' +
            '<archivPosition folder="Dokumente/Gehalt/Gehalt Hansel/{tmp}">                                              ' +
            '<archivZiel type="my:archivFolder" />                                                                    ' +
            '</archivPosition>                                                                                        ' +
            '<archivPosition link="true" folder="Dokumente/Hansel/Gehalt Hansel">                                           ' +
            '<archivZiel type="my:archivFolder" />                                                                    ' +
            '</archivPosition>                                                                                        ' +
            '<tags name="Gehalt" />                                                                                   ' +
            '<tags name="Hansel" />                                                                                      ' +
            '<category name="Gehalt/Gehalt Hansel" />                                                                  ' +
            '<searchItem name="person" fix="Hansel" target="my:person" />                                              ' +
            '<searchItem name="tmp" objectTyp="date" value="datum">                                                   ' +
            '<format formatString="YYYY" />                                                                           ' +
            '</searchItem>                                                                                            ' +
            '<archivTyp name="Rückrechnung" searchString="Rückrechnungsdifferenz">                                    ' +
            '<tags name="Rückrechnung" />                                                                             ' +
            '<searchItem name="titel" text="Abrechnungsmonat" word="2,2"  />  ' +
            '<searchItem name="title" fix="Rückrechnung {titel}" target="cm:title"/>  ' +
            '<searchItem name="datum" text="Abrechnungsmonat" word="2,2" objectTyp="date" target="my:documentDate">   ' +
            '<check lowerValue="01/01/2005" upperValue="01/01/2020" />                                                ' +
            '</searchItem>                                                                                            ' +
            '<searchItem name="betrag" text="Rückrechnungsdifferenz" objectTyp="float" target="my:amount">            ' +
            '<check lowerValue="-200" upperValue="200" />                                                             ' +
            '<delimitter typ="start" text="&#0032;" count="1" removeBlanks="after" />                                 ' +
            '<archivZiel aspect="my:amountable" />                                                                    ' +
            '</searchItem>                                                                                            ' +
            '</archivTyp>                                                                                             ' +
            '<archivTyp name="Verdienstabrechnung" searchString="" unique="error">                                    ' +
            '<searchItem name="title" text="Abrechnungsmonat" word="1,2" target="cm:title" />                         ' +
            '<searchItem name="datum" value="title" objectTyp="date" target="my:documentDate">                        ' +
            '<check lowerValue="01/01/2005" upperValue="01/01/2020" />                                                ' +
            '</searchItem>                                                                                            ' +
            '<searchItem name="betrag" text="0000123456" objectTyp="float" target="my:amount">                        ' +
            '<check lowerValue="3000" upperValue="15000" />                                                           ' +
            '<archivZiel aspect="my:amountable" />                                                                    ' +
            '</searchItem>                                                                                            ' +
            '</archivTyp>                                                                                             ' +
            '</archivTyp>';
        XMLDoc.loadXML(rules);
        XMLDoc.parse();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).not.toBeNull();
        var archivTyp = new ArchivTyp(new XMLObject(XMLDoc.docNode));
        archivTyp.resolve();
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Gehalt/Gehalt Hansel/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.isSubType("my:archivContent")).toBeTruthy();
        expect(doc.properties["my:amount"]).toBe(200);
        expect(doc.properties["my:documentDate"].getTime()).toBe(new Date(2015, 4, 1).getTime());
        expect(doc.properties["cm:title"]).toBe("Rückrechnung Mai 2015");
        expect(doc.properties["my:person"]).toBe("Hansel");
        expect(doc.hasTag("Gehalt")).toBeTruthy();
        expect(doc.hasTag("Hansel")).toBeTruthy();
        expect(doc.hasAspect("my:amountable")).toBeTruthy();
        expect(doc.properties["cm:categories"][0].name).toBe("Gehalt Hansel");
        //assertTrue(doc.isCategory());
        //assertTrue(doc.category.contains(new BasicObject("")));
        var linkDoc = companyhome.childByNamePath("/Archiv/Dokumente/Hansel/Gehalt Hansel/WebScriptTest");
        expect(linkDoc).not.toBeNull();
        expect(linkDoc.isSubType("my:archivContent")).toBeTruthy();
        expect(linkDoc.properties["my:amount"]).toBe(200);
        expect(linkDoc.properties["my:documentDate"].getTime()).toBe(new Date(2015, 4, 1).getTime());
        expect(linkDoc.properties["cm:title"]).toBe("Rückrechnung Mai 2015");
        expect(linkDoc.properties["my:person"]).toBe("Hansel");
        expect(linkDoc.hasTag("Gehalt")).toBeTruthy();
        expect(linkDoc.hasTag("Hansel")).toBeTruthy();
        expect(linkDoc.hasAspect("my:amountable")).toBeTruthy();
        expect(linkDoc.parent[0].isSubType("my:archivFolder")).toBeTruthy();
        expect(linkDoc.properties["cm:categories"][0].name).toBe("Gehalt Hansel");
        expect(doc.id).toBe(linkDoc.id);
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });


});




