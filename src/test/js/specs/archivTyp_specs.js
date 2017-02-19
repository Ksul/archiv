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
        REC.currentDocument.properties.content.write(new Content("Test"));
        search.setFind(false);
    });

    it("testNormal", function () {
        REC.content ="ZAUBERFRAU";
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
        REC.content ="ZAUBERFRAU";
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
        REC.content ="ZAUBERFRAU";
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
        REC.content ="ZAUBERFRAU";
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
        REC.content ="ZAUBERFRAU";
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
        REC.content ="ZAUBERFRAU";
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
        node.properties.content.write(new Content("Hallo"));
        REC.content ="ZAUBERFRAU";
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
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        var doc = companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest");
        expect(doc).not.toBeNull();
        expect(doc.isVersioned()).toBeTruthy();
        expect(doc.hasAspect(new BasicObject("cm:workingcopy"))).not.toBeTruthy();
        expect(doc.properties.content.content).toBe("Test");
        var version = doc.getVersion(1);
        expect(version).not.toBeNull();
        expect(version.properties.content.content).toBe("Hallo");
        version = doc.getVersion(2);
        expect(version).not.toBeNull();
        expect(version.properties.content.content).toBe("Test");
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });

    it("testDuplicateWithNewVersion2", function () {
        var folder = REC.archivRoot.createFolder("Dokumente");
        folder = folder.createFolder("Rechnungen");
        folder = folder.createFolder("Rechnungen Zauberfrau");
        folder = folder.createFolder("2015");
        var node = folder.createNode("Test", "my:archivContent");
        node.properties.content.write(new Content("Hallo"));
        node.setProperty("cm:title", "Rechnung 1");
        search.setFind(true, node);
        REC.content ="ZAUBERFRAU";
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
        expect(doc.properties.content.content).toBe("Test");
        var version = doc.getVersion(1);
        expect(version).not.toBeNull();
        expect(version.properties.content.content).toBe("Hallo");
        version = doc.getVersion(2);
        expect(version).not.toBeNull();
        expect(version.properties.content.content).toBe("Test");
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).toBeNull();
    });

    it("testComplete1", function () {
        REC.content ="Verdienstabrechnung     0000123456  3000 Abrechnungsmonat Mai 2015";
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
        REC.content ="Verdienstabrechnung     0000123456 Rückrechnungsdifferenz 200 Abrechnungsmonat R Mai 2015";
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

    it("testWithMissingMandatoryField", function() {
        var folder = REC.archivRoot.createFolder("Dokumente");
        folder = folder.createFolder("Rechnungen");
        folder = folder.createFolder("Rechnungen Zauberfrau");
        folder = folder.createFolder("2015");
        folder.createNode("WebScriptTest", "my:archivContent");
        REC.content ="ZAUBERFRAU";
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
        expect(companyhome.childByNamePath("/Archiv/Inbox/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Dokumente/Rechnungen/Rechnungen Zauberfrau/2015/WebScriptTest")).not.toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/Doppelte/WebScriptTest")).toBeNull();
        expect(companyhome.childByNamePath("/Archiv/Fehler/WebScriptTest")).not.toBeNull();
    });
});




